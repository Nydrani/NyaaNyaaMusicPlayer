package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * SettingsFragment for attaching to the activity
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final String KEY_PREF_DANK = "dank";

    public static SettingsFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new SettingsFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }


    //=========================================================================
    // PreferenceFragment implementation
    //=========================================================================

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreatePreferences");

        setPreferencesFromResource(R.xml.settings_layout, rootKey);
    }


    //=========================================================================
    // OnSharedPreferenceChangeListener implementation
    //=========================================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSharedPreferenceChanged");


        if (key.equals(KEY_PREF_DANK)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
