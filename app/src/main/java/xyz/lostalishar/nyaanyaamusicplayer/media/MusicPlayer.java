package xyz.lostalishar.nyaanyaamusicplayer.media;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * Custom music player wrapper around MediaPlayer
 */

public class MusicPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    private static final String TAG = MusicPlayer.class.getSimpleName();

    private MediaPlayer mediaPlayer;

    public long musicId;

    public MusicPlayer() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicId = 0;

        initMediaPlayer();
    }


    // ========================================================================
    // Exposed MediaPlayer functions
    // ========================================================================

    public boolean load(String source) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        try {
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepare();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Unable to load data source: " + source);
            return false;
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called prepare in illegal state");
            return false;
        }

        return true;
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        try {
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called start in illegal state");
        }
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        try {
            mediaPlayer.pause();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called pause in illegal state");
        }
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called stop in illegal state");
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

    public void setVolume(float leftVolume, float rightVolume) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setVolume");

       mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setMusicId(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setMusicId");

        this.musicId = musicId;
    }

    public int getCurrentPosition() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPosition");

        return mediaPlayer.getCurrentPosition();
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
                release();
                initMediaPlayer();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED");
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCompletion");
    }


    // ========================================================================
    // MusicPlayer helper functions
    // ========================================================================

    private void initMediaPlayer() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initMediaPlayer");

        mediaPlayer = new MediaPlayer();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioAttributes(audioAttributes);
    }
}
