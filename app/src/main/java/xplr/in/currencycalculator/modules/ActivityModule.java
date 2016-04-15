package xplr.in.currencycalculator.modules;

import android.app.Activity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;

/**
 * Created by cheriot on 4/14/16.
 */
@Module
public class ActivityModule {

    private Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides @ActivityScope @Named("calculate")
    public CurrencyCursorAdapter provideCalculateCurrencyCursorAdapter() {
        return new CurrencyCursorAdapter(activity, R.layout.list_item_currency_calculation);
    }

    @Provides @ActivityScope @Named("select")
    public CurrencyCursorAdapter provideSelectCurrencyCursorAdapter() {
        return new CurrencyCursorAdapter(activity, R.layout.list_item_selectable_currency);
    }
}
