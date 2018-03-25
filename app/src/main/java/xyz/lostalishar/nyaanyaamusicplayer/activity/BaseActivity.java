package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialcab.MaterialCab;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.ThemeUtils;

public abstract class BaseActivity extends AppCompatActivity implements CabHolder {
    private static final String TAG = BaseActivity.class.getSimpleName();

    // flag for service binding
    private boolean bound = false;

    protected MaterialCab cab;

    private ServiceListener serviceListener;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setTheme(ThemeUtils.getTheme(this));
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // check if app has all the necessary permissions
        if (NyaaUtils.needsPermissions(this)) {
            NyaaUtils.requestMissingPermissions(this);
        }

        // initialise variables for broadcast receiving from the service
        IntentFilter filter = new IntentFilter();
        filter.addAction(NyaaUtils.SERVICE_EXIT);
        serviceListener = new ServiceListener(this);

        // register receiver here since the service can exit during background play
        registerReceiver(serviceListener, filter);
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

        // set theme if changed
        //recreate();
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

        terminate();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();

        unregisterReceiver(serviceListener);
    }


    //=========================================================================
    // Other activity callbacks
    //=========================================================================

    @Override
    public void onBackPressed() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBackPressed");

        if (cab != null && cab.isActive()) {
            cab.finish();
        } else {
            super.onBackPressed();
        }
    }


    //=========================================================================
    // CabHolder callback
    //=========================================================================

    @Override
    public MaterialCab openCab(MaterialCab.Callback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCab");

        if (cab != null && cab.isActive()) {
            cab.finish();
        }

        cab = new MaterialCab(this, R.id.cab_stub)
                .start(callback);

        return cab;
    }

    @Override
    public void closeCab() {
        if (BuildConfig.DEBUG) Log.d(TAG, "closeCab");

        if (cab != null && cab.isActive()) {
            cab.finish();
        }
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

        MusicUtils.startService(this);
        bound = MusicUtils.bindToService(this);
    }

    // terminate code
    protected void terminate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "terminate");

        if (bound) {
            MusicUtils.unbindFromService(this);
            bound = false;
        }
    }

    // Displays a dialog fragment on top of activity
    protected void setDialogFragment(DialogFragment dialog) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDialogFragment");

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);

        dialog.show(ft, null);
    }


    //=========================================================================
    // Internal classes
    //=========================================================================

    private static final class ServiceListener extends BroadcastReceiver {
        private static final String TAG = ServiceListener.class.getSimpleName();

        private WeakReference<BaseActivity> reference;

        public ServiceListener(BaseActivity baseActivity) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            reference = new WeakReference<>(baseActivity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {
                case NyaaUtils.SERVICE_EXIT:
                    reference.get().bound = false;
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.e(TAG, "Unknown action: " + action);
            }
        }
    }
}
