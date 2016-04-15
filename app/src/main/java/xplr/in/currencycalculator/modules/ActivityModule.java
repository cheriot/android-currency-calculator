package xplr.in.currencycalculator.modules;

import android.app.Activity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;

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
    public CurrencyCursorAdapter provideCalculateCurrencyCursorAdapter(CurrencyMetaRepository currencyMetaRepository) {
        return new CurrencyCursorAdapter(activity, R.layout.list_item_currency_calculation, currencyMetaRepository);
    }

    @Provides @ActivityScope @Named("select")
    public CurrencyCursorAdapter provideSelectCurrencyCursorAdapter(CurrencyMetaRepository currencyMetaRepository) {
        return new CurrencyCursorAdapter(activity, R.layout.list_item_selectable_currency, currencyMetaRepository);
    }
}
