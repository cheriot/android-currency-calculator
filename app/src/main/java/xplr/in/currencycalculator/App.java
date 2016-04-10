package xplr.in.currencycalculator;

import javax.inject.Inject;

import xplr.in.currencycalculator.activities.MainActivity;
import xplr.in.currencycalculator.databases.CurrenciesDatabase;
import xplr.in.currencycalculator.sync.CurrencySyncAdapter;

public class App extends android.app.Application {

    private AppComponent appComponent;
    @Inject CurrenciesDatabase currenciesDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();
        appComponent.inject(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public void inject(MainActivity activity) {
        appComponent.inject(activity);
    }

    public void inject(CurrencySyncAdapter activity) {
        appComponent.inject(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(currenciesDatabase != null) currenciesDatabase.close();
    }

}
