package xplr.in.currencycalculator.adapters;

import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 5/30/16.
 */
public interface CurrencySelectionChangeListener {
    void onCurrencySelectionChange(Currency currency, boolean isSelected);
}
