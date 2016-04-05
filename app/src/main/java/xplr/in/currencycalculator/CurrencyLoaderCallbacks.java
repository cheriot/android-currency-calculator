package xplr.in.currencycalculator;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;

import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static int LOADER_ID = 0;
    private Context context;
    private CurrencyRepository currencyRepository;
    private CursorAdapter currenciesAdapter;

    public CurrencyLoaderCallbacks(Context context, CurrencyRepository currencyRepository, CursorAdapter currenciesAdapter) {
        this.context = context;
        this.currencyRepository = currencyRepository;
        this.currenciesAdapter = currenciesAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CurrencyLoader(context, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        currenciesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        currenciesAdapter.swapCursor(null);
    }
}
