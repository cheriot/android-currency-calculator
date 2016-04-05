package xplr.in.currencycalculator;

import android.content.Context;
import android.database.Cursor;

import xplr.in.currencycalculator.loaders.WorkingAsyncTaskLoader;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyLoader extends WorkingAsyncTaskLoader<Cursor> {

    private CurrencyRepository currencyRepository;

    public CurrencyLoader(Context context, CurrencyRepository currencyRepository) {
        super(context);
        this.currencyRepository = currencyRepository;
    }

    @Override
    protected void releaseResources(Cursor data) {
        if(!data.isClosed()) data.close();
    }

    @Override
    public Cursor loadInBackground() {
        return currencyRepository.getSelectedCursor();
    }
}
