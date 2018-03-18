package xyz.lostalishar.nyaanyaamusicplayer.loader;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;
import xyz.lostalishar.nyaanyaamusicplayer.interfaces.OnMediaStoreChangedListener;
import xyz.lostalishar.nyaanyaamusicplayer.observer.MediaStoreObserver;

/**
 * MediaObservingLoader uses a content observer to observe database changes
 */

public abstract class MediaObservingLoader<D> extends CachedAsyncTaskLoader<D> implements OnMediaStoreChangedListener {
    private static final String TAG = MediaObservingLoader.class.getSimpleName();

    private ContentObserver contentObserver;
    private Uri uri;

    protected MediaObservingLoader(Context context, Uri uri) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");

        this.uri = uri;
        contentObserver = new MediaStoreObserver(new Handler(), this);
    }

    @Override
    protected void onStartLoading() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartLoading");
        super.onStartLoading();

        getContext().getContentResolver().registerContentObserver(
                uri, true, contentObserver);
    }

    @Override
    protected void onReset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReset");
        super.onReset();

        getContext().getContentResolver().unregisterContentObserver(contentObserver);
    }


    //=========================================================================
    // MediaStoreChangedListener implementation
    //=========================================================================

    public void onMediaStoreChanged() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onMediaStoreChanged");

        onContentChanged();
    }
}
