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

public class AlbumViewHolder extends BaseMusicViewHolder {
    private static final String TAG = AlbumViewHolder.class.getSimpleName();

    public TextView albumTitle;
    public TextView numTracks;

    public AlbumDataHolder albumDataHolder;

    public AlbumViewHolder(View view, BaseAdapter<AlbumViewHolder> adapter) {
        super(view, adapter);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // extra field for album
        albumTitle = (TextView) view.findViewById(R.id.album_title);
        numTracks = (TextView) view.findViewById(R.id.album_num_tracks);

        // instantiate album data holder
        albumDataHolder = new AlbumDataHolder();

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
            NyaaUtils.openAlbumList((Activity)v.getContext(), albumDataHolder.albumId);
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
    public static class AlbumDataHolder {
        private static final String TAG = AlbumDataHolder.class.getSimpleName();

        public long albumId;

        public AlbumDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
