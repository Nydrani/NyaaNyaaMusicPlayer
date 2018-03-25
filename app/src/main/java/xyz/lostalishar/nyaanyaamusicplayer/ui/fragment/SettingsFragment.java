package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

/**
 * SettingsFragment for attaching to the activity
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

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

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    //=========================================================================
    // PreferenceFragment implementation
    //=========================================================================

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreatePreferences");

        setPreferencesFromResource(R.xml.preferences, rootKey);

        // init the preference
        // @TODO prob better to move all this to private function
        Preference pref = findPreference(getString(PreferenceUtils.KEY_PREF_ABOUT_VERSION_KEY));
        if (pref != null) {
            pref.setSummary(BuildConfig.VERSION_NAME);
        }
        pref = findPreference(getString(PreferenceUtils.KEY_PREF_THEME_KEY));
        if (pref != null) {
            ListPreference listPreference = (ListPreference) pref;
            pref.setSummary(listPreference.getEntry());
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCallbackFragment");

        return this;
    }


    //=========================================================================
    // OnSharedPreferenceChangeListener implementation
    //=========================================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSharedPreferenceChanged");

        // might need null check
        Preference pref = findPreference(key);
        if (pref == null) {
            return;
        }

        if (key.equals(getString(PreferenceUtils.KEY_PREF_THEME_KEY))) {
            ListPreference listPreference = (ListPreference) pref;
            listPreference.setSummary(listPreference.getEntry());

            // recreate the activity to update the theme
            getActivity().recreate();

        } else if (key.equals(getString(PreferenceUtils.KEY_PREF_ANONYMOUS_DATA_KEY))) {
            SwitchPreference switchPreference = (SwitchPreference) pref;

            if (switchPreference.isChecked()) {
                switchPreference.setSummary(switchPreference.getSwitchTextOn());
            } else {
                switchPreference.setSummary(switchPreference.getSwitchTextOff());
            }
        }
    }
}
