package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
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
import xyz.lostalishar.nyaanyaamusicplayer.adapter.QueueAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.MusicQueueLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing the current play queue
 */

public class MusicQueueFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = MusicQueueFragment.class.getSimpleName();

    private RecyclerView.LayoutManager layout;
    private QueueAdapter adapter;
    private IntentFilter filter;
    private QueueUpdateListener queueUpdateListener;

    public static MusicQueueFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MusicQueueFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        List<Music> queueList = new ArrayList<>();
        adapter = new QueueAdapter(queueList);
        layout = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        filter = new IntentFilter(NyaaUtils.QUEUE_CHANGED);
        queueUpdateListener = new QueueUpdateListener(this);
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        Activity activity = getActivity();
        activity.registerReceiver(queueUpdateListener, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.list_base, container, false);
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.list_base_view);
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(adapter);

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
        activity.unregisterReceiver(queueUpdateListener);
    }



    //=========================================================================
    // LoaderManager.LoaderCallbacks
    //=========================================================================

    @Override
    public Loader<List<Music>> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateLoader");

        Activity activity = getActivity();

        return new MusicQueueLoader(activity);
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

    private void refreshQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshQueue");

        getLoaderManager().restartLoader(0, null, this);
    }


    //=========================================================================
    // Update queue UI on change listener
    //=========================================================================

    private static final class QueueUpdateListener extends BroadcastReceiver {
        private static final String TAG = QueueUpdateListener.class.getSimpleName();

        private WeakReference<MusicQueueFragment> reference;

        public QueueUpdateListener(MusicQueueFragment musicQueueFragment) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            reference = new WeakReference<>(musicQueueFragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onReceive");

            final String action = intent.getAction();

            if (action.equals(NyaaUtils.QUEUE_CHANGED)) {
                reference.get().refreshQueue();
            }
        }
    }
}
