package xplr.in.currencycalculator;

import javax.inject.Inject;

import xplr.in.currencycalculator.repositories.CurrenciesDatabase;

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

    @Override
    public void onTerminate() {
        super.onTerminate();
        currenciesDatabase.close();
    }

}
