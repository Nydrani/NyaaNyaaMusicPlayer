package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public abstract class BaseAdapter<VH extends BaseMusicViewHolder> extends RecyclerView.Adapter<VH>
        implements ActionMode.Callback {
    private static final String TAG = BaseAdapter.class.getSimpleName();

    private List<Music> musicList;
    protected Music chosenItem;

    private ActionMode actionMode;

    protected BaseAdapter(List<Music> musicList, ActionMode actionMode) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.actionMode = actionMode;
        this.musicList = musicList;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Music music = musicList.get(position);

        holder.musicTitle.setText(music.getName());
        holder.musicDescription.setText(music.getArtistName());

        // store id
        holder.musicDataHolder.musicId = music.getId();
    }

    @Override
    public long getItemId(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemId");

        return musicList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemCount");

        return musicList.size();
    }


    // ========================================================================
    // Exposed functions
    // ========================================================================

    public List<Music> getMusicList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMusicList");

        return musicList;
    }


    // ========================================================================
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPrepareActionMode");

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyActionMode");

        actionMode = null;
    }


    // ========================================================================
    // Helper functions
    // ========================================================================

    public void openCAB(View v, Integer position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCAB");

        chosenItem = musicList.get(position);

        if (actionMode == null) {
            actionMode = v.startActionMode(this);
        }
    }

    public void finishCAB() {
        if (BuildConfig.DEBUG) Log.d(TAG, "finishCAB");

        if (actionMode != null) {
            actionMode.finish();
            chosenItem = null;
        }
    }

    public boolean isCABOpen() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCABOpen");

        return actionMode != null;
    }


    // ========================================================================
    // Useful cursor functions
    // ========================================================================

    public void swap(List<Music> newList){
        if (BuildConfig.DEBUG) Log.d(TAG, "swap");

        musicList.clear();
        if (newList != null) {
            musicList.addAll(newList);
        }
        notifyDataSetChanged();
    }
}
