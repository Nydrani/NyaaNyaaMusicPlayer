package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

public abstract class BaseActivity extends AppCompatActivity implements CabHolder {
    private static final String TAG = BaseActivity.class.getSimpleName();

    // flag for service binding
    private boolean bound = false;

    private ActionMode actionMode;


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

        // check if app has all the necessary permissions
        if (NyaaUtils.needsPermissions(this)) {
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
    // CabHolder callback
    //=========================================================================

    @Override
    public ActionMode openCab(ActionMode.Callback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCab");

        if (actionMode == null) {
            actionMode = startActionMode(callback);
        }

        return actionMode;
    }

    @Override
    public void closeCab() {
        if (BuildConfig.DEBUG) Log.d(TAG, "closeCab");

        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    @Override
    public boolean isCabOpen() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCabOpen");

        return actionMode != null;
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
}
