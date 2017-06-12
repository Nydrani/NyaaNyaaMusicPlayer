package xyz.velvetmilk.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.adapter.MusicAdapter;
import xyz.velvetmilk.nyaanyaamusicplayer.loader.MusicListLoader;
import xyz.velvetmilk.nyaanyaamusicplayer.model.Music;

/**
 * Created by nydrani on 28/05/17.
 */
public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = MusicListFragment.class.getSimpleName();

    private RecyclerView.LayoutManager layout;

    private MusicAdapter adapter;


    public static MusicListFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MusicListFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        List<Music> musicList = new ArrayList<>();
        adapter = new MusicAdapter(musicList);
        layout = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.list_base, container, false);
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.list_base);
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }


    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public Loader<List<Music>> onCreateLoader (final int id, final Bundle args) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateLoader");

        Activity activity = getActivity();

        return new MusicListLoader(activity);
    }

    @Override
    public void onLoadFinished(final Loader<List<Music>> loader, final List<Music> data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadFinished");

        adapter.swap(data);
    }

    @Override
    public void onLoaderReset(final Loader<List<Music>> loader) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadReset");

        adapter.swap(null);
    }
}
