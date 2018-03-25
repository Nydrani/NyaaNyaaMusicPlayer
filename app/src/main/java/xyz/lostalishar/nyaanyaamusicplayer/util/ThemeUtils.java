package xyz.lostalishar.nyaanyaamusicplayer.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.StyleRes;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;
import xyz.lostalishar.nyaanyaamusicplayer.service.INyaaNyaaMusicService;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

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

        if (BuildConfig.DEBUG) Log.v(TAG, "style pref = " + prefStyle);

        if (prefStyle.equals(context.getString(R.string.preference_theme_light_value))) {
            return R.style.AppTheme_Light;
        } else if (prefStyle.equals(context.getString(R.string.preference_theme_dark_value))) {
            return R.style.AppTheme_Dark;
        }

        // default to light theme
        return R.style.AppTheme_Light;
    }
}
