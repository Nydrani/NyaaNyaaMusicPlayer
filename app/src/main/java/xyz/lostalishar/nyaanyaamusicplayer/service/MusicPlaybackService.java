package xyz.lostalishar.nyaanyaamusicplayer.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.activity.HomeActivity;
import xyz.lostalishar.nyaanyaamusicplayer.media.MusicPlayer;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;
import xyz.lostalishar.nyaanyaamusicplayer.provider.MusicDatabaseProvider;
import xyz.lostalishar.nyaanyaamusicplayer.provider.PlaybackQueueSQLHelper;
import xyz.lostalishar.nyaanyaamusicplayer.receiver.MediaButtonIntentReceiver;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

/*
 * MusicPlaybackService is the main service behind the app background audio playback.
 * It must be run with the preconditions:
 *   1. It has READ_EXTERNAL_STORE permissions. (otherwise all entrances to the app are barred)
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

    private HandlerThread databaseThread;
    private Handler databaseHandler;

    private MediaSession mediaSession;
    private AudioManager audioManager;
    private MediaController mediaController;

    private Notification musicNotification;

    private MusicPlaybackState musicPlaybackState;
    private List<MusicPlaybackTrack> musicQueue;

    // 1 minutes allowed to be inactive before death for testing
    private static final int SHUTDOWN_DELAY_TIME = 60 * 1000;

    public static final String ACTION_SHUTDOWN = "SHUTDOWN";
    public static final String ACTION_EXTRA_KEYCODE = "KEYCODE";
    public static final String THREAD_DATABASE = "DatabaseThread";
    public static final int UNKNOWN_POS = -1;
    public static final long UNKNOWN_ID = -1;

    private static final int UPDATE_QUEUE = 0;
    private static final int MUSIC_NOTIFICATION_ID = 1;

    private NoisyAudioReceiver noisyAudioReceiver;


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
        setupReceivers();
        setupAlarms();

        // setup threads
        databaseThread = new HandlerThread(THREAD_DATABASE, Process.THREAD_PRIORITY_BACKGROUND);
        databaseThread.start();
        databaseHandler = new DatabaseHandler(this, databaseThread.getLooper());

        // setup music state
        musicPlayer = new MusicPlayer(this);
        musicPlaybackState = new MusicPlaybackState(UNKNOWN_POS, 0);
        musicQueue = new ArrayList<>();

        // setup misc services
        setupMediaSession();
        setupNotification();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // restore playback queue
        loadPlaybackQueue();

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
        // intent can be null (called when process died since START_STICKY on app close)
        // make sure to call for a shutdown since it sits in idle
        if (intent == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "INTENT IS NULL (CHECK ME OUT)");
            scheduleDelayedShutdown();
            return START_STICKY;
        }

        final String action = intent.getAction();
        if (BuildConfig.DEBUG) Log.d(TAG, "Action: " + action + " called");

        if (ACTION_SHUTDOWN.equals(action)) {
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
            // only schedule if service is not in use
            if (!(musicPlayer.isPlaying())) {
                scheduleDelayedShutdown();
            }
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

        // unregister receivers
        unregisterReceivers();

        // make sure to cancel lingering AlarmManager tasks
        cancelDelayedShutdown();

        // stop background threads
        databaseThread.quitSafely();

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

    public boolean load(int queuePos) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        // early exit if out of bounds queue position
        if (queuePos <= UNKNOWN_POS || queuePos >= musicQueue.size()) {
            return false;
        }

        // early exit if there are no items in the queue
        if (musicQueue.size() == 0) {
            return false;
        }

        // find the location from the MediaStore
        Cursor cursor = makeMusicLocationCursor(musicQueue.get(queuePos).getId());

        if (cursor == null) {
            return false;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found " + cursor.getCount() + " item(s)");
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

            // store song id on success
            // musicPlayer.musicId = musicId;
            musicPlaybackState.setQueuePos(queuePos);
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);

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

    public void play() {
        if (BuildConfig.DEBUG) Log.d(TAG, "play");

        int status = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }

        try {
            musicPlayer.start();

            // @TODO extra functionality (move somewhere else later)
            //   1. make sure the app doesn't schedule to kill itself
            //   2. update the media session to play, pause, stop etc status
            //   3. enable listening to play/pause buttons from quick settings/hardware buttons
            //   4. update the notification
            //   5. don't get killed by OS
            //   6. send notification of meta changing to UI
            //   7. save current playback state
            cancelDelayedShutdown();
            updateMediaSession("PLAY");
            mediaSession.setActive(true);
            startForeground(MUSIC_NOTIFICATION_ID, buildNotification());
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
            savePlaybackState();
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
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
            savePlaybackState();
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
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
            savePlaybackState();
            audioManager.abandonAudioFocus(this);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called stop in illegal state");
        }
    }

    public void next() {
        if (BuildConfig.DEBUG) Log.d(TAG, "next");

        int nextQueuePos = (musicPlaybackState.getQueuePos() + 1) % musicQueue.size();

        reset();
        load(nextQueuePos);
        play();
    }

    public void previous() {
        if (BuildConfig.DEBUG) Log.d(TAG, "previous");

        // apparently mod in java behaves as  { a % b = a - a / b * b }
        int prevQueuePos = ((musicPlaybackState.getQueuePos() - 1) %
                musicQueue.size() + musicQueue.size()) % musicQueue.size();

        reset();
        load(prevQueuePos);
        play();
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        musicPlayer.reset();

        scheduleDelayedShutdown();
        updateMediaSession("RESET");
        mediaSession.setActive(false);
        stopForeground(true);
        NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
        audioManager.abandonAudioFocus(this);
    }

    public List<MusicPlaybackTrack> getQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getQueue");

        return musicQueue;
    }

    public MusicPlaybackState getState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getState");

        return musicPlaybackState;
    }

    public int addToQueue(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "addToQueue");

        // @TODO for now only allow item in queue when it doesn't already exist (return old pos)
        for (int i = 0; i < musicQueue.size(); i++) {
            if (musicQueue.get(i).getId() == musicId) {
                return i;
            }
        }

        MusicPlaybackTrack track = new MusicPlaybackTrack(musicId);
        musicQueue.add(track);
        NyaaUtils.notifyChange(this, NyaaUtils.QUEUE_CHANGED);

        // @TODO for now update queue database in here (change to use message handling later)
        //updatePlaybackQueue(true, track);
        databaseHandler.post(new Runnable() {
            @Override
            public void run() {
                savePlaybackQueue();
            }
        });
        // savePlaybackQueue();

        return musicQueue.indexOf(track);
    }

    public long removeFromQueue(int pos) {
        if (BuildConfig.DEBUG) Log.d(TAG, "removeFromQueue");

        // early exit if out of bounds queue position
        if (pos <= UNKNOWN_POS || pos >= musicQueue.size()) {
            return UNKNOWN_ID;
        }

        // early exit if there are no items in the queue
        if (musicQueue.size() == 0) {
            return UNKNOWN_ID;
        }

        // reset music player only if chosen was currently playing
        if (pos == musicPlaybackState.getQueuePos()) {
            reset();
        }

        long id = musicQueue.get(pos).getId();
        musicQueue.remove(pos);
        updatePlaybackState();


        NyaaUtils.notifyChange(this, NyaaUtils.QUEUE_CHANGED);

        // @TODO for now update queue database in here (change to use message handling later)
        // updatePlaybackQueue(false, track);
        databaseHandler.post(new Runnable() {
            @Override
            public void run() {
                savePlaybackQueue();
            }
        });
        // savePlaybackQueue();

        return id;
    }

    public int clearQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "clearQueue");

        // reset music player to unload current playing track
        reset();

        int numCleared = musicQueue.size();
        musicQueue.clear();
        updatePlaybackState();

        NyaaUtils.notifyChange(this, NyaaUtils.QUEUE_CHANGED);

        // @TODO for now update queue database in here (change to use message handling later)
        // updatePlaybackQueue(false, track);
        databaseHandler.post(new Runnable() {
            @Override
            public void run() {
                savePlaybackQueue();
            }
        });
        // savePlaybackQueue();

        return numCleared;
    }

    public boolean isPlaying() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isPlaying");

        try {
            return musicPlayer.isPlaying();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called isPlaying in illegal state");
        }

        return false;
    }

    // @TODO make private since not exposed function
    public void seekTo(int msec) {
        if (BuildConfig.DEBUG) Log.d(TAG, "seekTo");

        try {
            musicPlayer.seekTo(msec);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called seekTo in illegal state");
        }
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
    public int getCurrentQueuePos() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentQueuePos");

        return musicPlaybackState.getQueuePos();
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

    private Cursor makeMusicNameCursor(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicNameCursor");

        ContentResolver musicResolver = getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media.TITLE };
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { String.valueOf(musicId) };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        return musicResolver.query(musicUri, projection, selection, selectionArgs, sortOrder);
    }

    private void setupMediaSession() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupMediaSession");

        mediaSession = new MediaSession(this, TAG);

        MediaSession.Callback mediaSessionCallback = new MediaSession.Callback() {
            @Override
            public void onPlay() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onPlay");

                play();
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
            public void onSkipToNext() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onSkipToNext");

                next();
            }

            @Override
            public void onSkipToPrevious() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onSkipToPrevious");

                previous();
            }

            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onMediaButtonEvent");

                final KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (BuildConfig.DEBUG) Log.d(TAG, event.toString());

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

        mediaController = mediaSession.getController();
        updateMediaSession("NONE");
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

        Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        serviceIntent.putExtra(ACTION_EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

        // @TODO apparently deprecated. fix later
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        Notification.Action playPauseAction = new Notification.Action.Builder(android.R.drawable.ic_media_pause,
                getText(R.string.service_musicplayback_notification_pause_message), servicePendingIntent)
                .build();

        musicNotification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                .setContentText(getText(R.string.service_musicplayback_notification_message))
                .setSmallIcon(android.R.drawable.star_on)
                .setContentIntent(activityPendingIntent)
                .addAction(playPauseAction)
                .build();
    }

    private void setupReceivers() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupReceivers");

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        noisyAudioReceiver = new NoisyAudioReceiver(this);
        registerReceiver(noisyAudioReceiver, filter);
    }

    private void unregisterReceivers() {
        if (BuildConfig.DEBUG) Log.d(TAG, "unregisterReceivers");

        unregisterReceiver(noisyAudioReceiver);
    }

    // @TODO update later to make this function only handle KEYCODES while having a generic function
    // @TODO to delegate which function to which intent
    private void handleCommand(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "handleCommand");

        final int keyCode = intent.getIntExtra(ACTION_EXTRA_KEYCODE, 0);

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                play();
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
                next();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                previous();
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown KeyCode provided: " + KeyEvent.keyCodeToString(keyCode));
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

        int state = playbackState.getState();
        if (state == PlaybackState.STATE_PLAYING) {
            pause();
        } else if (state == PlaybackState.STATE_PAUSED ||
                state == PlaybackState.STATE_STOPPED) {
            play();
        }
    }

    private void scheduleDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "scheduleDelayedShutdown");

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + SHUTDOWN_DELAY_TIME, shutdownPendingIntent);
    }

    private void cancelDelayedShutdown() {
        if (BuildConfig.DEBUG) Log.d(TAG, "cancelDelayedShutdown");

        alarmManager.cancel(shutdownPendingIntent);
    }

    private void updateMediaSession(String state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMediaSession");

        // @TODO debugging
        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No play state found");
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "State before: " + String.valueOf(playbackState.getState()));
        }

        long playbackStateActions = PlaybackState.ACTION_SKIP_TO_NEXT |
                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_PAUSE |
                PlaybackState.ACTION_STOP;

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(playbackStateActions);

        int pos = musicPlaybackState.getQueuePos();
        if (pos != UNKNOWN_POS) {
            stateBuilder.setActiveQueueItemId(pos);
        }

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
            case "NONE":
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

        PlaybackState newState = stateBuilder.build();
        mediaSession.setPlaybackState(newState);

        // @TODO debugging
        playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No play state found");
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "State after: " + String.valueOf(playbackState.getState()));
        }
    }

    private Notification buildNotification() {
        if (BuildConfig.DEBUG) Log.d(TAG, "buildNotification");

        // find the location from the MediaStore
        Cursor cursor = makeMusicNameCursor(musicQueue.get(musicPlaybackState.getQueuePos()).getId());

        if (cursor == null) {
            return musicNotification;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found " + cursor.getCount() + " item(s)");
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Item: " + cursor.getString(dataColumn));
            }

            cursor.close();
            return musicNotification;
        }

        // success
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        cursor.close();

        Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        serviceIntent.putExtra(ACTION_EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

        // @TODO apparently deprecated. fix later
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        Notification.Action playPauseAction = new Notification.Action.Builder(android.R.drawable.ic_media_pause,
                getText(R.string.service_musicplayback_notification_pause_message), servicePendingIntent)
                .build();

        return new Notification.Builder(this)
                .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                .setContentText(name)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentIntent(activityPendingIntent)
                .addAction(playPauseAction)
                .build();
    }

    private void savePlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "savePlaybackState");

        // update the current playback seek position
        musicPlaybackState.setSeekPos(getCurrentPosition());
        PreferenceUtils.saveCurPlaying(this, musicPlaybackState);
    }

    private void loadPlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadPlaybackState");

        MusicPlaybackState state = PreferenceUtils.loadCurPlaying(this);
        int pos = state.getQueuePos();

        musicPlaybackState.setQueuePos(pos);
        musicPlaybackState.setSeekPos(state.getSeekPos());

        // die if load failed, probably due to out of bounds array position
        if (!(load(pos))) {
            return;
        }

        seekTo(state.getSeekPos());
    }

    private void loadPlaybackQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadPlaybackQueue");

        Cursor cursor = getContentResolver().query(MusicDatabaseProvider.QUEUE_CONTENT_URI, null, null, null,
                PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION + " ASC");

        if (cursor == null) {
            return;
        }

        int idColumn = cursor.getColumnIndex(PlaybackQueueSQLHelper.PlaybackQueueColumns.ID);
        int positionColumn = cursor.getColumnIndex(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            final long id = cursor.getLong(idColumn);
            final int position = cursor.getInt(positionColumn);

            // @TODO debugging
            if (BuildConfig.DEBUG) Log.d(TAG, "id: " + String.valueOf(id));
            if (BuildConfig.DEBUG) Log.d(TAG, "position: " + String.valueOf(position));


            MusicPlaybackTrack track = new MusicPlaybackTrack(id);
            musicQueue.add(track);
        }

        cursor.close();
    }

    private void savePlaybackQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "savePlaybackQueue");

        ContentResolver resolver = getContentResolver();
        int deleted = resolver.delete(MusicDatabaseProvider.QUEUE_CONTENT_URI, null, null);
        if (BuildConfig.DEBUG) Log.d(TAG, "Number of rows deleted: " + String.valueOf(deleted));

        int queueSize = musicQueue.size();
        ContentValues values[] = new ContentValues[queueSize];
        // making sure to save the position to restore for later
        for (int i = 0; i < queueSize; i++) {
            MusicPlaybackTrack track = musicQueue.get(i);
            ContentValues value = new ContentValues();
            value.put(PlaybackQueueSQLHelper.PlaybackQueueColumns.ID, track.getId());
            value.put(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION, i);

            values[i] = value;
        }

        int inserted = resolver.bulkInsert(MusicDatabaseProvider.QUEUE_CONTENT_URI, values);
        if (BuildConfig.DEBUG) Log.d(TAG, "Number of rows inserted: " + String.valueOf(inserted));
    }

    /**
     * Updates the required table fields for playback queue
     * @param type True for insert, False for deletion
     * @param track Item of interest
     */
    private void updatePlaybackQueue(boolean type, MusicPlaybackTrack track) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePlaybackQueue");

        ContentResolver resolver = getContentResolver();
        ContentValues value;
        int trackIndex = musicQueue.indexOf(track);

        if (type) {
            value = new ContentValues();
            value.put(PlaybackQueueSQLHelper.PlaybackQueueColumns.ID, track.getId());
            value.put(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION, trackIndex);

            Uri uri = resolver.insert(MusicDatabaseProvider.QUEUE_CONTENT_URI, value);

            if (BuildConfig.DEBUG) Log.d(TAG, "Inserted into: " + uri);
        } else {
            int queueSize = musicQueue.size();
            String[] args;
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            // first delete the track from the table
            args = new String[] { String.valueOf(trackIndex) };

            operations.add(ContentProviderOperation.newDelete(MusicDatabaseProvider.QUEUE_CONTENT_URI)
                    .withSelection(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION + "=?", args)
                    .build());

            // then update the rest of the tracks where position > trackPosition
            for (int i = trackIndex; i < queueSize; i++) {
                args = new String[] { String.valueOf(i+1) };
                value = new ContentValues();
                value.put(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION, i);

                operations.add(ContentProviderOperation.newUpdate(MusicDatabaseProvider.QUEUE_CONTENT_URI)
                        .withValues(value)
                        .withSelection(PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION + "=?", args)
                        .build());
            }

            try {
                ContentProviderResult[] results = resolver.applyBatch(MusicDatabaseProvider.URI_AUTHORITY, operations);
                for (ContentProviderResult result : results) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Result: " + result.toString());
                }
            } catch (RemoteException e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "RemoteException: " + e);
            } catch (OperationApplicationException e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "OperationApplicationException: " + e);
            }
        }
    }

    private void updatePlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePlaybackState");

        int queueSize = musicQueue.size();
        int queuePos = musicPlaybackState.getQueuePos();

        // if no items in queue: set unknown queue position
        // else if position is >= than size of queue: set queue position to 0
        if (queueSize == 0) {
            musicPlaybackState.setQueuePos(UNKNOWN_POS);
        } else if (queuePos >= queueSize) {
            musicPlaybackState.setQueuePos(0);
            load(0);
        } else {
            load(queuePos);
        }
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
                play();
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

                setVolume(0.4f, 0.4f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED");
                break;
        }
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    private static class NoisyAudioReceiver extends BroadcastReceiver {
        private static final String TAG = NoisyAudioReceiver.class.getSimpleName();

        private final WeakReference<MusicPlaybackService> musicPlaybackService;

        private NoisyAudioReceiver(MusicPlaybackService service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            musicPlaybackService = new WeakReference<>(service);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            if (BuildConfig.DEBUG) Log.d(TAG, "Action: " + action);

            musicPlaybackService.get().pause();
        }
    }

    private static class DatabaseHandler extends Handler {
        private static final String TAG = DatabaseHandler.class.getSimpleName();

        private final WeakReference<MusicPlaybackService> musicPlaybackService;

        private DatabaseHandler(MusicPlaybackService service, Looper looper) {
            super(looper);
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            musicPlaybackService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage");

            switch (msg.what) {
                case UPDATE_QUEUE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "UPDATE_QUEUE obtained");
                default:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Unknown message code: " + String.valueOf(msg.what));
            }
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
        public boolean load(int queuePos) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "load");

            return musicPlaybackService.get().load(queuePos);
        }

        @Override
        public void play() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "start");

            musicPlaybackService.get().play();
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

        @Override
        public void next() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "next");

            musicPlaybackService.get().next();
        }

        @Override
        public void previous() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "previous");

            musicPlaybackService.get().previous();
        }

        @Override
        public MusicPlaybackState getState() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getState");

            return musicPlaybackService.get().getState();
        }

        @Override
        public List<MusicPlaybackTrack> getQueue() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getQueue");

            return musicPlaybackService.get().getQueue();
        }

        @Override
        public int addToQueue(long musicId) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "addToQueue");

            return musicPlaybackService.get().addToQueue(musicId);
        }

        @Override
        public long removeFromQueue(int pos) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "removeFromQueue");

            return musicPlaybackService.get().removeFromQueue(pos);
        }

        @Override
        public int clearQueue() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "clearQueue");

            return musicPlaybackService.get().clearQueue();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "isPlaying");

            return musicPlaybackService.get().isPlaying();
        }
    }
}
