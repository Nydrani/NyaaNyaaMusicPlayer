package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.annotation.NonNull;
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
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.MusicListViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class MusicAdapter extends BaseAdapter<MusicListViewHolder> {
    private static final String TAG = MusicAdapter.class.getSimpleName();

    private List<Music> musicList;

    public MusicAdapter(List<Music> musicList, CabHolder cabHolder) {
        super(cabHolder);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.musicList = musicList;
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public @NonNull MusicListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_music, parent, false);

        return new MusicListViewHolder(v, this);
    }
    @Override
    public void onBindViewHolder(@NonNull MusicListViewHolder holder, int position) {
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
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabCreated");

        materialCab.setMenu(R.menu.context_music_list);
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
            case R.id.actionbar_add:
                MusicUtils.enqueue(addItems(), null);
                cab.finish();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
        }

        return false;
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

    private long[] addItems() {
        if (BuildConfig.DEBUG) Log.d(TAG, "addItems");

        long[] addArray = new long[chosenItems.size()];
        for (int i = 0; i < addArray.length; i++) {
            addArray[i] = musicList.get(chosenItems.get(i)).getId();
        }

        return addArray;
    }
}
