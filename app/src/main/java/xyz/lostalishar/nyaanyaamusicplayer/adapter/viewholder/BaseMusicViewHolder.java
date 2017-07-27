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
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;

/**
 * ViewHolder for music list
 */

public abstract class BaseMusicViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BaseMusicViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;

    public WeakReference<BaseAdapter<? extends BaseMusicViewHolder>> adapter;

    public BaseMusicDataHolder musicDataHolder;

    protected BaseMusicViewHolder(View view, BaseAdapter<? extends BaseMusicViewHolder> adapter) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        // get reference to adapter
        this.adapter = new WeakReference<BaseAdapter<? extends BaseMusicViewHolder>>(adapter);

        musicTitle = (TextView) view.findViewById(R.id.music_name);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

        // instantiate music data holder
        musicDataHolder = new BaseMusicDataHolder();

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

        Snackbar.make(v, musicTitle.getText(), Snackbar.LENGTH_LONG)
                .setAction("Description", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), musicDescription.getText(), Toast.LENGTH_LONG)
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
    public static class BaseMusicDataHolder {
        private static final String TAG = BaseMusicDataHolder.class.getSimpleName();

        public long musicId;

        public BaseMusicDataHolder() {
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
            // empty constructor for now
        }
    }
}
