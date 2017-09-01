package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;

import java.lang.ref.WeakReference;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.ui.fragment.BaseFragment;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public abstract class BaseAdapter<VH extends BaseMusicViewHolder> extends RecyclerView.Adapter<VH>
        implements ActionMode.Callback {
    private static final String TAG = BaseAdapter.class.getSimpleName();

    public int chosenItem;
    public CabHolder cabHolder;

    protected BaseAdapter(CabHolder cabHolder) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.cabHolder = cabHolder;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPrepareActionMode");

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyActionMode");

        chosenItem = MusicPlaybackService.UNKNOWN_POS;
    }


    // ========================================================================
    // Multi item toggle CAB
    // ========================================================================

    public void toggleCab(View v, ActionMode.Callback callback, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "toggleCab");

        chosenItem = position;
        cabHolder.openCab(callback);
    }
}
