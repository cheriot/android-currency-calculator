package xplr.in.currencycalculator.loaders;

import android.database.Cursor;

import xplr.in.currencycalculator.activities.CurrencyListActivity;

/**
 * Created by cheriot on 4/6/16.
 */
public class AllCurrencyLoader extends AbstractCurrencyLoader {
    private static String LOG_TAG = AllCurrencyLoader.class.getCanonicalName();

    public AllCurrencyLoader(CurrencyListActivity activity) {
        super(activity);
    }

    @Override
    public Cursor loadInBackground() {
        return getActivity().getCurrencyRepository().getAllCursor();
    }
}