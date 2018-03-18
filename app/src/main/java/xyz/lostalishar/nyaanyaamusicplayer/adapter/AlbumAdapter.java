package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.AlbumViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Album;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class AlbumAdapter extends BaseAdapter<AlbumViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private List<Album> albumList;

    public AlbumAdapter(List<Album> albumList, CabHolder cabHolder) {
        super(cabHolder);
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
    public @NonNull AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_album, parent, false);

        return new AlbumViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Album album = getAlbumList().get(position);

        String albumName = album.getName();

        int numTracks = album.getNumTracks();
        String tracksDescription = holder.itemView.getResources().
                getQuantityString(R.plurals.num_tracks, numTracks, numTracks);

        holder.albumTitle.setText(albumName);
        holder.numTracks.setText(tracksDescription);

        // store id
        holder.albumDataHolder.albumId = album.getId();
        holder.albumDataHolder.albumName = albumName;
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
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabCreated");

        materialCab.setMenu(R.menu.context_album_list);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabItemClicked");

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.actionbar_details:
                cab.finish();
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

    public void swap(@NonNull List<Album> newList){
        if (BuildConfig.DEBUG) Log.d(TAG, "swap");

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MusicListDiffCallback(albumList, newList));

        albumList.clear();
        albumList.addAll(newList);

        result.dispatchUpdatesTo(this);
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    private static class MusicListDiffCallback extends DiffUtil.Callback {
        private static final String TAG = MusicListDiffCallback.class.getSimpleName();

        private List<Album> oldList;
        private List<Album> newList;

        private MusicListDiffCallback(List<Album> oldList, List<Album> newList) {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            this.oldList = oldList;
            this.newList = newList;
        }


        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if (BuildConfig.DEBUG) Log.d(TAG, "areContentsTheSame");

            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if (BuildConfig.DEBUG) Log.d(TAG, "areItemsTheSame");

            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }

        @Override
        public int getOldListSize() {
            if (BuildConfig.DEBUG) Log.d(TAG, "getOldListSize");

            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            if (BuildConfig.DEBUG) Log.d(TAG, "getNewListSize");

            return newList.size();
        }
    }
}
