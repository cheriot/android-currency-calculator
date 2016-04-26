package xplr.in.currencycalculator.loaders;

import android.database.Cursor;
import android.support.annotation.Keep;

import xplr.in.currencycalculator.activities.CurrencyListActivity;

/**
 * Created by cheriot on 4/6/16.
 */
public class SelectedCurrencyLoader extends AbstractCurrencyLoader {
    private static String LOG_TAG = SelectedCurrencyLoader.class.getCanonicalName();

    @Keep
    public SelectedCurrencyLoader(CurrencyListActivity activity) {
        super(activity);
    }

    @Override
    public Cursor loadInBackground() {
        return getActivity().getCurrencyRepository().findSelectedCursor();
    }
}
