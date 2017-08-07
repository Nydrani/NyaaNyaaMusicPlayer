package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.AlbumAdapter;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for album list
 */

public class AlbumViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = AlbumViewHolder.class.getSimpleName();

    public TextView albumTitle;
    public TextView numSongs;

    public WeakReference<AlbumAdapter> adapter;

    public AlbumDataHolder albumDataHolder;

    public AlbumViewHolder(View view, AlbumAdapter adapter) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.adapter = new WeakReference<>(adapter);

        // extra field for album
        albumTitle = (TextView) view.findViewById(R.id.album_title);
        numSongs = (TextView) view.findViewById(R.id.album_num_songs);

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

        // finish action mode here
        adapter.get().finishCAB();
    }

    @Override
    public boolean onLongClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLongClick");

        Snackbar.make(v, albumTitle.getText(), Snackbar.LENGTH_LONG)
                .setAction("Description", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), albumTitle.getText(), Toast.LENGTH_LONG)
                                .show();
                    }
                }).show();

        // open action mode here
        adapter.get().openCAB(v, getAdapterPosition());

        return true;
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
