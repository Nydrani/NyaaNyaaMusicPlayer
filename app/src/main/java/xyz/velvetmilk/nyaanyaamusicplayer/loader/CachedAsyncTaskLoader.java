package xyz.velvetmilk.nyaanyaamusicplayer.loader;


import android.content.Context;
import android.content.AsyncTaskLoader;
import android.util.Log;

import xyz.velvetmilk.nyaanyaamusicplayer.BuildConfig;

/**
 * Created by nydrani on 27/05/17.
 */

public abstract class CachedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {
    private static final String TAG = CachedAsyncTaskLoader.class.getSimpleName();
    private D mData;

    public CachedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartLoading");

        super.onStartLoading();
        if (this.mData != null) {
            deliverResult(this.mData);
        }
        if (takeContentChanged() || this.mData == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        if (BuildConfig.DEBUG) Log.d(TAG, "deliverResult");

        this.mData = data;
        if (isStarted()) {
            super.deliverResult(data);
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
        this.mData = null;
    }
}
