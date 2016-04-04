package xplr.in.currencycalculator.sources;

import com.google.inject.ImplementedBy;

/**
 * Created by cheriot on 4/4/16.
 */
@ImplementedBy(HttpCurrencySource.class)
public interface CurrencySource {
    String get();
}
