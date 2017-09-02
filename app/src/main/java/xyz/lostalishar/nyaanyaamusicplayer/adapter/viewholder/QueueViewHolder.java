package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for the playback queue
 */

public class QueueViewHolder extends BaseMusicViewHolder {
    private static final String TAG = QueueViewHolder.class.getSimpleName();

    public TextView queueTitle;
    public TextView queueDescription;
    public ImageView queueMenu;

    public PopupMenu popupMenu;

    public QueueDataHolder queueDataHolder;

    public QueueViewHolder(View view, BaseAdapter<QueueViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        queueTitle = (TextView)view.findViewById(R.id.queue_title);
        queueDescription = (TextView)view.findViewById(R.id.queue_description);
        queueMenu = (ImageView)view.findViewById(R.id.queue_menu);

        popupMenu = new PopupMenu(itemView.getContext(), queueMenu);
        popupMenu.inflate(R.menu.popup_queue);

        // set the onclick to show menu
        queueMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

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

        int position = getAdapterPosition();

        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
            position = 0;
        }

        // play music
        if (!(adapter.get().cabHolder.isCabOpen())) {
            MusicUtils.load(position);
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
