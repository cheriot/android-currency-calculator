package xplr.in.currencycalculator;

import android.app.Activity;
import android.os.StrictMode;

import javax.inject.Inject;

import xplr.in.currencycalculator.modules.ActivityComponent;
import xplr.in.currencycalculator.modules.ActivityModule;
import xplr.in.currencycalculator.modules.AppComponent;
import xplr.in.currencycalculator.modules.AppModule;
import xplr.in.currencycalculator.modules.DaggerAppComponent;
import xplr.in.currencycalculator.repositories.CurrenciesDatabase;

public class App extends android.app.Application {

    // Could make this static so entry points have a more readable line.
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
        if(BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox() // What does this actually do?
                    .build());
        }
    }

    public ActivityComponent newActivityScope(Activity activity) {
        return appComponent.newActivityComponent(new ActivityModule(activity));
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
