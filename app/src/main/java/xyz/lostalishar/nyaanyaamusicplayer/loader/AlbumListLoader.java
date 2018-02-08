package xyz.lostalishar.nyaanyaamusicplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

/**
 * Loads a list of Music classes from the MediaStore cursor in the background from an album
 */

public class AlbumListLoader extends MusicListLoader {
    private static final String TAG = AlbumListLoader.class.getSimpleName();

    private long albumId = MusicPlaybackService.UNKNOWN_ID;

    public AlbumListLoader(Context context, long albumId) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.albumId = albumId;
    }


    //=========================================================================
    // Helper functions
    //=========================================================================

    // override this to change query
    @Override
    public Cursor makeMusicCursor(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "makeMusicCursor");

        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[6];
        String selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String args[] = { String.valueOf(albumId) };
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
