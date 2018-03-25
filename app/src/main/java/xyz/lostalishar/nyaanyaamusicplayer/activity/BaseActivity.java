package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.ThemeUtils;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    // variable for theme caching
    private int theme;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        theme = ThemeUtils.getTheme(this);
        setTheme(theme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        // set theme if changed
        if (theme != ThemeUtils.getTheme(this)) {
            recreate();
        }
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // Displays a dialog fragment on top of activity
    protected void setDialogFragment(DialogFragment dialog) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDialogFragment");

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);

        dialog.show(ft, null);
    }
}
