package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.Context;
import android.support.annotation.StyleRes;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * Utilities for changing the app theme
 */

public class ThemeUtils {
    private static final String TAG = ThemeUtils.class.getSimpleName();

    public ThemeUtils() {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }

    @StyleRes
    public static int getTheme(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getTheme");

        String prefStyle = PreferenceUtils.loadStyleRes(context);

        if (prefStyle.equals(context.getString(R.string.preference_theme_light_value))) {
            return R.style.AppTheme_Light;
        } else if (prefStyle.equals(context.getString(R.string.preference_theme_dark_value))) {
            return R.style.AppTheme_Dark;
        }

        // default to light theme
        return R.style.AppTheme_Light;
    }
}
