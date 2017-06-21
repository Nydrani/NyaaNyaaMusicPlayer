package xyz.lostalishar.nyaanyaamusicplayer.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.activity.BaseActivity;
import xyz.lostalishar.nyaanyaamusicplayer.media.MusicPlayer;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.receiver.MediaButtonIntentReceiver;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

/*
 * MusicPlaybackService is the main service behind the app background audio playback.
 * It must be run with the preconditions:
 *   1. It has READ_EXTERNAL_STORE permissions. (otherwise all entrances to the app are barred)
 *     a. @TODO if the user changes the permissions as the service is running, -->
 *     a. @TODO it will leak some managers and sessions (FIX THIS)
 *
 * After some time (SHUTDOWN_DELAY_TIME) it will automatically shutdown.
 */

public class MusicPlaybackService extends Service implements
        AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MusicPlaybackService.class.getSimpleName();
    private IBinder binder;
    private MusicPlayer musicPlayer;
    private AlarmManager alarmManager;
    private PendingIntent shutdownPendingIntent;

    // @TODO quick hacky fix for fixing session state when onCompleted called in MusicPlayer.class (made public)
    public MediaSession mediaSession;
    private MediaController mediaController;
    public AudioManager audioManager;

    public Notification musicNotification;

    // 1 minutes allowed to be inactive before death for testing (@TODO service killed before song finishes)
    private static final int SHUTDOWN_DELAY_TIME = 60 * 1000;
    public static final String ACTION_SHUTDOWN = "SHUTDOWN";
    private static final int MUSIC_NOTIFICATION_ID = 1;


    // ========================================================================
    // Service lifecycle overrides
    // ========================================================================

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        // die if service doesn't have necessary permissions
        if (NyaaUtils.needsPermissions(this)) {
            stopSelf();
            return;
        }

        // init service
        binder = new NyaaNyaaMusicServiceStub(this);
        setupAlarms();

        setupMediaSession();
        setupNotification();
        mediaController = mediaSession.getController();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        musicPlayer = new MusicPlayer(this);

        // restore previous playback state
        loadPlaybackState();

        // schedule shutdown for idle service
        scheduleDelayedShutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBind");

        // race condition may happen if onBind or onUnbind gets called before onDestroy when stopSelf()
        // is called in onCreate() due to missing permissions
        // so we need to check here if we have permissions since we only instantiate when we do
        if (NyaaUtils.needsPermissions(this)) {
            return null;
        }

        cancelDelayedShutdown();
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onRebind");

        // permissions are needed
        if (NyaaUtils.needsPermissions(this)) {
            return;
        }

        cancelDelayedShutdown();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand");

        // we need to check for permissions here if the user ever runs the service and then denies
        // the permissions later
        if (NyaaUtils.needsPermissions(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // @TODO find out when intent can be null
        // intent can be null (not sure when)
        // make sure to call for a shutdown since it sits in idle
        if (intent == null) {
            scheduleDelayedShutdown();
            return START_STICKY;
        }

        final String action = intent.getAction();
        if (BuildConfig.DEBUG) Log.d(TAG, "Action: " + action + " called");

        if (ACTION_SHUTDOWN.equals(action)) {
            savePlaybackState();
            stopSelf();
            return START_NOT_STICKY;
        }

        handleCommand(intent);

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onUnbind");

        // race condition may happen if onBind or onUnbind gets called before onDestroy when stopSelf()
        // is called in onCreate() due to missing permissions
        // this results in binding when no fields have been instantiated
        // so we need to check here if we have permissions since we only instantiate when we do
        if (!(NyaaUtils.needsPermissions(this))) {
            savePlaybackState();
            scheduleDelayedShutdown();
        }

        return true;
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");

        // early die if service never had the necessary permissions
        if (NyaaUtils.needsPermissions(this)) {
            return;
        }

        // make sure to cancel lingering AlarmManager tasks
        cancelDelayedShutdown();

        // stop running as foreground service
        stopForeground(true);

        // release media session
        mediaSession.release();

        // abandon audio since we no longer need
        audioManager.abandonAudioFocus(this);

        // release music player
        if (musicPlayer != null) {
            musicPlayer.reset();
            musicPlayer.release();
        }
    }


    // ========================================================================
    // Exposed functions for clients
    // ========================================================================

    public boolean load(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        // store song id
        musicPlayer.musicId = musicId;

        // find the location from the MediaStore
        Cursor cursor = makeMusicLocationCursor(musicId);

        if (cursor == null) {
            return false;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found more/less than 1 item");
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Item: " + cursor.getString(dataColumn));
            }

            cursor.close();
            return false;
        }

        // success
        cursor.moveToFirst();
        String loc = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        cursor.close();

        try {
            musicPlayer.load(loc);

            updateMediaSession("STOP");
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Unable to load data source: " + loc);
            return false;
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called prepare in illegal state");
            return false;
        }

        return true;
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        int status = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }

        try {
            musicPlayer.start();

            // @TODO extra functionality
            //   1. make sure the app doesn't schedule to kill itself
            //   2. update the media session to play, pause, stop etc status
            //   3. enable listening to play/pause buttons from quick settings/hardware buttons
            //   4. don't get killed by OS
            cancelDelayedShutdown();
            updateMediaSession("PLAY");
            mediaSession.setActive(true);
            startForeground(MUSIC_NOTIFICATION_ID, musicNotification);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called start in illegal state");
        }
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        try {
            musicPlayer.pause();

            scheduleDelayedShutdown();
            updateMediaSession("PAUSE");
            stopForeground(true);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called pause in illegal state");
        }
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        try {
            musicPlayer.stop();

            scheduleDelayedShutdown();
            updateMediaSession("STOP");
            mediaSession.setActive(false);
            stopForeground(true);
            audioManager.abandonAudioFocus(this);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called stop in illegal state");
        }

    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        musicPlayer.reset();

        scheduleDelayedShutdown();
        updateMediaSession("RESET");
        mediaSession.setActive(false);
        stopForeground(true);
        audioManager.abandonAudioFocus(this);
    }

    // @TODO make private since not exposed function
    public void seekTo(int msec) {
        if (BuildConfig.DEBUG) Log.d(TAG, "seekTo");

        musicPlayer.seekTo(msec);
    }

    // @TODO make private since not exposed function
    public void setVolume(float leftVolume, float rightVolume) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setVolume");

        musicPlayer.setVolume(leftVolume, rightVolume);
    }

    // @TODO make private since not exposed function
    public int getCurrentPosition() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPosition");

        return musicPlayer.getCurrentPosition();
    }

    // @TODO make private since not exposed function
    public long getCurrentId() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentId");

        return musicPlayer.musicId;
    }


    // ========================================================================
    // MusicPlaybackService helper functions
    // ========================================================================

    private Cursor makeMusicLocationCursor(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicLocationCursor");

        ContentResolver musicResolver = getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media.DATA };
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { String.valueOf(musicId) };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        return musicResolver.query(musicUri, projection, selection, selectionArgs, sortOrder);
    }

    private void setupMediaSession() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupMediaSession");

        mediaSession = new MediaSession(this, TAG);

        MediaSession.Callback mediaSessionCallback = new MediaSession.Callback() {
            private final String TAG = MusicPlaybackService.class.getSimpleName();

            @Override
            public void onPlay() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onPlay");

                start();
            }

            @Override
            public void onPause() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onPause");

                pause();
            }

            @Override
            public void onStop() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onStop");

                stop();
            }

            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onMediaButtonEvent");

                final KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                // event should never be null --> but do sanity check just in case
                if (event != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, event.toString());
                } else {
                    if (BuildConfig.DEBUG) Log.w(TAG, "KeyEvent not found");
                }

                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        };

        mediaSession.setCallback(mediaSessionCallback);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                new Intent(this, MediaButtonIntentReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setMediaButtonReceiver(pi);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);

        long playBackStateActions =
                PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_STOP;
        PlaybackState newState = new PlaybackState.Builder()
                .setActions(playBackStateActions)
                .setState(PlaybackState.STATE_NONE, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build();
        mediaSession.setPlaybackState(newState);
    }

    private void setupAlarms() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupAlarms");

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MusicPlaybackService.class);
        intent.setAction(ACTION_SHUTDOWN);

        shutdownPendingIntent = PendingIntent.getService(this, 0, intent, 0);
    }

    private void setupNotification() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupNotification");

        Intent activityIntent = new Intent(this, BaseActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        musicNotification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                .setContentText(getText(R.string.service_musicplayback_notification_message))
                .setSmallIcon(android.R.drawable.star_on)
                .setContentIntent(activityPendingIntent)
                .build();
    }

    // @TODO update later to make this function only handle KEYCODES while having a generic function
    // @TODO to delegate which function to which intent
    private void handleCommand(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "handleCommand");

        final int keyCode = intent.getIntExtra("KEYCODE", 0);

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                start();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                togglePlayPause();
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                pause();
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                stop();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown keycode provided: " + KeyEvent.keyCodeToString(keyCode));
                break;
        }
    }

    private void togglePlayPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "togglePlayPause");

        // getPlaybackState can return null. Early exit if so
        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "PlaybackState not found");
            return;
        }

        if (playbackState.getState() == PlaybackState.STATE_PLAYING) {
            pause();
        } else if (playbackState.getState() == PlaybackState.STATE_PAUSED) {
            start();
        }
    }

    // @TODO QUICK HACKY FIX to allow music player onCompletion to shut down the service
    // @TODO fix later by using a handler (message passing)
    public void scheduleDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "scheduleDelayedShutdown");

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + SHUTDOWN_DELAY_TIME, shutdownPendingIntent);
    }

    private void cancelDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "cancelDelayedShutdown");

        alarmManager.cancel(shutdownPendingIntent);
    }

    // @TODO quick hacky fix for updating session from within MusicPlayer.class (made public)
    public void updateMediaSession(String state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMediaSession");

        // @TODO debugging
        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No play state found");
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "State before: " + String.valueOf(playbackState.getState()));
        }


        long playBackStateActions =
                PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_STOP;

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(playBackStateActions)
                .setActiveQueueItemId(musicPlayer.musicId);

        switch (state) {
            case "PLAY":
                stateBuilder.setState(PlaybackState.STATE_PLAYING, musicPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "PAUSE":
                stateBuilder.setState(PlaybackState.STATE_PAUSED, musicPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "STOP":
                stateBuilder.setState(PlaybackState.STATE_STOPPED, musicPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "RESET":
                stateBuilder.setState(PlaybackState.STATE_NONE, PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                        1.0f);
                break;
            case "ERROR":
                stateBuilder.setState(PlaybackState.STATE_ERROR, PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                        1.0f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown state received: " + state);
                return;
        }

        // @TODO debugging
        PlaybackState newState = stateBuilder.build();
        mediaSession.setPlaybackState(newState);

        playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No play state found");
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "State after: " + String.valueOf(playbackState.getState()));
        }
    }

    private void savePlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "savePlaybackState");

        // @TODO need to check if the music player is loaded otherwise calling getCurrentPosition
        // @TODO and getCurrentId is erroneous
        MusicPlaybackState state = new MusicPlaybackState(getCurrentId(), getCurrentPosition());
        PreferenceUtils.saveCurPlaying(this, state);
    }

    // @TODO could refactor to instead store the loaded state in the service rather
    // @TODO than the MusicPlayer class and let the service take care of loading
    private void loadPlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadPlaybackState");

        MusicPlaybackState state = PreferenceUtils.loadCurPlaying(this);

        // die if load failed, probably due to ID not found
        if (!(load(state.getMusicId()))) {
            return;
        }

        seekTo(state.getMusicPos());
        // make sure to update the MediaSession upon loading
        updateMediaSession("PAUSE");
    }


    // ========================================================================
    // AudioManager listener overrides
    // ========================================================================

    @Override
    public void onAudioFocusChange(int change) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onAudioFocusChange");

        switch (change) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_GAIN");

                setVolume(1.0f, 1.0f);
                start();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE");
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_LOSS");

                stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");

                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                setVolume(0.2f, 0.2f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED");
                break;
        }
    }


    // ========================================================================
    // AIDL music playback service implementation for binder
    // ========================================================================

    private static class NyaaNyaaMusicServiceStub extends INyaaNyaaMusicService.Stub {
        private static final String TAG = NyaaNyaaMusicServiceStub.class.getSimpleName();

        private final WeakReference<MusicPlaybackService> musicPlaybackService;

        private NyaaNyaaMusicServiceStub(MusicPlaybackService service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            musicPlaybackService = new WeakReference<>(service);
        }

        @Override
        public boolean load(long musicId) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "load");

            return musicPlaybackService.get().load(musicId);
        }

        @Override
        public void start() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "start");

            musicPlaybackService.get().start();
        }

        @Override
        public void pause() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "pause");

            musicPlaybackService.get().pause();
        }

        @Override
        public void stop() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "stop");

            musicPlaybackService.get().stop();
        }

        @Override
        public void reset() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "reset");

            musicPlaybackService.get().reset();
        }
    }
}
