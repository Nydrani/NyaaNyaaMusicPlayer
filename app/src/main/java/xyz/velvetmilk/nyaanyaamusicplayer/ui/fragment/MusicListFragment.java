package xyz.velvetmilk.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.loader.MusicListLoader;
import xyz.velvetmilk.nyaanyaamusicplayer.model.Music;

/**
 * Created by nydrani on 28/05/17.
 */
public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = MusicListFragment.class.getSimpleName();

    private View rootView;
    private ListView listView;
    private ArrayAdapter<Music> adapter;


    public static MusicListFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        MusicListFragment fragment = new MusicListFragment();
        return fragment;
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        adapter = new ArrayAdapter<Music>(activity, android.R.layout.simple_list_item_1);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.list_base, container, false);
        listView = (ListView)rootView.findViewById(R.id.list_base);
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Music>> onCreateLoader (final int id, final Bundle args) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateLoader");

        Activity activity = getActivity();
        MusicListLoader loader = new MusicListLoader(activity);
        return loader;
    }


    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public void onLoadFinished(final Loader<List<Music>> loader, final List<Music> data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadFinished");

        if (data.isEmpty()) {
            adapter.clear();
            return;
        }

        adapter.addAll(data);
    }

    @Override
    public void onLoaderReset(final Loader<List<Music>> loader) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadReset");

        adapter.clear();
    }
}
