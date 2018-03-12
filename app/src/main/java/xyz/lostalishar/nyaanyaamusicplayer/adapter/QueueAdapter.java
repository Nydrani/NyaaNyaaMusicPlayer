package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.afollestad.materialcab.MaterialCab;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.QueueViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
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

    private List<Music> musicList;

    public QueueAdapter(List<Music> musicList, CabHolder cabHolder) {
        super(cabHolder);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.musicList = musicList;
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public @NonNull QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_queue, parent, false);

        return new QueueViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Music music = musicList.get(position);

        // store final variables like a pleb
        final int boundPosition = holder.getAdapterPosition();
        final View boundView = holder.itemView;

        holder.queueTitle.setText(music.getName());
        holder.queueDescription.setText(music.getArtistName());
        holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popupmenu_select:
                        toggleCab(boundView, boundPosition);
                        return true;
                    case R.id.popupmenu_remove:
                        MusicUtils.dequeue(new long[] {
                                musicList.get(boundPosition).getId()
                        }, null);
                        return true;
                    default:
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Unknown MenuItem choice: " + item.getTitle().toString());
                        }
                }

                return false;
            }
        });

        // store id
        holder.queueDataHolder.musicId = music.getId();

        // @TODO update background color if current position is playing
        // @TODO change to something with better UI design later lmao
        MusicPlaybackState state = MusicUtils.getState();
        if (state == null || isCabActive()) {
            return;
        }

        if (state.getQueuePos() == position) {
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(holder.itemView.getContext(),R.color.red));
        } else if (holder.itemView.getBackground() != null) {
            holder.itemView.setBackground(null);
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
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabCreated");

        materialCab.setMenu(R.menu.context_queue_list);
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
            case R.id.actionbar_remove:
                MusicUtils.enqueue(removeItems(), null);
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

    public List<Music> getMusicList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getMusicList");

        return musicList;
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

    private long[] removeItems() {
        if (BuildConfig.DEBUG) Log.d(TAG, "removeItems");

        long[] removeArray = new long[chosenItems.size()];
        for (int i = 0; i < removeArray.length; i++) {
            removeArray[i] = musicList.get(chosenItems.get(i)).getId();
        }

        return removeArray;
    }
}
