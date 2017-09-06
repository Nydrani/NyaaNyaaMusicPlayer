package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.NyaaUtils;

/**
 * ViewHolder for album list
 */

public class ArtistViewHolder extends BaseMusicViewHolder {
    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    public TextView artistTitle;
    public TextView artistDescription;

    public ArtistDataHolder artistDataHolder;

    public ArtistViewHolder(View view, BaseAdapter<ArtistViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // extra field for artists
        artistTitle = (TextView) view.findViewById(R.id.artist_title);
        artistDescription = (TextView) view.findViewById(R.id.artist_description);

        // instantiate artist data holder
        artistDataHolder = new ArtistDataHolder();

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
        if (!(adapter.get().isCabActive())) {
            NyaaUtils.openArtistList((Activity)v.getContext(), artistDataHolder.artistId);
        }

        // close cab
        super.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLongClick");


        // open action mode
        return super.onLongClick(v);
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    // @TODO upgrade later
    // simple data holder to hold the id of each music piece
    public static class ArtistDataHolder {
        private static final String TAG = ArtistDataHolder.class.getSimpleName();

        public long artistId;

        public ArtistDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
