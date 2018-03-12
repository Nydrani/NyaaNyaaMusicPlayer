package xyz.lostalishar.nyaanyaamusicplayer.adapter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;

import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.R;
import xyz.lostalishar.nyaanyaamusicplayer.adapter.viewholder.ArtistViewHolder;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.CabHolder;
import xyz.lostalishar.nyaanyaamusicplayer.model.Artist;

/**
 * Currently not implementing a List rather than Cursor due to:
 *     1. It will use UI thread to convert database response to List (which is bad)
 *     2. No objectively good reasoning (no real google implementation yet)
 *     3. cbf doing benchmarks
 */

public class ArtistAdapter extends BaseAdapter<ArtistViewHolder> {
    private static final String TAG = ArtistAdapter.class.getSimpleName();

    private List<Artist> artistList;

    public ArtistAdapter(List<Artist> artistList, CabHolder cabHolder) {
        super(cabHolder);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.artistList = artistList;

        // @TODO check if ids are stable
        // ids are stable. at least i would hope (pls be stable MediaStore)
        setHasStableIds(true);
    }


    // ========================================================================
    // RecyclerView.Adapter overrides
    // ========================================================================

    @Override
    public @NonNull ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_layout_artist, parent, false);

        return new ArtistViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onBindViewHolder");

        Artist artist = getArtistList().get(position);

        String artistName = artist.getName();

        int numAlbums = artist.getNumAlbums();
        int numTracks = artist.getNumTracks();
        String albumsDescription = holder.itemView.getResources().
                getQuantityString(R.plurals.num_albums, numAlbums, numAlbums);
        String tracksDescription = holder.itemView.getResources().
                getQuantityString(R.plurals.num_tracks, numTracks, numTracks);

        String descriptionMessage = albumsDescription + " | " + tracksDescription;

        holder.artistTitle.setText(artistName);
        holder.artistDescription.setText(descriptionMessage);

        // store id
        holder.artistDataHolder.artistId = artist.getId();
        holder.artistDataHolder.artistName = artistName;
    }

    @Override
    public long getItemId(int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemId");

        return artistList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getItemCount");

        return artistList.size();
    }


    // ========================================================================
    // ActionMode.Callback overrides
    // ========================================================================

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabCreated");

        materialCab.setMenu(R.menu.context_artist_list);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCabItemClicked");

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.actionbar_details:
                cab.finish();
                return true;
            default:
                if (BuildConfig.DEBUG) Log.w(TAG, "Unknown menu item id: " + id);
        }

        return false;
    }


    // ========================================================================
    // Exposed functions
    // ========================================================================

    public List<Artist> getArtistList() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getArtistList");

        return artistList;
    }


    // ========================================================================
    // Useful cursor functions
    // ========================================================================

    public void swap(List<Artist> newList){
        if (BuildConfig.DEBUG) Log.d(TAG, "swap");

        artistList.clear();
        if (newList != null) {
            artistList.addAll(newList);
        }
        notifyDataSetChanged();
    }
}
