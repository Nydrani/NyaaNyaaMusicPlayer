package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for album list
 */

public class AlbumViewHolder extends BaseMusicViewHolder {
    private static final String TAG = AlbumViewHolder.class.getSimpleName();

    public AlbumViewHolder(View view, BaseAdapter<AlbumViewHolder> adapter) {
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

        // add song here
        if (!(adapter.get().isCABOpen())) {
            MusicUtils.enqueue(new long[] { musicDataHolder.musicId }, null);
        }

        // close cab
        super.onClick(v);
    }
}
