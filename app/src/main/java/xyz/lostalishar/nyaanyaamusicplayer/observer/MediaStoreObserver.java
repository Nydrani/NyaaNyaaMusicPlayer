package xyz.lostalishar.nyaanyaamusicplayer.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnMediaStoreChangedListener;

/**
 * ContentObserver on MediaStore to update list if needed
 */

public class MediaStoreObserver extends ContentObserver {
    private static final String TAG = MediaStoreObserver.class.getSimpleName();

    private OnMediaStoreChangedListener listener;

    public MediaStoreObserver(Handler handler, OnMediaStoreChangedListener listener) {
        super(handler);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.listener = listener;
    }


    //=========================================================================
    // ContentObserver overrides
    //=========================================================================

    @Override
    public void onChange(boolean selfChange) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onChange");

        listener.onMediaStoreChanged();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onChange");

        onChange(selfChange);
    }
}
