package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    private static final String TAG = SettingsActivity.class.getSimpleName();


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.preference_title);
        }

        // load fragments
        if (savedInstanceState == null) {
            loadFragments();
        }
    }


    //=========================================================================
    // Options menu callbacks
    //=========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // OnPreferenceStartScreenCallback implementation
    //=========================================================================

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPreferenceStartScreen");

        String key = preferenceScreen.getKey();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);

        Fragment fragment = SettingsFragment.newInstance();
        fragment.setArguments(args);
        replaceBaseFragment(fragment, key);

        return true;
    }

    //=========================================================================
    // Helper functions
    //=========================================================================

    private void loadFragments() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadFragments");

        setBaseFragment(SettingsFragment.newInstance());
    }

    /*
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    private void setBaseFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setBaseFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getBaseFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.activity_base_content, fragment);
        }
        ft.commit();
    }

    /*
     * Replaces the fragment in the FrameLayout container and adds to back stack
     */
    private void replaceBaseFragment(Fragment fragment, String key) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setBaseFragment");

        FragmentManager fm = getSupportFragmentManager();
        Fragment element = getBaseFragment(fm);

        // check for "remove fragment" and null fragment in container
        if (fragment == null && element == null) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (fragment == null) {
            ft.remove(element);
        } else {
            ft.replace(R.id.activity_base_content, fragment, key);
        }
        ft.addToBackStack(key);
        ft.commit();
    }

    // Gets the current fragment being shown
    private Fragment getBaseFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getBaseFragment");

        return fm.findFragmentById(R.id.activity_base_content);
    }
}
