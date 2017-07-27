package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for the playback queue
 */

public class QueueViewHolder extends BaseMusicViewHolder {
    private static final String TAG = QueueViewHolder.class.getSimpleName();

    public QueueViewHolder(View view, BaseAdapter<QueueViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // onclick for each item
        // @TODO fix this up soon lmao
        view.setOnClickListener(this);
    }


    // ========================================================================
    // Internal OnClickListener overrides
    // ========================================================================

    @Override
    public void onClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

        // remove song
        if (!(adapter.get().isCABOpen())) {
            if (BuildConfig.DEBUG) Log.d(TAG, "removing item from queue");

            if (!MusicUtils.load(getAdapterPosition())) {
                MusicUtils.load(0);
            }
        }

        // close cab
        super.onClick(v);
    }
}
