package xyz.lostalishar.nyaanyaamusicplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.model.Artist;

/**
 * Loads a list of Artist classes from the MediaStore cursor in the background
 */

public class ArtistLoader extends MediaObservingLoader<List<Artist>> {
    private static final String TAG = ArtistLoader.class.getSimpleName();

    private List<Artist> artistList;

    public ArtistLoader(Context context, Uri uri) {
        super(context, uri);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        artistList = new ArrayList<>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    @Override
    public List<Artist> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

        // reset the array
        artistList = new ArrayList<>();

        Cursor cursor = getCursor();

        // can sometimes return null on bad problems
        if (cursor == null) {
            return artistList;
        }

        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST);
        int numTracksColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);
        int numAlbumsColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS);


        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            final long id = cursor.getLong(idColumn);
            final String artistName = cursor.getString(artistColumn);
            final int numTracks = cursor.getInt(numTracksColumn);
            final int numAlbums = cursor.getInt(numAlbumsColumn);

            final Artist artist = new Artist(id, artistName, numTracks, numAlbums);

            // @TODO debugging
            Log.v(TAG, artist.toString());

            artistList.add(artist);
        }

        cursor.close();

        return artistList;
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private Cursor getCursor() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCursor");

        Context context = getContext();
        return makeArtistCursor(context);
    }

    // override this to change query
    public static Cursor makeArtistCursor(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeArtistCursor");

        ContentResolver musicResolver = context.getContentResolver();

        Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[4];
        // String selection = MediaStore.Audio.Media.IS_MUSIC + "=?";
        // String args[] = { "1" };
        String sortOrder = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Artists._ID;
        projection[1] = MediaStore.Audio.ArtistColumns.ARTIST;
        projection[2] = MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS;
        projection[3] = MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS;

        return musicResolver.query(musicUri, projection, null, null, sortOrder);
    }
}
