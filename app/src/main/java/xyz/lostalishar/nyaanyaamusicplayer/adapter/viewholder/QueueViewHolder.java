package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for the playback queue
 */

public class QueueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {
    private static final String TAG = QueueViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;

    public MusicDataHolder musicDataHolder;

    public QueueViewHolder(View view) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // onclick for each item
        // @TODO fix this up soon lmao
        view.setOnClickListener(this);

        musicTitle = (TextView) view.findViewById(R.id.music_name);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

        // instantiate music data holder
        musicDataHolder = new MusicDataHolder();
    }


    // ========================================================================
    // Internal OnClickListener overrides
    // ========================================================================

    @Override
    public void onClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

        // remove song
        MusicUtils.removeFromQueue(getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLongClick");

        Snackbar.make(v, musicTitle.getText(), Snackbar.LENGTH_LONG)
                .setAction("Description", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), musicDescription.getText(), Toast.LENGTH_LONG)
                                .show();
                    }
                }).show();

        return true;
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