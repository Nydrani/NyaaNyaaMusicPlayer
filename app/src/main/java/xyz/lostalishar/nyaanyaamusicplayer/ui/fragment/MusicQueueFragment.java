package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.QueueAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.MusicQueueLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing the current play queue
 */

public class MusicQueueFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<Music>> {
    private static final String TAG = MusicQueueFragment.class.getSimpleName();

    public QueueAdapter adapter;
    private RecyclerView.LayoutManager layout;
    private TextView pauseBox;

    private IntentFilter filter;
    private QueueUpdateListener queueUpdateListener;

    private OnViewInflatedListener viewInflatedListener;

    public static MusicQueueFragment newInstance() {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        return new MusicQueueFragment();
    }


    //=========================================================================
    // Fragment lifecycle
    //=========================================================================

    @Override
    public void onAttach(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onAttach");
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity = (Activity) context;

            try {
                viewInflatedListener = (OnViewInflatedListener) activity;
            } catch (ClassCastException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
                throw new ClassCastException(activity.toString() +
                        " must implement OnViewCreatedListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // no options menu on startup
        setHasOptionsMenu(false);

        List<Music> queueList = new ArrayList<>();

        adapter = new QueueAdapter(queueList, this);

        Activity activity = getActivity();
        layout = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);

        filter = new IntentFilter();
        filter.addAction(NyaaUtils.QUEUE_CHANGED);
        filter.addAction(NyaaUtils.META_CHANGED);
        filter.addAction(NyaaUtils.SERVICE_READY);
        queueUpdateListener = new QueueUpdateListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        Activity activity = getActivity();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);
        View rootView = inflater.inflate(R.layout.fragment_queue, container, false);
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.list_base_view);
        pauseBox = (TextView)rootView.findViewById(R.id.fragment_bottom_bar);

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layout);

        pauseBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

                MusicPlaybackState state = MusicUtils.getState();
                // do nothing on unknown state
                if (state == null) {
                    return;
                }

                if (MusicUtils.isPlaying()) {
                    MusicUtils.pause();
                } else if (state.getQueuePos() == MusicPlaybackService.UNKNOWN_POS) {
                    Toast.makeText(v.getContext(), R.string.toast_choose_track, Toast.LENGTH_SHORT).show();
                } else {
                    MusicUtils.resume();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        // notify listener
        if (viewInflatedListener != null) {
            viewInflatedListener.onViewInflated(view);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        Activity activity = getActivity();
        activity.registerReceiver(queueUpdateListener, filter);

        // update ui on resume
        updateMetaUI();
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(queueUpdateListener);
    }


    //=========================================================================
    // Options menu callbacks
    //=========================================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.queue, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_clear_queue:
                clearQueue();
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

        return new MusicQueueLoader(activity);
    }

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


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void refreshQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshQueue");

        getLoaderManager().restartLoader(0, null, this);
    }

    private void updatePauseBox() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updatePauseBox");

        if (MusicUtils.isPlaying()) {
            pauseBox.setText(getString(R.string.fragment_bottom_bar_pause));
        } else {
            pauseBox.setText(getString(R.string.fragment_bottom_bar_play));
        }
    }

    private void updateMetaUI() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMetaUI");

        updatePauseBox();
        adapter.notifyDataSetChanged();
    }

    private void clearQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "clearQueue");

        List<Music> musicList = adapter.getMusicList();
        int musicListSize = musicList.size();

        long[] musicIdArray = new long[musicListSize];
        for (int i = 0; i < musicListSize; i++) {
            musicIdArray[i] = musicList.get(i).getId();
        }

        int numCleared = MusicUtils.dequeue(musicIdArray, null);
        if (BuildConfig.DEBUG) Log.d(TAG, "Number dequeued: " + numCleared);

        String toastFormat = getResources().getString(R.string.toast_clear_x_tracks);
        String toastMessage = String.format(toastFormat, numCleared);
        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
    }


    //=========================================================================
    // Internal classes
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

            switch (action) {
                case NyaaUtils.QUEUE_CHANGED:
                    reference.get().refreshQueue();
                    break;
                case NyaaUtils.META_CHANGED:
                    reference.get().updateMetaUI();
                    break;
                case NyaaUtils.SERVICE_READY:
                    reference.get().refreshQueue();
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.e(TAG, "Unknown action: " + action);
            }
        }
    }

    public interface OnViewInflatedListener {
        void onViewInflated(View view);
    }
}
