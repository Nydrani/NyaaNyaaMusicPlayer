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

    private List<Music> musicList;

    public MusicListLoader(Context context) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicList = new ArrayList<>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    @Override
    public List<Music> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

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

    private static Cursor makeMusicCursor(Context context) {
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
