package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
}
