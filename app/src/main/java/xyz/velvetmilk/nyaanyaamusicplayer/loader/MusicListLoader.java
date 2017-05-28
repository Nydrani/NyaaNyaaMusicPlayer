package xyz.velvetmilk.nyaanyaamusicplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;
import xyz.velvetmilk.nyaanyaamusicplayer.model.Music;

/**
 * Created by nydrani on 27/05/17.
 */

public class MusicListLoader extends CachedAsyncTaskLoader<List<Music>> {
    private static final String TAG = MusicListLoader.class.getSimpleName();

    protected List<Music> musicList;
    protected Cursor cursor;

    public MusicListLoader(final Context context) {
        super(context);

        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicList = new ArrayList<Music>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    @Override
    public List<Music> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

        cursor = getCursor();

        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final long id = cursor.getLong(0);
                final String name = cursor.getString(1);
                final String artistName = cursor.getString(2);
                final String albumName = cursor.getString(3);
                final long duration = cursor.getLong(4);
                final String mimeType = cursor.getString(5);

                final int durationInSecs = (int) duration / 1000;
                final Music music = new Music(id, name, artistName, albumName, durationInSecs,
                        mimeType);

                musicList.add(music);
            }
        }

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        return musicList;
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    protected Cursor getCursor() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getCursor");

        Context context = getContext();
        return makeMusicCursor(context);
    }

    public static final Cursor makeMusicCursor(final Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicCursor");

        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[6];
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Media._ID;
        projection[1] = MediaStore.Audio.Media.TITLE;
        projection[2] = MediaStore.Audio.Media.ARTIST;
        projection[3] = MediaStore.Audio.Media.ALBUM;
        projection[4] = MediaStore.Audio.Media.DURATION;
        projection[5] = MediaStore.Audio.Media.MIME_TYPE;

        return musicResolver.query(musicUri, projection, selection, null, sortOrder);
    }
}
