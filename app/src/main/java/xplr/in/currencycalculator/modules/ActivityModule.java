package xplr.in.currencycalculator.modules;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import xplr.in.currencycalculator.adapters.CurrencySelectionChangeListener;
import xplr.in.currencycalculator.adapters.SelectCurrencyCombinedAdapter;
import xplr.in.currencycalculator.adapters.SelectedCurrencyAdapter;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/14/16.
 */
@Module
public class ActivityModule {

    private static final String LOG_TAG = ActivityModule.class.getSimpleName();
    private Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides public Activity provideActivity() {
        return this.activity;
    }

    @Provides public Context provideContext() { return this.activity; }

    @Provides @ActivityScope
    public SelectedCurrencyAdapter provideCalculateCurrencyCursorAdapter(CurrencyRepository currencyRepository, CurrencyMetaRepository currencyMetaRepository) {
        return new SelectedCurrencyAdapter(currencyRepository, currencyMetaRepository);
    }

    @Provides @ActivityScope
    public SelectCurrencyCombinedAdapter provideSelectCurrencyCursorAdapter() {
        return new SelectCurrencyCombinedAdapter((CurrencySelectionChangeListener)activity);
    }
}
