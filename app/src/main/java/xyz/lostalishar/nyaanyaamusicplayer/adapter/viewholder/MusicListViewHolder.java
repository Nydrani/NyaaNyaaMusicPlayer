package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * ViewHolder for music list
 */

public class MusicListViewHolder extends BaseMusicViewHolder implements View.OnClickListener,
        View.OnLongClickListener {
    private static final String TAG = MusicListViewHolder.class.getSimpleName();

    public MusicListViewHolder(View view) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

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

        // play song here
        MusicUtils.play(musicDataHolder.musicId);
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
}
