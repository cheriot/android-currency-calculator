package xplr.in.currencycalculator.modules;

import android.app.Activity;

import dagger.Module;

/**
 * Created by cheriot on 4/14/16.
 */
@Module
public class ActivityModule {

    private Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }
}
