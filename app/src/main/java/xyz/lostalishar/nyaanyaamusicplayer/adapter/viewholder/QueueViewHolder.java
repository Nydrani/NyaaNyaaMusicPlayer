package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;
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

    public TextView musicTitle;
    public TextView musicDescription;

    public QueueDataHolder queueDataHolder;

    public QueueViewHolder(View view, BaseAdapter<QueueViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicTitle = (TextView) view.findViewById(R.id.music_name);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

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
