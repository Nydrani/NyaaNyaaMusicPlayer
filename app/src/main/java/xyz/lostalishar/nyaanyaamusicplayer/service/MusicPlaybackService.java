package xyz.lostalishar.nyaanyaamusicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.media.AudioFocusRequest;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
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

/**
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

    private HandlerThread databaseThread;
    private Handler databaseHandler;

    private MediaSession mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private MediaController mediaController;

    private Notification musicNotification;
    private NotificationManager notificationManager;

    private MusicPlaybackState musicPlaybackState;
    private List<MusicPlaybackTrack> musicQueue;

    public static final String ACTION_SHUTDOWN = "SHUTDOWN";
    public static final String ACTION_EXTRA_KEYCODE = "KEYCODE";
    public static final String THREAD_DATABASE = "DatabaseThread";
    public static final String NOTIFICATION_ID = "MusicPlayerController";
    public static final String NOTIFICATION_NAME = "Remote Control";
    public static final int UNKNOWN_POS = -1;
    public static final long UNKNOWN_ID = -1;

    private static final int UPDATE_QUEUE = 0;
    private static final int NOTIFICATION_NUM = 1;

    private boolean transientPause = false;

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

        // setup audio manager
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // we need audio focus request for oreo+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build();
        }

        // restore playback queue
        loadPlaybackQueue();

        // clean playback queue
        cleanPlaybackQueue();

        // restore previous playback state
        loadPlaybackState();
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

        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onRebind");

        // permissions are needed
        if (NyaaUtils.needsPermissions(this)) {
            return;
        }

        cleanPlaybackQueue();
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
        // @TODO may be fixed since no more use of start_sticky
        if (intent == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "INTENT IS NULL (CHECK THIS OUT)");
            stopSelf();
            return START_NOT_STICKY;
        }

        final String action = intent.getAction();
        if (BuildConfig.DEBUG) Log.d(TAG, "Action: " + action + " called");

        if (ACTION_SHUTDOWN.equals(action)) {
            NyaaUtils.notifyChange(this, NyaaUtils.SERVICE_EXIT);
            stopSelf();
            return START_NOT_STICKY;
        }

        handleCommand(intent);

        return START_NOT_STICKY;
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
                //scheduleDelayedShutdown();
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

        savePlaybackState();
        savePlaybackQueue();

        // release music player
        if (musicPlayer != null) {
            musicPlayer.reset();
            musicPlayer.release();
        }

        // abandon audio since we no longer need
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }

        // delete notification channel on oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_ID);
        }

        // release media session
        mediaSession.release();

        // unregister receivers
        unregisterReceivers();

        // stop background threads
        databaseThread.quitSafely();

        // stop running as foreground service
        stopForeground(true);
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

            musicPlaybackState.setQueuePos(queuePos);
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);

            updateMediaSession(PlaybackState.STATE_PAUSED);
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

        int status;

        // check if my android version is oreo+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            status = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            status = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        if (status != AudioManager.AUDIOFOCUS_GAIN) {
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
            updateMediaSession(PlaybackState.STATE_PLAYING);
            mediaSession.setActive(true);
            startForeground(NOTIFICATION_NUM, buildNotification());
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
            savePlaybackState();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called start in illegal state");
        }
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        transientPause = false;

        try {
            musicPlayer.pause();

            updateMediaSession(PlaybackState.STATE_PAUSED);
            updateNotification();
            stopForeground(false);
            NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);
            savePlaybackState();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called pause in illegal state");
        }
    }

    public void next() {
        if (BuildConfig.DEBUG) Log.d(TAG, "next");

        int size = musicQueue.size();
        if (size == 0) {
            return;
        }

        int nextQueuePos = (musicPlaybackState.getQueuePos() + 1) % size;
        reset();
        load(nextQueuePos);

        updateNotification();
    }

    public void previous() {
        if (BuildConfig.DEBUG) Log.d(TAG, "previous");

        int size = musicQueue.size();
        if (size == 0) {
            return;
        }

        // apparently mod in java behaves as  { a % b = a - a / b * b }
        int prevQueuePos = ((musicPlaybackState.getQueuePos() - 1) % size + size) % size;
        reset();
        load(prevQueuePos);

        updateNotification();
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        musicPlayer.reset();

        updateMediaSession(PlaybackState.STATE_NONE);
        mediaSession.setActive(false);
        stopForeground(false);
        NyaaUtils.notifyChange(this, NyaaUtils.META_CHANGED);

        // abandon audio since we no longer need
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    public List<MusicPlaybackTrack> getQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getQueue");

        return musicQueue;
    }

    public MusicPlaybackState getState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getState");

        return musicPlaybackState;
    }

    public int enqueue(long[] musicIdList, int[] addedList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "enqueue");

        int addedCount = 0;
        boolean sendAddedList = addedList != null;

        for (int i = 0; i < musicIdList.length; i++) {
            MusicPlaybackTrack track = new MusicPlaybackTrack(musicIdList[i]);

            // check for items already in the queue
            if (musicQueue.contains(track)) {
                continue;
            }

            if (musicQueue.add(track)) {
                addedCount++;
                // update added list
                if (sendAddedList) {
                    addedList[i] = musicQueue.indexOf(track);
                }
            }

        }

        // on nothing removed, just return 0
        if (addedCount == 0) {
            return 0;
        }

        // update service
        NyaaUtils.notifyChange(this, NyaaUtils.QUEUE_CHANGED);

        // @TODO for now update queue database in here (change to use message handling later)
        //updatePlaybackQueue(true, track);
        databaseHandler.post(this::savePlaybackQueue);

        return addedCount;
    }

    public int dequeue(long[] musicIdList, long[] removedList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "dequeue");

        MusicPlaybackTrack currentPlayingTrack = getCurrentPlaying();
        long currentPlayingId = UNKNOWN_POS;
        int removedCount = 0;
        boolean sendRemovedList = removedList != null;

        // set the current playing
        if (currentPlayingTrack != null) {
            currentPlayingId = currentPlayingTrack.getId();
        }

        for (int i = 0; i < musicIdList.length; i++) {
            // reset music player only if chosen was currently playing
            if (musicIdList[i] == currentPlayingId) {
                reset();
            }

            MusicPlaybackTrack track = new MusicPlaybackTrack(musicIdList[i]);
            if (musicQueue.remove(track)) {
                removedCount++;
                // update removed list
                if (sendRemovedList) {
                    removedList[i] = musicIdList[i];
                }
            }
        }

        // on nothing removed, just return 0
        if (removedCount == 0) {
            return 0;
        }

        // update service
        updatePlaybackState();
        savePlaybackState();

        // notify people
        NyaaUtils.notifyChange(this, NyaaUtils.QUEUE_CHANGED);

        // @TODO for now update queue database in here (change to use message handling later)
        // updatePlaybackQueue(false, track);
        databaseHandler.post(this::savePlaybackQueue);

        return removedCount;
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

    public int getCurrentPosition() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPosition");

        try {
            return musicPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called getCurrentPosition in illegal state");
        }

        return 0;
    }

    public int getDuration() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getDuration");

        try {
            return musicPlayer.getDuration();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called getDuration in illegal state");
        }

        return 0;
    }

    public MusicPlaybackTrack getCurrentPlaying() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPlaying");

        // no track if unknown position
        if (musicPlaybackState.getQueuePos() == UNKNOWN_POS) {
            return null;
        }

        return musicQueue.get(musicPlaybackState.getQueuePos());
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

                reset();
            }

            @Override
            public void onSkipToNext() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onSkipToNext");

                boolean wasPlaying = isPlaying();
                next();
                if (wasPlaying) {
                    play();
                }
            }

            @Override
            public void onSkipToPrevious() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onSkipToPrevious");

                boolean wasPlaying = isPlaying();
                previous();
                if (wasPlaying) {
                    play();
                }
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

        // assumes all media sessions handle these flags after oreo+
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
                    | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        }

        mediaController = mediaSession.getController();
        updateMediaSession(PlaybackState.STATE_NONE);
    }

    private void setupNotification() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupNotification");

        Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        Intent deleteIntent = new Intent(this, MusicPlaybackService.class);
        deleteIntent.setAction(ACTION_SHUTDOWN);
        PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, 0);

        // need to do this for oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            musicNotification = new Notification.Builder(this, NOTIFICATION_ID)
                    .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                    .setContentText(getText(R.string.service_musicplayback_notification_message))
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentIntent(activityPendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .build();
        } else {
            musicNotification = new Notification.Builder(this)
                    .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                    .setContentText(getText(R.string.service_musicplayback_notification_message))
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentIntent(activityPendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .build();
        }

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // oreo needs a notification channel for some reason
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID,
                    NOTIFICATION_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
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
        boolean wasPlaying = isPlaying();

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
                reset();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                next();
                if (wasPlaying) {
                    play();
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (BuildConfig.DEBUG) Log.d(TAG, KeyEvent.keyCodeToString(keyCode));
                previous();
                if (wasPlaying) {
                    play();
                }
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

    private void updateMediaSession(int state) {
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
            case PlaybackState.STATE_PLAYING:
            case PlaybackState.STATE_PAUSED:
                stateBuilder.setState(state, musicPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case PlaybackState.STATE_NONE:
            case PlaybackState.STATE_ERROR:
                stateBuilder.setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN,
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

        int pos = musicPlaybackState.getQueuePos();

        // die if not playing
        if (pos == UNKNOWN_POS) {
            return musicNotification;
        }

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

        // construct notification intents
        Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Intent deleteIntent = new Intent(this, MusicPlaybackService.class);
        deleteIntent.setAction(ACTION_SHUTDOWN);
        PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, 0);

        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        serviceIntent.putExtra(ACTION_EXTRA_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);

        Notification.Action playPauseAction;
        if (isPlaying()) {
            // need to do this for marshmallow
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Icon icon = Icon.createWithResource(this, android.R.drawable.ic_media_pause);
                playPauseAction = new Notification.Action.Builder(icon,
                        getText(R.string.service_musicplayback_notification_pause_message), servicePendingIntent)
                        .build();
            } else {
                playPauseAction = new Notification.Action.Builder(
                        android.R.drawable.ic_media_pause,
                        getText(R.string.service_musicplayback_notification_pause_message), servicePendingIntent)
                        .build();
            }
        } else {
            // need to do this for marshmallow
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Icon icon = Icon.createWithResource(this, android.R.drawable.ic_media_play);
                playPauseAction = new Notification.Action.Builder(icon,
                        getText(R.string.service_musicplayback_notification_play_message), servicePendingIntent)
                        .build();
            } else {
                playPauseAction = new Notification.Action.Builder(
                        android.R.drawable.ic_media_play,
                        getText(R.string.service_musicplayback_notification_play_message), servicePendingIntent)
                        .build();
            }
        }

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, NOTIFICATION_ID)
                    .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                    .setContentText(name)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentIntent(activityPendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .addAction(playPauseAction)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle(getText(R.string.service_musicplayback_notification_title))
                    .setContentText(name)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentIntent(activityPendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .addAction(playPauseAction)
                    .build();
        }

        return notification;
    }

    private void updateNotification() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateNotification");

        notificationManager.notify(NOTIFICATION_NUM, buildNotification());
    }

    private void savePlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "savePlaybackState");

        // save the current playback seek position
        musicPlaybackState.setSeekPos(getCurrentPosition());
        PreferenceUtils.saveCurPlaying(this, musicPlaybackState);
    }

    private void loadPlaybackState() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadPlaybackState");

        MusicPlaybackState state = PreferenceUtils.loadCurPlaying(this);

        int pos = state.getQueuePos();
        int seekPos = state.getSeekPos();

        musicPlaybackState.setQueuePos(pos);
        musicPlaybackState.setSeekPos(seekPos);

        // die if load failed, probably due to out of bounds array position
        if (!(load(pos))) {
            return;
        }

        // seek to previous position
        seekTo(seekPos);
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

    // @TODO check for if the track even exists on the device
    private void loadPlaybackQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadPlaybackQueue");

        // query database
        Cursor cursor = getContentResolver().query(MusicDatabaseProvider.QUEUE_CONTENT_URI,
                null, null, null, PlaybackQueueSQLHelper.PlaybackQueueColumns.POSITION + " ASC");

        if (cursor == null) {
            return;
        }

        // restore playback queue from storage
        if (BuildConfig.DEBUG) Log.d(TAG, "Restoring queue");

        // setup the columns
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

    private void cleanPlaybackQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "cleanPlaybackQueue");

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media._ID };
        String selection = MediaStore.Audio.Media._ID + " IN (" + TextUtils.join(",", Collections.nCopies(musicQueue.size(), "?")) + ")";
        String args[] = new String[musicQueue.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = String.valueOf(musicQueue.get(i).getId());
        }
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        // query database
        Cursor cursor = getContentResolver().query(musicUri, projection, selection, args, sortOrder);

        if (cursor == null) {
            return;
        }

        // die if no difference
        if (cursor.getCount() == musicQueue.size()) {
            return;
        }


        // if discrepancies are found, wipe and reload the found ones
        if (BuildConfig.DEBUG) Log.d(TAG, "Cleaning queue");
        musicQueue.clear();

        // setup the columns
        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            final long id = cursor.getLong(idColumn);

            // @TODO debugging
            if (BuildConfig.DEBUG) Log.d(TAG, "id: " + String.valueOf(id));

            MusicPlaybackTrack track = new MusicPlaybackTrack(id);
            musicQueue.add(track);
        }
        cursor.close();

        // make sure to update the queue
        musicPlaybackState.setQueuePos(UNKNOWN_POS);
        musicPlaybackState.setSeekPos(0);
        savePlaybackState();
        savePlaybackQueue();
        
        // clearn notification
        notificationManager.cancel(NOTIFICATION_NUM);
    }

    // @TODO to find where the correct position of the queue should be, we need to find:
    // @TODO new position = old position - items removed above it, because everything cascades down
    // @TODO the question is, how do we find how many items were removed that were above the current playing
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
                if (transientPause) {
                    play();
                    transientPause = false;
                }
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

                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");

                boolean wasPlaying = isPlaying();
                pause();
                transientPause = wasPlaying;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (BuildConfig.DEBUG) Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                setVolume(0.4f, 0.4f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED (AUDIOFOCUS)");
                if (BuildConfig.DEBUG) Log.wtf(TAG, "CHANGE: " + String.valueOf(change));
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

    // @TODO might have to check for null on reference.get() if service dies
    public static class NyaaNyaaMusicServiceStub extends INyaaNyaaMusicService.Stub {
        private static final String TAG = NyaaNyaaMusicServiceStub.class.getSimpleName();

        private final WeakReference<MusicPlaybackService> serviceReference;

        private NyaaNyaaMusicServiceStub(MusicPlaybackService service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            serviceReference = new WeakReference<>(service);
        }


        public MusicPlaybackService getService() {
            if (BuildConfig.DEBUG) Log.d(TAG, "getService");

            return serviceReference.get();
        }

        @Override
        public boolean load(int queuePos) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "load");

            MusicPlaybackService service = serviceReference.get();

            return service != null && service.load(queuePos);
        }

        @Override
        public void play() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "start");

            MusicPlaybackService service = serviceReference.get();

            if (service != null) {
                service.play();
            }
        }

        @Override
        public void pause() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "pause");

            MusicPlaybackService service = serviceReference.get();

            if (service != null) {
                service.pause();
            }
        }

        @Override
        public void reset() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "reset");

            MusicPlaybackService service = serviceReference.get();

            if (service != null) {
                service.reset();
            }
        }

        @Override
        public void next() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "next");

            MusicPlaybackService service = serviceReference.get();

            if (service != null) {
                service.next();
            }
        }

        @Override
        public void previous() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "previous");

            MusicPlaybackService service = serviceReference.get();

            if (service != null) {
                service.previous();
            }
        }

        @Override
        public int getDuration() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getDuration");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.getDuration();
            }

            return UNKNOWN_POS;
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPosition");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.getCurrentPosition();
            }

            return UNKNOWN_POS;
        }

        @Override
        public MusicPlaybackState getState() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getState");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.getState();
            }

            return null;
        }

        @Override
        public List<MusicPlaybackTrack> getQueue() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getQueue");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.getQueue();
            }

            return new ArrayList<>();
        }

        @Override
        public MusicPlaybackTrack getCurrentPlaying() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPlaying");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.getCurrentPlaying();
            }

            return null;
        }

        @Override
        public int enqueue(long[] musicIdList, int[] addedList) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "enqueue");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.enqueue(musicIdList, addedList);
            }

            return UNKNOWN_POS;
        }

        @Override
        public int dequeue(long[] musicIdList, long[] removedList) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "dequeue");

            MusicPlaybackService service = serviceReference.get();
            if (service != null) {
                return service.dequeue(musicIdList, removedList);
            }

            return UNKNOWN_POS;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "isPlaying");

            MusicPlaybackService service = serviceReference.get();

            return service != null && service.isPlaying();
        }
    }
}
