package xplr.in.currencycalculator;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.google.firebase.crash.FirebaseCrash;

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
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox()
                    .build());
        }
        appComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();
        appComponent.inject(this);
        
        new VerifyData().execute();

        /** Report FirebaseCrash Exception on production application crash. */
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                if (!BuildConfig.DEBUG) FirebaseCrash.report(e);
            }
        });
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

    private class VerifyData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            currenciesDatabase.verifyData();
            return null;
        }
    }
}
