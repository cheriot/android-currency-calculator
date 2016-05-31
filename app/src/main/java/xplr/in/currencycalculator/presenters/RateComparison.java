package xplr.in.currencycalculator.presenters;

import xplr.in.currencycalculator.models.SelectedCurrency;

/**
 * Created by cheriot on 5/26/16.
 */
public class RateComparison {

    private final SelectedCurrency baseCurrency;
    private final SelectedCurrency targetCurrency;

    public RateComparison(SelectedCurrency baseCurrency, SelectedCurrency targetCurrency) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
    }

    public SelectedCurrency getBaseCurrency() {
        return baseCurrency;
    }

    public SelectedCurrency getTargetCurrency() {
        return targetCurrency;
    }
}
