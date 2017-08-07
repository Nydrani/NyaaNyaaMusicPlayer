package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

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
}
