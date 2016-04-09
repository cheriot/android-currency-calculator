package xplr.in.currencycalculator.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Subclassing AsyncTaskLoader directly doesn't work. Putting the needed code here for reuse.
 *
 * Based on the article
 * http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 * and the source of CursorLoader
 * https://github.com/android/platform_frameworks_support/blob/master/v4/java/android/support/v4/content/CursorLoader.java
 *
 * Created by cheriot on 4/5/16.
 */
public abstract class WorkingAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D currentData;

    public WorkingAsyncTaskLoader(Context context) {
        super(context);
    }

    protected abstract void releaseResources(D data);

    protected void registerObserver() {
        // Optionally overridden by subclass.
    }

    protected void unregisterObserver() {
        // Optionally overridden by subclass.
    }

    @Override
    public void deliverResult(D data) {
        if(isReset()) {
            releaseResources(data);
            return;
        }
        if(isStarted()) {
            super.deliverResult(data);
        }

        D oldData = currentData;
        currentData = data;
        if(oldData != null && oldData != currentData) {
            releaseResources(oldData);
        }
    }

    @Override
    public void onCanceled(D data) {
        if (data != null) {
            releaseResources(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if(currentData != null) {
            releaseResources(currentData);
            currentData = null;
        }

        unregisterObserver();
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onStartLoading() {
        if(currentData != null) {
            deliverResult(currentData);
        }

        registerObserver();

        if (takeContentChanged() || currentData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }
}
