package xplr.in.currencycalculator.sources;

import android.app.Application;

import javax.inject.Inject;

import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 4/12/16.
 */
public class ResRawRateSource extends ResRawSource implements RateSource {

    @Inject
    public ResRawRateSource(Application context) {
        super(context);
    }

    @Override
    public String get() {
        return super.get(R.raw.currencies_response);
    }
}
