package xplr.in.currencycalculator.loaders;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import xplr.in.currencycalculator.activities.CurrencyListActivity;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static int LOADER_ID = 0;

    private CurrencyListActivity activity;
    private Class currencyLoaderClass;

    public CurrencyLoaderCallbacks(CurrencyListActivity activity, Class currencyLoaderClass) {
        this.activity = activity;
        this.currencyLoaderClass = currencyLoaderClass;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        try {
            return (Loader<Cursor>)currencyLoaderClass
                    .getDeclaredConstructor(CurrencyListActivity.class)
                    .newInstance(activity);
        }
        catch(Exception e) {
            throw new RuntimeException("Invalid currencyLoaderClass " + currencyLoaderClass, e);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        activity.getCurrencyCursorAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        activity.getCurrencyCursorAdapter().swapCursor(null);
    }
}
