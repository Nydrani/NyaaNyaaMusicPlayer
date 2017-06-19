package xyz.lostalishar.nyaanyaamusicplayer.activity;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.ui.dialogfragment.AboutDialogFragment;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.MusicListFragment;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 0;

    // flag for service binding
    private boolean bound = false;


    //=========================================================================
    // Activity lifecycle
    //=========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (hasPermissions()) {
            init();
        }
    }

    @Override
    protected void onStart() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart");
        super.onStart();

        ComponentName name = MusicUtils.startService(this);
        if (BuildConfig.DEBUG) Log.d(TAG, name.toString());
        bound = MusicUtils.bindToService(this);
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

        if (bound) {
            MusicUtils.unbindFromService(this);
            bound = false;
        }
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
            case PERMISSION_REQUEST_CODE:
                if (checkPermissionGrantResults(grantResults)) {
                    init();
                } else {
                    finish();
                }
                break;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unhandled result code");
                break;
        }
    }


    //=========================================================================
    // Activity menu callbacks
    //=========================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_homelink:
                Snackbar.make(findViewById(android.R.id.content), "Replace with your own action", Snackbar.LENGTH_LONG)
                        .show();
                return true;

            case R.id.actionbar_settings:
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
                return true;

            case R.id.actionbar_about:
                setDialogFragment(AboutDialogFragment.newInstance());
                return true;

            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item");
                break;
        }
        return super.onOptionsItemSelected(item);

    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // startup code
    protected void init() {
        if (BuildConfig.DEBUG) Log.d(TAG, "init");

        setFragment(MusicListFragment.newInstance());
    }

    protected void setDialogFragment(DialogFragment dialog) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDialogFragment");

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);

        dialog.show(ft, null);
    }

    protected void setFragment(Fragment fragment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setFragment");

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, fragment);
        ft.commit();
    }

    /**
     * Checks if app has permissions and asks for permissions when it does not.
     */
    private boolean hasPermissions() {
        if (BuildConfig.DEBUG) Log.d(TAG, "hasPermissions");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        String[] permissions = new String[1];
        List<String> permissionList = new ArrayList<>();

        permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
        
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        String[] neededPermissions = new String[permissionList.size()];
        neededPermissions = permissionList.toArray(neededPermissions);

        // requests permissions on post android M versions
        if (neededPermissions.length > 0) {
            requestPermissions(neededPermissions, PERMISSION_REQUEST_CODE);
        }

        return neededPermissions.length == 0;
    }

    /**
     * Checks if the grant results from permission request succeeds
     */
    private boolean checkPermissionGrantResults(int[] grantResults) {
        if (BuildConfig.DEBUG) Log.d(TAG, "checkPermissionGrantResults");

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
