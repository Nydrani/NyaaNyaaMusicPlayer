package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Utilities for SharedPreferences for various preferences in the app
 */

public class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getSimpleName();
    private static final String SERVICE_PREFERENCES = "Service";
    private static final String STORAGE_PREFERENCES = "Storage";

    public static int KEY_PREF_ABOUT_VERSION_KEY = R.string.preference_about_version_key;
    public static int KEY_PREF_ABOUT_CATEGORY_KEY = R.string.preference_about_category_key;
    public static int KEY_PREF_THEME_KEY = R.string.preference_theme_key;
    public static int KEY_PREF_ANONYMOUS_DATA_KEY = R.string.preference_about_anonymous_data_key;

    public static final String SERVICE_QUEUE_PLAYING_POS = "cur_queue_music_pos";
    public static final String SERVICE_QUEUE_PLAYING_SEEKPOS = "cur_queue_music_seekpos";

    public static final String STORAGE_VIEW_PAGER_POSITION = "view_pager_position";

    public PreferenceUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    //=========================================================================
    // Exposed functions - Service Preferences
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
            state = new MusicPlaybackState();
        }

        return state;
    }


    //=========================================================================
    // Exposed functions - Storage Preferences
    //=========================================================================

    public static void saveCurViewPagerPosition(Context context, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "saveCurViewPagerPosition");

        SharedPreferences.Editor editor = context.getSharedPreferences(STORAGE_PREFERENCES, Context.MODE_PRIVATE)
                .edit();

        editor.putInt(STORAGE_VIEW_PAGER_POSITION, position);

        editor.apply();
    }

    public static int loadCurViewPagerPosition(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadCurViewPagerPosition");

        SharedPreferences preferences = context.getSharedPreferences(STORAGE_PREFERENCES, Context.MODE_PRIVATE);
        int position = 0;

        try {
            position = preferences.getInt(STORAGE_VIEW_PAGER_POSITION, 0);
        } catch (ClassCastException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Incorrect type found for preference");
        }

        return position;
    }


    //=========================================================================
    // Exposed functions - Default Preferences
    //=========================================================================

    public static String loadStyleRes(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadStyleRes");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String styleRes = "";

        try {
            styleRes = preferences.getString(context.getString(KEY_PREF_THEME_KEY), "");
        } catch (ClassCastException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Incorrect type found for preference");
        }

        return styleRes;
    }

    public static Boolean loadUsageDataPref(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadUsageDataPref");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean enabled = false;

        try {
            enabled = preferences.getBoolean(context.getString(KEY_PREF_ANONYMOUS_DATA_KEY), false);
        } catch (ClassCastException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Incorrect type found for preference");
        }

        return enabled;
    }
}
