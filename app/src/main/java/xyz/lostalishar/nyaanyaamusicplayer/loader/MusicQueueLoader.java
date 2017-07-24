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
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

/**
 * Loads a List of Music classes from the MediaStore cursor in the background
 */

public class MusicQueueLoader extends CachedAsyncTaskLoader<List<Music>> {
    private static final String TAG = MusicQueueLoader.class.getSimpleName();

    private List<Music> musicList;

    public MusicQueueLoader(Context context) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        musicList = new ArrayList<>();
    }


    //=========================================================================
    // AsyncTaskLoader implementation
    //=========================================================================

    // @TODO it seems that multiple musicId doesnt work (queue ui doesn't display it)
    @Override
    public List<Music> loadInBackground() {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInBackground");

        Cursor cursor = getCursor();

        // @TODO for now (since list position is not relative to database position due to sorting
        // @TODO or not sorting changing the order
        // @TODO fix is Issue #2
        // @TODO might have issues with concurrency
        List<MusicPlaybackTrack> queueArray = MusicUtils.getQueue();


        // can sometimes return null on bad problems
        if (cursor == null) {
            return musicList;
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "QueueCursor size: " + cursor.getCount());


        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

        for (MusicPlaybackTrack track : queueArray) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final long id = cursor.getLong(idColumn);

                // @TODO skip if id is incorrect (this is O(n^2)) but cant do any better right now
                // @TODO so add will be in the correct position
                if (id != track.getId()) {
                    continue;
                }

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
    public static Cursor makeMusicCursor(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicCursor");

        // obtain the current queue from the service
        List<MusicPlaybackTrack> queueArray = MusicUtils.getQueue();
        int queueArraySize = queueArray.size();

        if (BuildConfig.DEBUG) Log.d(TAG, "QueueArray size: " + queueArraySize);


        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[6];

        // build the selection to get only the rows from the ids
        StringBuilder selection = new StringBuilder();
        String[] args = new String[queueArraySize];

        // build the selection statement
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < queueArraySize; i++) {
            args[i] = String.valueOf(queueArray.get(i).getId());
            selection.append("?, ");
        }
        // edge case for empty array size
        if (queueArraySize != 0) {
            selection.delete(selection.length() - 2, selection.length());
        }
        selection.append(")");

        if (BuildConfig.DEBUG) Log.d(TAG, "Selection query: " + selection.toString());

        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        projection[0] = MediaStore.Audio.Media._ID;
        projection[1] = MediaStore.Audio.Media.TITLE;
        projection[2] = MediaStore.Audio.Media.ARTIST;
        projection[3] = MediaStore.Audio.Media.ALBUM;
        projection[4] = MediaStore.Audio.Media.DURATION;
        projection[5] = MediaStore.Audio.Media.MIME_TYPE;

        return musicResolver.query(musicUri, projection, selection.toString(), args, sortOrder);
    }
}
