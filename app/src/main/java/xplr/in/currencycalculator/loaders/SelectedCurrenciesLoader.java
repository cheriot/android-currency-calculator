package xplr.in.currencycalculator.loaders;

import android.database.Cursor;
import android.support.annotation.Keep;

import xplr.in.currencycalculator.activities.CurrencyListActivity;

/**
 * Created by cheriot on 4/6/16.
 */
public class SelectedCurrenciesLoader extends AbstractCurrencyLoader {
    private static String LOG_TAG = SelectedCurrenciesLoader.class.getCanonicalName();

    @Keep
    public SelectedCurrenciesLoader(CurrencyListActivity activity) {
        super(activity);
    }

    @Override
    public Cursor loadInBackground() {
        return getActivity().getCurrencyRepository().findSelectedCursor();
    }
}
