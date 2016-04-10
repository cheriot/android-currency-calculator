package xplr.in.currencycalculator;

import javax.inject.Singleton;

import dagger.Component;
import xplr.in.currencycalculator.activities.MainActivity;
import xplr.in.currencycalculator.activities.SelectCurrencyActivity;
import xplr.in.currencycalculator.sync.CurrencySyncAdapter;

/**
 * Created by cheriot on 4/10/16.
 */
@Singleton
@Component(modules=AppModule.class)
public interface AppComponent {

    void inject(App app);

    void inject(MainActivity activity);

    void inject(SelectCurrencyActivity selectCurrencyActivity);

    void inject(CurrencySyncAdapter currencySyncAdapter);
}
