package xyz.velvetmilk.nyaanyaamusicplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.R;
import xyz.velvetmilk.nyaanyaamusicplayer.model.Music;

/**
 * Created by nydrani on 28/05/17.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private static final String TAG = MusicAdapter.class.getSimpleName();

    private Context context;
    private List<Music> musicList;


    public MusicAdapter(Context context, List<Music> musicList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.context = context;
        this.musicList = musicList;
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater li = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // create a new view
        View v = li.inflate(R.layout.list_layout_music, parent, false);
        MusicViewHolder vh = new MusicViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        // Cursor functions
        /*
        musicList.moveToPosition(position);
        String name = musicList.getString(musicList.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = musicList.getString(musicList.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        */
        Music music = musicList.get(position);

        holder.musicTitle.setText(music.getName());
        holder.musicDescription.setText(music.getArtistName());

    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemCount");

        return musicList.size();
    }


    // ========================================================================
    // Useful cursor functions
    // ========================================================================

    /*
    public Cursor swapCursor(Cursor cursor) {
        if (musicList == cursor) {
            return null;
        }
        Cursor oldCursor = musicList;
        musicList = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }
    */
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
    // Internal class
    // ========================================================================

    // inner class to hold a reference to each item of RecyclerView
    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = MusicViewHolder.class.getSimpleName();

        public TextView musicTitle;
        public TextView musicDescription;

        public MusicViewHolder(View view) {
            super(view);
            if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

            musicTitle = (TextView) view.findViewById(R.id.music_name);
            musicDescription = (TextView) view.findViewById(R.id.music_description);
        }
    }
}
