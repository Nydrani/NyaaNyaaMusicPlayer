package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * BaseFragment containing list of music provided by adapter
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();

    public ActionMode actionMode;


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    // ========================================================================
    // Helper functions
    // ========================================================================

    public void openCAB(View v, ActionMode.Callback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCAB");

        if (actionMode == null) {
            actionMode = v.startActionMode(callback);
        }
    }

    public void closeCAB() {
        if (BuildConfig.DEBUG) Log.d(TAG, "finishCAB");

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public boolean isCABOpen() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCABOpen");

        return actionMode != null;
    }
}
