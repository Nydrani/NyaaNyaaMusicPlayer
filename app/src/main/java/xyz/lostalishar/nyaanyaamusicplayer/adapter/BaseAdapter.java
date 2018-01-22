package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;

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
        implements MaterialCab.Callback {
    private static final String TAG = BaseAdapter.class.getSimpleName();

    protected List<View> chosenViews;
    protected List<Integer> chosenItems;
    protected CabHolder cabHolder;
    protected MaterialCab cab;

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
    public boolean onCabFinished(MaterialCab materialCab) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabFinished");

        clearLists();
        return true;
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
        if (cab == null || !cab.isActive()) {
            cab = cabHolder.openCab(this);
        } else if (cab.isActive() && chosenItems.size() == 0) {
            cab.finish();
        }

        // set cab title
        // @TODO set title to name of highlighted instead of just count
        if (chosenItems.size() > 0) {
            cab.setTitle(String.valueOf(chosenItems.size()));
        }
    }

    public boolean isCabActive() {
        if (BuildConfig.DEBUG) Log.d(TAG, "isCabActive");

        return cab != null && cab.isActive();
    }

    private void clearLists() {
        if (BuildConfig.DEBUG) Log.d(TAG, "clearLists");

        for (View v : chosenViews) {
            v.setBackground(null);
        }

        chosenItems.clear();
        chosenViews.clear();
    }
}
