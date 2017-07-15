package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    // flag for service binding
    private boolean bound = false;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // check if app has all the necessary permissions
        if (NyaaUtils.needsPermissions(this)) {
            NyaaUtils.requestMissingPermissions(this);
        }
    }

    @Override
    protected void onStart() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart");
        super.onStart();

        // if i don't have permissions, don't start service
        if (NyaaUtils.needsPermissions(this)) {
            setFragment(null);
            return;
        }

        initialise();
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop");
        super.onStop();

        deinitialise();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    //=========================================================================
    // Activity request permissions callbacks
    //=========================================================================

    @Override
    public void onRequestPermissionsResult(int resultCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onRequestPermissionsResult");

        // don't parse if there was empty permissions + grant results due to multiple requests
        if (permissions.length == 0 && grantResults.length == 0) {
            return;
        }

        switch (resultCode) {
            case NyaaUtils.PERMISSION_REQUEST_CODE:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        // exit if permissions are denied
                        finish();
                        return;
                    }
                }

                // got all permissions
                initialise();
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unhandled result code");
                break;
        }
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // initialisation code
    protected void initialise() {
        if (BuildConfig.DEBUG) Log.d(TAG, "initialise");

        ComponentName name = MusicUtils.startService(this);
        bound = MusicUtils.bindToService(this);
    }

    // deinitialisation code
    protected void deinitialise() {
        if (BuildConfig.DEBUG) Log.d(TAG, "deinitialise");

        if (bound) {
            MusicUtils.unbindFromService(this);
            bound = false;
        }
    }

    // Displays a dialog fragment on top of activity
    protected void setDialogFragment(DialogFragment dialog) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDialogFragment");

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);

        dialog.show(ft, null);
    }

    /*
     * Replaces the fragment in the FrameLayout container
     * If fragment == null : remove "all" from fragment     <---- all is assuming only 1
     * If fragment != null : replace with new fragment
     */
    protected void setFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setFragment");

        FragmentManager fm = getFragmentManager();
        Fragment element = getFragment(fm);

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

    // Gets the current fragment being shown
    protected Fragment getFragment(FragmentManager fm) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getFragment");

        return fm.findFragmentById(R.id.activity_base_content);
    }
}
