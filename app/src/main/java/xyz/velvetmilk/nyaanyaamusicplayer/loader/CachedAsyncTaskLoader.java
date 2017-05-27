package xyz.velvetmilk.nyaanyaamusicplayer.loader;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by nydrani on 27/05/17.
 */

public abstract class CachedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {
    private D mData;

    public CachedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
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
        this.mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        this.mData = null;
    }
}
