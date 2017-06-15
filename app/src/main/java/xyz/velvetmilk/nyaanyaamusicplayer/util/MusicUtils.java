package xyz.velvetmilk.nyaanyaamusicplayer.util;

import android.util.Log;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Created by nydrani on 12/06/17.
 * Music service utilities
 */

public class MusicUtils {
    private static final String TAG = MusicUtils.class.getSimpleName();
    private static MusicPlaybackService musicPlaybackService;


    public MusicUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }

    public static void setMusicPlaybackService(MusicPlaybackService service) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setMusicPlaybackService");

        musicPlaybackService = service;
    }

    public static void play(long songId) {
        if (BuildConfig.DEBUG) Log.d(TAG, "play");

        if (musicPlaybackService == null) {
            return;
        }

        //musicPlaybackService.reset();
        musicPlaybackService.load(songId);
        //musicPlaybackService.start();
    }

    public static void stop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stop");

        if (musicPlaybackService == null) {
            return;
        }

        musicPlaybackService.stop();

    }
}
