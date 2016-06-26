package xplr.in.currencycalculator.modules;


import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.repositories.CurrencyBulkRepository;
import xplr.in.currencycalculator.sources.HttpRateSource;
import xplr.in.currencycalculator.sources.RateSource;
import xplr.in.currencycalculator.sources.ResRawRateSource;
import xplr.in.currencycalculator.sync.CurrencySyncAdapter;


/**
 * Dagger2 module.
 *
 * Most of these @Singleton's can be @Resusable when it becomes available. Just not EventBus.
 */
@Module
public class AppModule {

    // Changes to package structure and class names will change the key generated by default. Be
    // specific to avoid "loosing" preferences by accident later.
    private static final String APP_SHARED_PREFERENCES_KEY = "xplr.in.currencycalculator.app_preferences";

    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton
    App provideApplication() {
        return app;
    }

    @Provides @Singleton
    EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides @Singleton SharedPreferences provideAppSharedPrefs() {
        return app.getSharedPreferences(APP_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    @Provides @Singleton @Named("remote")
    RateSource provideRemoteRateSource() {
        return new HttpRateSource();
    }

    @Provides @Singleton @Named("local")
    RateSource provideLocalRateSource() {
        return new ResRawRateSource(app);
    }

    @Provides
    CurrencySyncAdapter providesCurrencySyncAdapter(CurrencyBulkRepository currencyBulkRepository) {
        return new CurrencySyncAdapter(app, true, false, currencyBulkRepository);
    }

    @Provides
    TelephonyManager providesTelephonyManager() {
        return (TelephonyManager)app.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Provides
    Analytics providesAnalytics() {
        return new Analytics(app);
    }
}
