package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for the playback queue
 */

public class QueueViewHolder extends BaseMusicViewHolder {
    private static final String TAG = QueueViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;
    public TextView musicMenu;

    public QueueDataHolder queueDataHolder;

    public QueueViewHolder(View view, BaseAdapter<QueueViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicTitle = (TextView)view.findViewById(R.id.queue_title);
        musicDescription = (TextView)view.findViewById(R.id.queue_description);
        musicMenu = (TextView)view.findViewById(R.id.queue_menu);

        // instantiate music data holder
        queueDataHolder = new QueueDataHolder();

        // onclick for each item
        // @TODO fix this up soon lmao
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }


    // ========================================================================
    // Internal OnClickListener overrides
    // ========================================================================

    @Override
    public void onClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

        // play music
        if (!MusicUtils.load(getAdapterPosition())) {
            MusicUtils.load(0);
        }

        // for multi click toggle
        super.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLongClick");

        return true;
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    // @TODO upgrade later
    // simple data holder to hold the id of each music piece
    public static class QueueDataHolder {
        private static final String TAG = QueueDataHolder.class.getSimpleName();

        public long musicId;
        public int position;

        public QueueDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
