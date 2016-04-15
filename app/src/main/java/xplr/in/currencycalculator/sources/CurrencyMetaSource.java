package xplr.in.currencycalculator.sources;

import android.app.Application;

import java.io.InputStream;

import javax.inject.Inject;

import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 4/14/16.
 */
public class CurrencyMetaSource extends ResRawSource {

    @Inject
    public CurrencyMetaSource(Application context) {
        super(context);
    }

    public InputStream get() {
        return super.getInputStream(R.raw.currencies_meta);
    }
}
