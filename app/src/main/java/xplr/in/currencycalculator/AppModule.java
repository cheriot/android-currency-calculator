package xplr.in.currencycalculator;


import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import xplr.in.currencycalculator.sources.CurrencySource;
import xplr.in.currencycalculator.sources.HttpCurrencySource;


/**
 * Dagger2 module.
 */
@Module
public class AppModule {

    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides App provideApplication() {
        return app;
    }

    @Provides CurrencySource currencySource() {
        return new HttpCurrencySource();
    }
}
