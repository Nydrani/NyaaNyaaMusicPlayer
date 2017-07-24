package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

public class QueueAdapter extends RecyclerView.Adapter<QueueViewHolder> {
    private static final String TAG = QueueAdapter.class.getSimpleName();

    private List<Music> musicList;

    public QueueAdapter(List<Music> musicList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.musicList = musicList;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_music, parent, false);

        return new QueueViewHolder(v);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Music music = musicList.get(position);

        holder.musicTitle.setText(music.getName());
        holder.musicDescription.setText(music.getArtistName());

        // store id
        holder.musicDataHolder.musicId = music.getId();

        // update background color if i am playing
        MusicPlaybackState state = MusicUtils.getState();
        if (state != null && state.getQueuePos() == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.color.colorPrimaryDark));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.color.white));
        }
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
