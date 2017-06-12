package xyz.velvetmilk.nyaanyaamusicplayer.adapter;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.model.Music;
import xyz.velvetmilk.nyaanyaamusicplayer.util.MusicUtils;

/**
 * Created by nydrani on 28/05/17.
 *
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private static final String TAG = MusicAdapter.class.getSimpleName();

    private List<Music> musicList;


    public MusicAdapter(List<Music> musicList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.musicList = musicList;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_music, parent, false);

        return new MusicViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Music music = musicList.get(position);

        holder.musicTitle.setText(music.getName());
        holder.musicDescription.setText(music.getArtistName());

        // store id
        holder.musicDataHolder.musicId = music.getId();
    }

    @Override
    public long getItemId(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemId");

        return musicList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemCount");

        return musicList.size();
    }


    // ========================================================================
    // Useful cursor functions
    // ========================================================================

    public void swap(List<Music> newList){
        if (BuildConfig.DEBUG) Log.d(TAG, "swap");

        if (newList == null) {
            musicList.clear();
        } else {
            musicList.addAll(newList);
        }
        notifyDataSetChanged();
    }


    // ========================================================================
    // Internal classes
    // ========================================================================

    // inner class to hold a reference to each item of RecyclerView
    public static class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG = MusicViewHolder.class.getSimpleName();

        public TextView musicTitle;
        public TextView musicDescription;

        public MusicDataHolder musicDataHolder;

        public MusicViewHolder(View view) {
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


        // ==================================
        // internal OnClickListener overrides
        // ==================================

        @Override
        public void onClick(View v) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onClick");

            Snackbar.make(v, musicTitle.getText(), Snackbar.LENGTH_LONG)
                    .setAction("Description", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), musicDescription.getText(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }).show();
            // play song here
            MusicUtils.play(musicDataHolder.musicId);
        }
    }

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
