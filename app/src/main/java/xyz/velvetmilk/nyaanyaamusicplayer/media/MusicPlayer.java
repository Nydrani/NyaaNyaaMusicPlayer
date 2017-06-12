package xyz.velvetmilk.nyaanyaamusicplayer.media;

import android.media.MediaPlayer;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Created by nydrani on 12/06/17.
 */

public class MusicPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {
    private static final String TAG = MusicPlayer.class.getSimpleName();
    private MusicPlaybackService musicPlaybackService;
    private MediaPlayer mediaPlayer;

    public MusicPlayer(MusicPlaybackService service) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicPlaybackService = service;
        initMediaPlayer();
    }
    // ========================================================================
    // MediaPlayer functions
    // ========================================================================

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }
    public void load(String source) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        try {
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Unable to load data source: " + source);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Called prepareAsync in illegal state");
        }
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        try {
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Called start in illegal state");
        }
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Called stop in illegal state");
        }
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        mediaPlayer.reset();
    }

    public void release() {
        if (BuildConfig.DEBUG) Log.d(TAG, "release");

        mediaPlayer.release();
    }


    // ========================================================================
    // MediaPlayer listener overrides
    // ========================================================================

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onError");

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                if (BuildConfig.DEBUG) Log.e(TAG, "MEDIA_ERROR_SERVER_DIED");
                mediaPlayer.release();
                initMediaPlayer();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED");
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPrepared");
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCompletion");
    }
}
