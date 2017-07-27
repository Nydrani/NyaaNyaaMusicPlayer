package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.MusicAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.MusicListLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

/**
 * Fragment containing entire list of music on device
 */

public class MusicListFragment extends BaseFragment {
    private static final String TAG = MusicListFragment.class.getSimpleName();

    public static MusicListFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MusicListFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        List<Music> musicList = new ArrayList<>();

        adapter = new MusicAdapter(musicList);
    }


    //=========================================================================
    // Options menu callbacks
    //=========================================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_refresh:
                refreshList();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public Loader<List<Music>> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateLoader");

        Activity activity = getActivity();

        return new MusicListLoader(activity);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void refreshList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshList");

        getLoaderManager().restartLoader(0, null, this);
    }
}
