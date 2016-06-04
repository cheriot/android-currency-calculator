package xplr.in.currencycalculator.modules;

import dagger.Subcomponent;
import xplr.in.currencycalculator.activities.MainActivity;
import xplr.in.currencycalculator.activities.OfferComparisonActivity;
import xplr.in.currencycalculator.activities.RateComparisonActivity;
import xplr.in.currencycalculator.activities.SelectCurrencyActivity;

/**
 * Created by cheriot on 4/14/16.
 */
@ActivityScope
@Subcomponent(modules={ActivityModule.class})
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(SelectCurrencyActivity selectCurrencyActivity);

    void inject(RateComparisonActivity rateComparisonActivity);

    void inject(OfferComparisonActivity offerComparisonActivity);
}
