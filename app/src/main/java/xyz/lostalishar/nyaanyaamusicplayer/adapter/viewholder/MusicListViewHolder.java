package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for music list
 */

public class MusicListViewHolder extends BaseMusicViewHolder {
    private static final String TAG = MusicListViewHolder.class.getSimpleName();

    public MusicListViewHolder(View view, BaseAdapter<MusicListViewHolder> adapter) {
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

        // play song here
        if (!(adapter.get().isCABOpen())) {
            if (BuildConfig.DEBUG) Log.d(TAG, "adding item to queue");

            MusicUtils.addToQueue(musicDataHolder.musicId);
        }

        // close cab
        super.onClick(v);
    }
}
