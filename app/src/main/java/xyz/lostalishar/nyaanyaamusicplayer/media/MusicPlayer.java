package xyz.lostalishar.nyaanyaamusicplayer.media;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Custom music player wrapper around MediaPlayer
 */

public class MusicPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    private static final String TAG = MusicPlayer.class.getSimpleName();

    private MediaPlayer mediaPlayer;

    private WeakReference<MusicPlaybackService> service;

    public MusicPlayer(MusicPlaybackService service) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.service = new WeakReference<>(service);

        initMediaPlayer();
    }


    // ========================================================================
    // Exposed MediaPlayer functions
    // ========================================================================

    public void load(String source) throws IOException, IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        mediaPlayer.setDataSource(source);
        mediaPlayer.prepare();
    }

    public void start() throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        // @TODO apparently this doesn't actually throw an IllegalStateException
        // @TODO as a result, it breaks the app. fml
        mediaPlayer.start();
    }

    public void pause() throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        mediaPlayer.pause();
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        mediaPlayer.reset();
    }

    public void release() {
        if (BuildConfig.DEBUG) Log.d(TAG, "release");

        mediaPlayer.release();
    }

    public void seekTo(int msec) throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "seekTo");

        mediaPlayer.seekTo(msec);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setVolume");

        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public int getCurrentPosition() throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentPosition");

        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "getDuration");

        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() throws IllegalStateException {
        if (BuildConfig.DEBUG) Log.d(TAG, "isPlaying");

        return mediaPlayer.isPlaying();
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
                mp.release();
                initMediaPlayer();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.wtf(TAG, "VERY BAD HAPPENED (MEDIA PLAYER ERROR)");
                if (BuildConfig.DEBUG) Log.wtf(TAG, "what: " + String.valueOf(what));

                // @TODO not sure what to do here --> fix later. for now just reset
                mp.release();
                initMediaPlayer();
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCompletion");

        MusicPlaybackService playbackService = service.get();
        playbackService.next();
        playbackService.play();
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
