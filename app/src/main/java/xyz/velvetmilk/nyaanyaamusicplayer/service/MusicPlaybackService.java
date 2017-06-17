package xyz.velvetmilk.nyaanyaamusicplayer.service;

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

import java.lang.ref.WeakReference;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.activity.BaseActivity;
import xyz.velvetmilk.nyaanyaamusicplayer.media.MusicPlayer;
import xyz.velvetmilk.nyaanyaamusicplayer.receiver.MediaButtonIntentReceiver;

public class MusicPlaybackService extends Service implements
        AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MusicPlaybackService.class.getSimpleName();
    private IBinder binder;
    private MusicPlayer musicPlayer;
    private AlarmManager alarmManager;
    private PendingIntent shutdownPendingIntent;
    private MediaSession mediaSession;
    private MediaController mediaController;
    private AudioManager audioManager;

    public Notification musicNotification;

    // 5 minutes allowed to be inactive before death
    private static final int DELAY_TIME = 5 * 60 * 1000;
    public static final String ACTION_SHUTDOWN = "SHUTDOWN";
    private static final int MUSIC_NOTIFICATION_ID = 1;



    // ========================================================================
    // Service lifecycle overrides
    // ========================================================================

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        binder = new NyaaNyaaMusicServiceStub(this);
        setupAlarms();

        setupMediaSession();
        setupNotification();
        mediaController = mediaSession.getController();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        musicPlayer = new MusicPlayer(mediaSession);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBind");

        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onRebind");

        cancelDelayedShutdown();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand");

        // @TODO find out when intent can be null
        // intent can be null (not sure when)
        if (intent == null) {
            return START_STICKY;
        }

        final String action = intent.getAction();
        if (ACTION_SHUTDOWN.equals(action)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "ACTION_SHUTDOWN called");

            stopSelf();
            return START_NOT_STICKY;
        }

        handleCommand(intent);

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onUnbind");

        scheduleDelayedShutdown();

        return true;
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");

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
        musicPlayer.setMusicId(musicId);

        // find the location from the MediaStore
        Cursor cursor = makeMusicLocationCursor(musicId);

        if (cursor == null) {
            return false;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found more than 1 item");
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

        return musicPlayer.load(loc);
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        int status = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }

        startForeground(MUSIC_NOTIFICATION_ID, musicNotification);

        musicPlayer.start();
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        stopForeground(true);
        musicPlayer.pause();
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        stopForeground(true);
        audioManager.abandonAudioFocus(this);
        musicPlayer.stop();
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        stopForeground(true);
        audioManager.abandonAudioFocus(this);
        musicPlayer.reset();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setVolume");

        musicPlayer.setVolume(leftVolume, rightVolume);
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

    private void handleCommand(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "handleCommand");


        int keyCode = intent.getIntExtra("KEYCODE", 0);

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

    private void scheduleDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "scheduleDelayedShutdown");

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + DELAY_TIME, shutdownPendingIntent);
    }

    private void cancelDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "cancelDelayedShutdown");

        alarmManager.cancel(shutdownPendingIntent);
    }

    private void setupAlarms() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupAlarms");

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MusicPlaybackService.class);
        intent.setAction(ACTION_SHUTDOWN);

        shutdownPendingIntent = PendingIntent.getService(this, 0, intent, 0);
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
