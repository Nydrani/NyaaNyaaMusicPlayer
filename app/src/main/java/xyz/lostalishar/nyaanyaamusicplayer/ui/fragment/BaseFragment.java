package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;

/**
 * BaseFragment containing list of music provided by adapter
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();

    public CabHolder cabHolder;


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onAttach(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onAttach");
        super.onAttach(context);

        if (context instanceof Activity){
            Activity activity = (Activity)context;

            try {
                cabHolder = (CabHolder)activity;
            } catch (ClassCastException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
                throw new ClassCastException(activity.toString() +
                        " must implement CabHolder");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    // ========================================================================
    // Helper functions
    // ========================================================================

    /*
    public void openCAB(View v, ActionMode.Callback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCAB");

        if (actionMode == null) {
            actionMode = v.startActionMode(callback);
        }
    }

    public void closeCAB() {
        if (BuildConfig.DEBUG) Log.d(TAG, "finishCab");

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public boolean isCabOpen() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCabOpen");

        return actionMode != null;
    }
    */
}
