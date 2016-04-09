package xplr.in.currencycalculator;

import xplr.in.currencycalculator.databases.CurrenciesDatabase;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CurrenciesDatabase.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        CurrenciesDatabase.getInstance().close();
    }

}
