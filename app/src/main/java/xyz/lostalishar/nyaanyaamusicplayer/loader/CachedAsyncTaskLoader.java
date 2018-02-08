package xyz.lostalishar.nyaanyaamusicplayer.loader;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import xyz.lostalishar.nyaanyaamusicplayer.BuildConfig;

/**
 * AsyncTaskLoader that caches the result of the asynchronous task
 */

public abstract class CachedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {
    private static final String TAG = CachedAsyncTaskLoader.class.getSimpleName();
    private D mData;

    protected CachedAsyncTaskLoader(Context context) {
        super(context);
        if (BuildConfig.DEBUG) Log.d(TAG, "constructor");
    }


    @Override
    protected void onStartLoading() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartLoading");
        super.onStartLoading();

        if (takeContentChanged() || mData == null) {
            forceLoad();
        } else {
            deliverResult(mData);
        }
    }

    @Override
    public void deliverResult(D data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "deliverResult");

        if (!isReset()) {
            mData = data;
            if (isStarted()) {
                super.deliverResult(data);
            }
        }
    }

    @Override
    protected void onStopLoading() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStopLoading");
        super.onStopLoading();

        cancelLoad();
    }

    @Override
    protected void onReset() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onReset");
        super.onReset();

        onStopLoading();
        mData = null;
    }
}
