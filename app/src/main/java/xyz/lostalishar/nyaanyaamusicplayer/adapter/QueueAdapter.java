package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.QueueViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class QueueAdapter extends BaseAdapter<QueueViewHolder> {
    private static final String TAG = QueueAdapter.class.getSimpleName();

    public QueueAdapter(List<Music> musicList, ActionMode actionMode) {
        super(musicList, actionMode);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_music, parent, false);

        return new QueueViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        super.onBindViewHolder(holder, position);

        // @TODO update background color if current position is playing
        // @TODO change to something with better UI design later lmao
        MusicPlaybackState state = MusicUtils.getState();
        if (state == null) {
            return;
        }

        if (state.getQueuePos() == position) {
            holder.itemView.setBackgroundColor(Color.RED);
        } else if (holder.itemView.getBackground() != null) {
            holder.itemView.setBackground(null);
        }
    }


    // ========================================================================
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateActionMode");

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_music_queue, menu);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onActionItemClicked");

        int id = item.getItemId();

        switch (id) {
            case R.id.actionbar_details:
                mode.finish();
                return true;
            case R.id.actionbar_remove:
                MusicUtils.dequeue(new long[] { chosenItem.getId() }, null);
                mode.finish();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
        }

        return false;
    }
}
