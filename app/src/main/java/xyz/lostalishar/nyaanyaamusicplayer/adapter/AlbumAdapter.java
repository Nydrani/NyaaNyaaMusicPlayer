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
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.AlbumViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Album;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class AlbumAdapter extends BaseAdapter<AlbumViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private List<Album> albumList;

    public AlbumAdapter(List<Album> albumList, BaseFragment fragment) {
        super(fragment);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.albumList = albumList;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_album, parent, false);

        return new AlbumViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Album album = getAlbumList().get(position);

        holder.albumTitle.setText(album.getName());
        holder.numSongs.setText(String.valueOf(album.getNumSongs()));

        // store id
        holder.albumDataHolder.albumId = album.getId();
    }

    @Override
    public long getItemId(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemId");

        return albumList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemCount");

        return albumList.size();
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


    // ========================================================================
    // Exposed functions
    // ========================================================================

    public List<Album> getAlbumList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getAlbumList");

        return albumList;
    }


    // ========================================================================
    // Useful cursor functions
    // ========================================================================

    public void swap(List<Album> newList){
        if (BuildConfig.DEBUG) Log.d(TAG, "swap");

        albumList.clear();
        if (newList != null) {
            albumList.addAll(newList);
        }
        notifyDataSetChanged();
    }
}
