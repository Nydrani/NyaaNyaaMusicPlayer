package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Utilities for SharedPreferences for various preferences in the app
 */

public class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getSimpleName();
    private static final String SETTINGS_PREFERENCES = "Settings";
    private static final String SERVICE_PREFERENCES = "Service";

    private static final String SERVICE_QUEUE_PLAYING_POS = "cur_queue_music_pos";
    private static final String SERVICE_QUEUE_PLAYING_SEEKPOS = "cur_queue_music_seekpos";

    public PreferenceUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    //=========================================================================
    // Exposed functions
    //=========================================================================

    public static void saveCurPlaying(Context context, MusicPlaybackState state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "saveCurPlaying");

        SharedPreferences.Editor editor = context.getSharedPreferences(SERVICE_PREFERENCES, Context.MODE_PRIVATE)
                .edit();

        editor.putInt(SERVICE_QUEUE_PLAYING_POS, state.getQueuePos());
        editor.putInt(SERVICE_QUEUE_PLAYING_SEEKPOS, state.getSeekPos());

        editor.apply();
    }

    public static MusicPlaybackState loadCurPlaying(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadCurPlaying");

        SharedPreferences preferences = context.getSharedPreferences(SERVICE_PREFERENCES, Context.MODE_PRIVATE);
        MusicPlaybackState state = new MusicPlaybackState();

        try {
            int queuePos = preferences.getInt(SERVICE_QUEUE_PLAYING_POS, MusicPlaybackService.UNKNOWN_POS);
            int seekPos = preferences.getInt(SERVICE_QUEUE_PLAYING_SEEKPOS, 0);
            state.setQueuePos(queuePos);
            state.setSeekPos(seekPos);
        } catch (ClassCastException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Incorrect type found for preference");
        }

        return state;
    }

}
