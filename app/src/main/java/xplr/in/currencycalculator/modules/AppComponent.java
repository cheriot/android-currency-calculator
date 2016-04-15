package xplr.in.currencycalculator.modules;

import javax.inject.Singleton;

import dagger.Component;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.sync.CurrencySyncService;

/**
 * Created by cheriot on 4/10/16.
 */
@Singleton
@Component(modules=AppModule.class)
public interface AppComponent {

    void inject(App app);

    ActivityComponent newActivityComponent(ActivityModule activityModule);

    void inject(CurrencySyncService currencySyncService);
}
