package xplr.in.currencycalculator.sources;

import android.app.Application;

import java.io.InputStream;
import java.util.List;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.CurrencyMeta;

/**
 * Created by cheriot on 4/14/16.
 */
public class CurrencyMetaSource extends ResRawSource {

    public CurrencyMetaSource(Application context) {
        super(context);
    }

    public List<CurrencyMeta> get() {
        InputStream inputStream = super.getInputStream(R.raw.currencies_meta);
        return new CurrencyMetaParser().parse(inputStream);
    }
}
