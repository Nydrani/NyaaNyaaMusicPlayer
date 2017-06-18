package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;

/**
 * Utilities for SharedPreferences for various preferences in the app
 */

public class PreferenceUtils {
    private static final String TAG = MusicUtils.class.getSimpleName();
    private static final String SETTINGS_PREFERENCES = "Settings";
    private static final String SERVICE_PREFERENCES = "Service";

    private static final String SERVICE_PLAYING_ID = "cur_music_id";
    private static final String SERVICE_PLAYING_SEEKPOS = "cur_music_pos";

    public PreferenceUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }

    public static void saveCurPlaying(Context context, MusicPlaybackState state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setCurPlaying");

        SharedPreferences.Editor editor = context.getSharedPreferences(SERVICE_PREFERENCES, Context.MODE_PRIVATE)
                .edit();

        editor.putLong(SERVICE_PLAYING_ID, state.getMusicId());
        editor.putInt(SERVICE_PLAYING_SEEKPOS, state.getMusicPos());

        editor.apply();
    }

    public static MusicPlaybackState loadCurPlaying(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadCurPlaying");

        SharedPreferences preferences = context.getSharedPreferences(SERVICE_PREFERENCES, Context.MODE_PRIVATE);
        MusicPlaybackState state = new MusicPlaybackState();

        try {
            long musicId = preferences.getLong(SERVICE_PLAYING_ID, 0);
            int musicPos = preferences.getInt(SERVICE_PLAYING_SEEKPOS, 0);
            state.setMusicId(musicId);
            state.setMusicPos(musicPos);
        } catch (ClassCastException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Incorrect type found for preference");
        }

        return state;
    }

}
