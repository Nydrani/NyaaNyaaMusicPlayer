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
 * Loads a List of Music classes from the MediaStore cursor in the background
 */

public class ArtistListLoader extends MusicListLoader {
    private static final String TAG = ArtistListLoader.class.getSimpleName();

    private long artistId = MusicPlaybackService.UNKNOWN_ID;

    public ArtistListLoader(Context context, long artistId) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.artistId = artistId;
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
        String selection = MediaStore.Audio.Media.ARTIST_ID + "=?";
        String args[] = { String.valueOf(artistId) };
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
