package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.MusicAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.MusicListLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing entire list of music on device
 */
public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = MusicListFragment.class.getSimpleName();

    private RecyclerView.LayoutManager layout;
    private MusicAdapter adapter;
    private IntentFilter filter;
    private ListRefreshListener listRefreshListener;


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

        Activity activity = getActivity();
        List<Music> musicList = new ArrayList<>();

        adapter = new MusicAdapter(musicList);
        layout = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        filter = new IntentFilter(NyaaUtils.REFRESH);
        listRefreshListener = new ListRefreshListener(this);
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        Activity activity = getActivity();
        activity.registerReceiver(listRefreshListener, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        Activity activity = getActivity();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);
        View rootView = inflater.inflate(R.layout.list_base, container, false);
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.list_base_view);

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layout);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(listRefreshListener);
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

    @Override
    public void onLoadFinished(Loader<List<Music>> loader, List<Music> data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadFinished");

        adapter.swap(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Music>> loader) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLoadReset");

        adapter.swap(null);
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void refreshList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshList");

        getLoaderManager().restartLoader(0, null, this);
    }


    //=========================================================================
    // Update queue UI on change listener
    //=========================================================================

    private static final class ListRefreshListener extends BroadcastReceiver {
        private static final String TAG = MusicListFragment.ListRefreshListener.class.getSimpleName();

        private WeakReference<MusicListFragment> reference;

        public ListRefreshListener(MusicListFragment musicListFragment) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            reference = new WeakReference<>(musicListFragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            if (action.equals(NyaaUtils.REFRESH)) {
                reference.get().refreshList();
            }
        }
    }
}
