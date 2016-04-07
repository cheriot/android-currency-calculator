package xplr.in.currencycalculator.loaders;

import android.database.Cursor;

import xplr.in.currencycalculator.CurrencyListActivity;

/**
 * Created by cheriot on 4/6/16.
 */
public class SelectedCurrencyLoader extends AbstractCurrencyLoader {
    private static String LOG_TAG = SelectedCurrencyLoader.class.getCanonicalName();

    public SelectedCurrencyLoader(CurrencyListActivity activity) {
        super(activity);
    }

    @Override
    public Cursor loadInBackground() {
        return getActivity().getCurrencyRepository().getSelectedCursor();
    }
}
