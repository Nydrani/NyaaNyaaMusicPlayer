package xyz.lostalishar.nyaanyaamusicplayer.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.QueueAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.loader.MusicQueueLoader;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * Fragment containing the current play queue
 */

public class MusicQueueFragment extends BaseFragment {
    private static final String TAG = MusicQueueFragment.class.getSimpleName();

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

        List<Music> queueList = new ArrayList<>();

        adapter = new QueueAdapter(queueList);

        filter = new IntentFilter(NyaaUtils.QUEUE_CHANGED);
        filter.addAction(NyaaUtils.META_CHANGED);
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


    //=========================================================================
    // Helper functions
    //=========================================================================

    private void refreshQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshQueue");

        getLoaderManager().restartLoader(0, null, this);
    }

    private void updateMetaUI() {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateMetaUI");

        adapter.notifyDataSetChanged();
    }

    private void clearQueue() {
        if (BuildConfig.DEBUG) Log.d(TAG, "clearQueue");

        List<Music> musicList = adapter.getMusicList();
        int musicListSize = musicList.size();

        int[] posArray = new int[musicListSize];
        for (int i = 0; i < musicListSize; i++) {
            posArray[i] = i;
        }

        int numCleared = MusicUtils.dequeue(posArray, null);
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

            if (action.equals(NyaaUtils.QUEUE_CHANGED)) {
                reference.get().refreshQueue();
            } else if (action.equals(NyaaUtils.META_CHANGED)) {
                reference.get().updateMetaUI();
            }
        }
    }
}
