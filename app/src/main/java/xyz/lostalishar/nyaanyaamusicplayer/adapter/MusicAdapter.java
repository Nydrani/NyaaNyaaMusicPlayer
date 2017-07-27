package xyz.lostalishar.nyaanyaamusicplayer.adapter;

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
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.MusicListViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class MusicAdapter extends BaseAdapter<MusicListViewHolder> {
    private static final String TAG = MusicAdapter.class.getSimpleName();

    public MusicAdapter(List<Music> musicList) {
        super(musicList);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public MusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_music, parent, false);

        return new MusicListViewHolder(v, this);
    }


    // ========================================================================
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateActionMode");

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_music_list, menu);

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
            case R.id.actionbar_about:
                mode.finish();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
        }

        return false;
    }
}
