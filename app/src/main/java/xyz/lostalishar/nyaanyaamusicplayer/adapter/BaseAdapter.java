package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public abstract class BaseAdapter<VH extends BaseMusicViewHolder> extends RecyclerView.Adapter<VH>
        implements ActionMode.Callback {
    private static final String TAG = BaseAdapter.class.getSimpleName();

    protected int chosenItem;

    private ActionMode actionMode;

    protected BaseAdapter(ActionMode actionMode) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.actionMode = actionMode;

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

        actionMode = null;
    }


    // ========================================================================
    // Helper functions
    // ========================================================================

    public void openCAB(View v, Integer position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "openCAB");

        chosenItem = position;

        if (actionMode == null) {
            actionMode = v.startActionMode(this);
        }
    }

    public void finishCAB() {
        if (BuildConfig.DEBUG) Log.d(TAG, "finishCAB");

        if (actionMode != null) {
            actionMode.finish();
            chosenItem = -1;
        }
    }

    public boolean isCABOpen() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCABOpen");

        return actionMode != null;
    }
}
