package xyz.velvetmilk.nyaanyaamusicplayer.media;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
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
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MusicPlayer.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private AudioAttributes audioAttributes;
    private AudioManager audioManager;
    private MediaSession mediaSession;

    public MusicPlayer(AudioManager audioManager, MediaSession mediaSession) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.audioManager = audioManager;
        this.mediaSession = mediaSession;
        audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        initMediaPlayer();
    }


    // ========================================================================
    // MediaPlayer functions
    // ========================================================================

    private void initMediaPlayer() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initMediaPlayer");

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioAttributes(audioAttributes);
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

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        try {
            mediaPlayer.pause();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Called pause in illegal state");
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

        audioManager.abandonAudioFocus(this);
        mediaSession.setActive(false);

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
    public void onPrepared(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPrepared");

        int status = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            mp.stop();
            return;
        }

        mediaSession.setActive(true);
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCompletion");

        audioManager.abandonAudioFocus(this);
        mediaSession.setActive(false);
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
                mediaPlayer.setVolume(1.0f, 1.0f);
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
                mediaPlayer.setVolume(0.2f, 0.2f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED");
                break;
        }
    }
}
