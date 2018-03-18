package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * SettingsFragment for attaching to the activity
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static SettingsFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new SettingsFragment();
    }


    //=========================================================================
    // PreferenceFragment implementation
    //=========================================================================

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_layout, rootKey);
    }
}
