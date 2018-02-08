package xyz.lostalishar.nyaanyaamusicplayer.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * ContentObserver on MediaStore to update list if needed
 */

public class MediaStoreObserver extends ContentObserver {
    private static final String TAG = MediaStoreObserver.class.getSimpleName();

    public MediaStoreObserver(Handler handler) {
        super(handler);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    //=========================================================================
    // ContentObserver implementation
    //=========================================================================

    @Override
    public void onChange(boolean selfChange) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onChange");

        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onChange");

        if (BuildConfig.DEBUG) Log.d(TAG, "something changed");
    }
}
