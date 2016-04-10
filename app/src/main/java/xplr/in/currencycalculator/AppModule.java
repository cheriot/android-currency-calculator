package xplr.in.currencycalculator;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    @Provides @Singleton App provideApplication() {
        return app;
    }

    @Provides @Singleton CurrencySource provideCurrencySource() {
        return new HttpCurrencySource();
    }

    @Provides @Singleton SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
