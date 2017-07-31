package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

/**
 * BaseFragment containing list of music provided by adapter
 */

public abstract class BaseFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = BaseFragment.class.getSimpleName();

    public BaseAdapter<? extends BaseMusicViewHolder> adapter;


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }


    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public void onLoadFinished(Loader<List<Music>> loader, List<Music> data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadFinished");

        adapter.finishCAB();
        adapter.swap(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Music>> loader) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadReset");

        adapter.finishCAB();
        adapter.swap(null);
    }
}
