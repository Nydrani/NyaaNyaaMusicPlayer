package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for the playback queue
 */

public class QueueViewHolder extends BaseMusicViewHolder {
    private static final String TAG = QueueViewHolder.class.getSimpleName();

    public QueueViewHolder(View view) {
        super(view);
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
        super.onClick(v);
        if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

        // remove song
        MusicUtils.removeFromQueue(getAdapterPosition());
    }
}
