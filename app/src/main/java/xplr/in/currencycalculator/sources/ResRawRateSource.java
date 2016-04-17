package xplr.in.currencycalculator.sources;

import javax.inject.Inject;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 4/12/16.
 */
public class ResRawRateSource extends ResRawSource implements RateSource {

    @Inject
    public ResRawRateSource(App context) {
        super(context);
    }

    @Override
    public String get() {
        return super.getString(R.raw.currencies_response);
    }
}
