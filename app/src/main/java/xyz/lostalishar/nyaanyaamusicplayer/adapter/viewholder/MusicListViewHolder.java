package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for music list
 */

public class MusicListViewHolder extends BaseMusicViewHolder {
    private static final String TAG = MusicListViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;

    public MusicDataHolder musicDataHolder;

    public MusicListViewHolder(View view, BaseAdapter<MusicListViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicTitle = (TextView) view.findViewById(R.id.music_title);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

        // instantiate music data holder
        musicDataHolder = new MusicDataHolder();

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

        // add song here
        if (!(adapter.get().cabHolder.isCabOpen())) {
            MusicUtils.enqueue(new long[] { musicDataHolder.musicId }, null);
        }

        // close cab
        super.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLongClick");

        // show cab
        return super.onLongClick(v);
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    // @TODO upgrade later
    // simple data holder to hold the id of each music piece
    public static class MusicDataHolder {
        private static final String TAG = MusicDataHolder.class.getSimpleName();

        public long musicId;

        public MusicDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
