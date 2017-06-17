package xyz.velvetmilk.nyaanyaamusicplayer.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.util.Log;

import java.io.IOException;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.activity.BaseActivity;
import xyz.velvetmilk.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Created by nydrani on 12/06/17.
 * Custom music player wrapper around MediaPlayer
 */

public class MusicPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MusicPlayer.class.getSimpleName();

    private MusicPlaybackService service;
    private MediaPlayer mediaPlayer;
    private AudioAttributes audioAttributes;
    private MediaSession mediaSession;
    private MediaController mediaController;

    private AudioManager audioManager;
    private NotificationManager notificationManager;

    private Notification notification;

    private long musicId;

    public MusicPlayer(MusicPlaybackService service, MediaSession mediaSession) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.service = service;
        this.mediaSession = mediaSession;

        audioManager = (AudioManager)service.getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);

        audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mediaController = mediaSession.getController();

        musicId = 0;

        initMediaPlayer();
        initMediaSession();
        initNotification();
    }


    // ========================================================================
    // Exposed MediaPlayer functions
    // ========================================================================

    public void load(String source) {
        if (BuildConfig.DEBUG) Log.d(TAG, "load");

        try {
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Unable to load data source: " + source);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called prepareAsync in illegal state");
        }
    }

    public void start() {
        if (BuildConfig.DEBUG) Log.d(TAG, "start");

        service.startForeground(1, notification);
        mediaSession.setActive(true);
        updateMediaSession("PLAY");
        try {
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called start in illegal state");
        }
    }

    public void pause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "pause");

        service.stopForeground(true);
        updateMediaSession("PAUSE");
        try {
            mediaPlayer.pause();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called pause in illegal state");
        }
    }

    public void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        service.stopForeground(true);
        updateMediaSession("STOP");
        mediaSession.setActive(false);
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Called stop in illegal state");
        }
    }

    public void reset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "reset");

        service.stopForeground(true);
        audioManager.abandonAudioFocus(this);
        mediaSession.setActive(false);
        mediaPlayer.reset();
    }

    public void release() {
        if (BuildConfig.DEBUG) Log.d(TAG, "release");

        service.stopForeground(true);
        mediaPlayer.release();
    }

    public void setMusicId(long musicId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setMusicId");

        this.musicId = musicId;
    }


    // ========================================================================
    // MediaPlayer listener overrides
    // ========================================================================

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onError");

        service.stopForeground(true);
        audioManager.abandonAudioFocus(this);
        updateMediaSession("ERROR");
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
        updateMediaSession("PLAY");
        service.startForeground(1, notification);
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCompletion");

        service.stopForeground(true);
        audioManager.abandonAudioFocus(this);
        updateMediaSession("STOP");
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


    // ========================================================================
    // MusicPlayer helper functions
    // ========================================================================

    private void initMediaPlayer() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initMediaPlayer");

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioAttributes(audioAttributes);
    }

    private void initMediaSession() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initMediaSession");

        long playBackStateActions =
                PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_STOP;
        PlaybackState newState = new PlaybackState.Builder()
                .setActions(playBackStateActions)
                .setActiveQueueItemId(musicId)
                .setState(PlaybackState.STATE_NONE, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build();
        mediaSession.setPlaybackState(newState);
    }

    private void initNotification() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initNotification");

        Intent activityIntent = new Intent(service, BaseActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(service, 0, activityIntent, 0);

        notification = new Notification.Builder(service)
                .setContentTitle(service.getText(R.string.service_musicplayback_notification_title))
                .setContentText(service.getText(R.string.service_musicplayback_notification_message))
                .setSmallIcon(android.R.drawable.star_on)
                .setContentIntent(activityPendingIntent)
                .build();
    }

    private void updateMediaSession(String state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMediaSession");

        // @TODO debugging
        if (BuildConfig.DEBUG) Log.d(TAG, "State before: " );
        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, String.valueOf(playbackState.getState()));
        }

        long playBackStateActions =
                PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_STOP;

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(playBackStateActions)
                .setActiveQueueItemId(musicId);

        switch (state) {
            case "PLAY":
                stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "PAUSE":
                stateBuilder.setState(PlaybackState.STATE_PAUSED, mediaPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "STOP":
                stateBuilder.setState(PlaybackState.STATE_STOPPED, mediaPlayer.getCurrentPosition(),
                        1.0f);
                break;
            case "ERROR":
                stateBuilder.setState(PlaybackState.STATE_ERROR, PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                        1.0f);
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown state received");
                return;
        }

        // @TODO debugging
        PlaybackState newState = stateBuilder.build();
        mediaSession.setPlaybackState(newState);

        if (BuildConfig.DEBUG) Log.d(TAG, "State after: " );
        playbackState = mediaController.getPlaybackState();
        if (playbackState != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, String.valueOf(playbackState.getState()));
        }
    }
}
