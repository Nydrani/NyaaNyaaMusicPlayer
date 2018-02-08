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
import xyz.lostalishar.nyaanyaamusicplayer.model.Album;

/**
 * Loads a List of Music classes from the MediaStore cursor in the background
 */

public class AlbumLoader extends CachedAsyncTaskLoader<List<Album>> {
    private static final String TAG = AlbumLoader.class.getSimpleName();

    private List<Album> albumList;

    public AlbumLoader(Context context) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        albumList = new ArrayList<>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    @Override
    public List<Album> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

        // reset the array
        albumList = new ArrayList<>();

        Cursor cursor = getCursor();

        // can sometimes return null on bad problems
        if (cursor == null) {
            return albumList;
        }

        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
        int numSongsColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            final long id = cursor.getLong(idColumn);
            final String name = cursor.getString(titleColumn);
            final String artistName = cursor.getString(artistColumn);
            final int numSongs = cursor.getInt(numSongsColumn);

            final Album album = new Album(id, name, artistName, numSongs);

            // @TODO debugging
            Log.v(TAG, album.toString());

            albumList.add(album);
        }

        cursor.close();

        return albumList;
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private Cursor getCursor() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCursor");

        Context context = getContext();
        return makeAlbumCursor(context);
    }

    // override this to change query
    public static Cursor makeAlbumCursor(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeAlbumCursor");

        ContentResolver musicResolver = context.getContentResolver();

        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = new String[4];
        String selection = MediaStore.Audio.Media.IS_MUSIC + "=?";
        String args[] = { "1" };
        String sortOrder = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Albums._ID;
        projection[1] = MediaStore.Audio.AlbumColumns.ALBUM;
        projection[2] = MediaStore.Audio.AlbumColumns.ARTIST;
        projection[3] = MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS;

        return musicResolver.query(musicUri, projection, null, null, sortOrder);
    }
}
