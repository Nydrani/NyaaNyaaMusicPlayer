package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * ViewHolder for music list
 */

public abstract class BaseMusicViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = BaseMusicViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;

    public BaseMusicDataHolder musicDataHolder;

    public BaseMusicViewHolder(View view) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // onclick for each item
        // @TODO fix this up soon lmao

        musicTitle = (TextView) view.findViewById(R.id.music_name);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

        // instantiate music data holder
        musicDataHolder = new BaseMusicDataHolder();
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    // @TODO upgrade later
    // simple data holder to hold the id of each music piece
    public static class BaseMusicDataHolder {
        private static final String TAG = BaseMusicDataHolder.class.getSimpleName();

        public long musicId;

        public BaseMusicDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
