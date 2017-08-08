package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.BaseAdapter;

/**
 * ViewHolder for music list
 */

public abstract class BaseMusicViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BaseMusicViewHolder.class.getSimpleName();

    public WeakReference<BaseAdapter<? extends BaseMusicViewHolder>> adapter;

    protected BaseMusicViewHolder(View view, BaseAdapter<? extends BaseMusicViewHolder> adapter) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.adapter = new WeakReference<BaseAdapter<? extends BaseMusicViewHolder>>(adapter);

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

        // open action mode here
        adapter.get().toggleCAB(v, adapter.get(), getAdapterPosition());

        return true;
    }
}
