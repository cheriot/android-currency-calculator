package xplr.in.currencycalculator.sources;

import android.app.Application;

import javax.inject.Inject;

import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 4/12/16.
 */
public class ResRawCurrencySource extends ResRawSource implements CurrencySource {

    @Inject
    public ResRawCurrencySource(Application context) {
        super(context);
    }

    @Override
    public String get() {
        return super.get(R.raw.currencies_response);
    }
}
