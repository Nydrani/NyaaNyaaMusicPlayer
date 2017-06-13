package xyz.velvetmilk.nyaanyaamusicplayer.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Random;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.media.MusicPlayer;

public class MusicPlaybackService extends Service {
    private static final String TAG = MusicPlaybackService.class.getSimpleName();
    private IBinder binder;
    private Random rng;
    private MusicPlayer musicPlayer;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private MediaSession.Callback mediaSessionCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        binder = new MusicPlaybackBinder();
        rng = new Random();

        setupMediaSession();

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        musicPlayer = new MusicPlayer(audioManager, mediaSession);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBind");

        // @TODO Return the communication channel to the service.
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");

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

    // @TODO test function
    public int getRandomNumber() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getRandomNumber");

        return rng.nextInt(100);
    }


    public void load(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        // find the location from the MediaStore
        Cursor cursor = makeMusicLocationCursor(songId);

        if (cursor == null) {
            return;
        }

        // more than 1 item of this id --> debug me
        if (cursor.getCount() != 1) {
            if (BuildConfig.DEBUG) Log.d(TAG, "found more than 1 item");
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (BuildConfig.DEBUG) Log.d(TAG, "item: " + cursor.getString(dataColumn));
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

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        musicPlayer.stop();
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        musicPlayer.reset();
    }


    // ========================================================================
    // MusicPlaybackService helper functions
    // ========================================================================

    private Cursor makeMusicLocationCursor(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicLocationCursor");

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media.DATA };
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { String.valueOf(songId) };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


        return musicResolver.query(musicUri, projection, selection, selectionArgs, sortOrder);
    }

    private void setupMediaSession() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setupMediaSession");

        mediaSession = new MediaSession(this, TAG);
        mediaSessionCallback = new MediaSession.Callback() {
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
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onMediaButtonEvent");
                return false;
            }
        };

        mediaSession.setCallback(mediaSessionCallback);
    }


    // ========================================================================
    // Internal binder for service
    // ========================================================================

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    // @TODO fix copy pasta from android docs
    public class MusicPlaybackBinder extends Binder {
        private final String TAG = MusicPlaybackService.class.getSimpleName();

        public MusicPlaybackService getService() {
            if (BuildConfig.DEBUG) Log.d(TAG, "getService");

            // Return this instance of MusicPlaybackService so clients can call public methods
            return MusicPlaybackService.this;
        }
    }
}
