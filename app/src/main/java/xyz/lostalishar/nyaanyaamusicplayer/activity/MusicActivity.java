package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

public abstract class MusicActivity extends BaseActivity {
    private static final String TAG = MusicActivity.class.getSimpleName();

    // flag for service binding
    private boolean bound = false;

    private ServiceListener serviceListener;


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


    //=========================================================================
    // Internal classes
    //=========================================================================

    private static final class ServiceListener extends BroadcastReceiver {
        private static final String TAG = ServiceListener.class.getSimpleName();

        private WeakReference<MusicActivity> reference;

        public ServiceListener(MusicActivity baseActivity) {
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
