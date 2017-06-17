package xyz.velvetmilk.nyaanyaamusicplayer.service;

import android.app.AlarmManager;
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
import xyz.velvetmilk.nyaanyaamusicplayer.media.MusicPlayer;
import xyz.velvetmilk.nyaanyaamusicplayer.receiver.MediaButtonIntentReceiver;

public class MusicPlaybackService extends Service {
    private static final String TAG = MusicPlaybackService.class.getSimpleName();
    private IBinder binder;
    private MusicPlayer musicPlayer;
    private AlarmManager alarmManager;
    private PendingIntent shutdownPendingIntent;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private MediaController mediaController;

    // 5 minutes allowed to be inactive before death
    private static final int DELAY_TIME = 5 * 60 * 1000;
    private static final String ACTION_SHUTDOWN = "SHUTDOWN";


    // ========================================================================
    // Service lifecycle overrides
    // ========================================================================

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        binder = new NyaaNyaaMusicServiceStub(this);
        setupAlarms();

        setupMediaSession();
        mediaController = mediaSession.getController();

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        musicPlayer = new MusicPlayer(audioManager, mediaSession);
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

        // make sure to cancel alarmmanager since it still runs in the background
        cancelDelayedShutdown();

        // release managers
        audioManager.abandonAudioFocus(musicPlayer);
        mediaSession.release();

        // release music player
        if (musicPlayer != null) {
            musicPlayer.reset();
            musicPlayer.release();
        }
    }


    // ========================================================================
    // Exposed functions for clients
    // ========================================================================

    public void load(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        // make sure to reset before any loading
        musicPlayer.reset();

        // store song id
        musicPlayer.setMusicId(musicId);

        // find the location from the MediaStore
        Cursor cursor = makeMusicLocationCursor(musicId);

        if (cursor == null) {
            return;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Found more than 1 item");
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Item: " + cursor.getString(dataColumn));
            }

            cursor.close();
            return;
        }

        // success
        cursor.moveToFirst();
        String loc = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        musicPlayer.load(loc);
        cursor.close();
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        musicPlayer.start();
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        musicPlayer.pause();
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        musicPlayer.stop();
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

                musicPlayer.start();
            }

            @Override
            public void onPause() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onPause");

                musicPlayer.pause();
            }

            @Override
            public void onStop() {
                if (BuildConfig.DEBUG) Log.d(TAG, "onStop");

                musicPlayer.stop();
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
        public void load(long musicId) throws RemoteException {
            if (BuildConfig.DEBUG) Log.d(TAG, "load");

            musicPlaybackService.get().load(musicId);
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
    }
}
