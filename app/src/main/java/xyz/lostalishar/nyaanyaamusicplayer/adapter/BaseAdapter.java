package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.BaseMusicViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public abstract class BaseAdapter<VH extends BaseMusicViewHolder> extends RecyclerView.Adapter<VH>
        implements ActionMode.Callback {
    private static final String TAG = BaseAdapter.class.getSimpleName();

    public List<View> chosenViews;
    public List<Integer> chosenItems;
    public CabHolder cabHolder;
    public ActionMode actionMode;

    protected BaseAdapter(CabHolder cabHolder) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.cabHolder = cabHolder;
        chosenItems = new ArrayList<>();
        chosenViews = new ArrayList<>();

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

        // clear color when destroyed
        for (View v : chosenViews) {
            v.setBackground(null);
        }
        chosenItems.clear();
        chosenViews.clear();
    }


    // ========================================================================
    // Multi item toggle CAB
    // ========================================================================

    public void toggleCab(View v, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "toggleCab");

        int foundItem = chosenItems.indexOf(position);

        // add/remove from list functionality
        if (foundItem == -1) {
            // add color
            v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.grey));
            chosenViews.add(v);
            chosenItems.add(position);
        } else {
            // remove color
            v.setBackground(null);
            chosenViews.remove(v);
            chosenItems.remove(foundItem);
        }

        // cab open/close functionality
        if (!cabHolder.isCabOpen()) {
            actionMode = cabHolder.openCab(this);
        } else if (cabHolder.isCabOpen() && chosenItems.size() == 0) {
            cabHolder.closeCab();
            actionMode = null;
        }

        // set cab title
        // @TODO set title to name of highlighted instead of just count
        if (chosenItems.size() > 0) {
            actionMode.setTitle(String.valueOf(chosenItems.size()));
        }
    }
}
