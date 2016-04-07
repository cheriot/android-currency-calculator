package xplr.in.currencycalculator.loaders;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;

import xplr.in.currencycalculator.CurrencyListActivity;
import xplr.in.currencycalculator.repositories.CurrencyDataChangeEvent;

/**
 * Created by cheriot on 4/5/16.
 */
public abstract class AbstractCurrencyLoader extends WorkingAsyncTaskLoader<Cursor> {

    private static String LOG_TAG = AbstractCurrencyLoader.class.getCanonicalName();
    private CurrencyListActivity activity;

    public AbstractCurrencyLoader(CurrencyListActivity activity) {
        super((Context)activity);
        this.activity = activity;
    }

    @Override
    protected void registerObserver() {
        Log.v(LOG_TAG, "registerObserver " + getClass().getName());
        if(!getActivity().getEventBus().isRegistered(this)) {
            getActivity().getEventBus().register(this);
        }
    }

    @Override
    protected void unregisterObserver() {
        Log.v(LOG_TAG, "unregisterObserver " + getClass().getName());
        this.activity.getEventBus().unregister(this);
    }

    @Subscribe
    public void onCurrencyDataChanged(CurrencyDataChangeEvent e) {
        Log.v(LOG_TAG, "Currency data change. Call forceLoad()");
        forceLoad();
    }

    @Override
    protected void releaseResources(Cursor data) {
        if(!data.isClosed()) data.close();
    }

    public CurrencyListActivity getActivity() {
        return activity;
    }
}
