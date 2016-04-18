package xplr.in.currencycalculator.loaders;

import android.database.Cursor;

import xplr.in.currencycalculator.activities.CurrencyListActivity;

/**
 * Created by cheriot on 4/6/16.
 */
public class AllCurrencyLoader extends AbstractCurrencyLoader {

    private String searchQuery;

    public AllCurrencyLoader(CurrencyListActivity activity, String searchQuery) {
        super(activity);
        this.searchQuery = searchQuery;
    }

    @Override
    public Cursor loadInBackground() {
        return getActivity().getCurrencyRepository().searchAllCursor(searchQuery);
    }
}