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
import xyz.lostalishar.nyaanyaamusicplayer.model.Music;

/**
 * Loads a List of Music classes from the MediaStore cursor in the background
 */

public class MusicListLoader extends MediaObservingLoader<List<Music>> {
    private static final String TAG = MusicListLoader.class.getSimpleName();

    private List<Music> musicList;

    public MusicListLoader(Context context, Uri uri) {
        super(context, uri);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicList = new ArrayList<>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    @Override
    public List<Music> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

        // reset the array
        musicList = new ArrayList<>();

        Cursor cursor = getCursor();

        // can sometimes return null on bad problems
        if (cursor == null) {
            return musicList;
        }


        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            final long id = cursor.getLong(idColumn);
            final String name = cursor.getString(titleColumn);
            final String artistName = cursor.getString(artistColumn);
            final String albumName = cursor.getString(albumColumn);
            final long duration = cursor.getLong(durationColumn);
            final String mimeType = cursor.getString(mimeTypeColumn);

            final int durationInSecs = (int) duration / 1000;
            final Music music = new Music(id, name, artistName, albumName, durationInSecs,
                    mimeType);

            musicList.add(music);
        }

        cursor.close();

        return musicList;
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    private Cursor getCursor() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCursor");

        Context context = getContext();
        return makeMusicCursor(context);
    }

    // override this to change query
    public Cursor makeMusicCursor(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicCursor");

        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[6];
        String selection = MediaStore.Audio.Media.IS_MUSIC + "=?";
        String args[] = { "1" };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Media._ID;
        projection[1] = MediaStore.Audio.Media.TITLE;
        projection[2] = MediaStore.Audio.Media.ARTIST;
        projection[3] = MediaStore.Audio.Media.ALBUM;
        projection[4] = MediaStore.Audio.Media.DURATION;
        projection[5] = MediaStore.Audio.Media.MIME_TYPE;

        return musicResolver.query(musicUri, projection, selection, args, sortOrder);
    }
}
