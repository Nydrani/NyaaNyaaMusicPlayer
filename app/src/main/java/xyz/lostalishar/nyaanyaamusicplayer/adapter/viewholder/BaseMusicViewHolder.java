package xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;

/**
 * ViewHolder for music list
 */

public abstract class BaseMusicViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BaseMusicViewHolder.class.getSimpleName();

    public TextView musicTitle;
    public TextView musicDescription;

    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    public BaseMusicDataHolder musicDataHolder;

    protected BaseMusicViewHolder(View view) {
        super(view);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicTitle = (TextView) view.findViewById(R.id.music_name);
        musicDescription = (TextView) view.findViewById(R.id.music_description);

        // instantiate music data holder
        musicDataHolder = new BaseMusicDataHolder();

        // onclick for each item
        // @TODO fix this up soon lmao
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        // setup action mode callback
        actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onCreateActionMode");

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_music_list, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onPrepareActionMode");

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onActionItemClicked");

                int id = item.getItemId();

                switch (id) {
                    case R.id.actionbar_details:
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.actionbar_about:
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onDestroyActionMode");

                actionMode = null;
            }
        };
    }


    // ========================================================================
    // Internal OnClickListener overrides
    // ========================================================================

    @Override
    public void onClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

        // finish action mode here
        if (actionMode != null) {
            actionMode.finish();
        }

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

        if (actionMode == null) {
            v.setSelected(true);
            actionMode = v.startActionMode(actionModeCallback);
        }

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
