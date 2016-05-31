package xplr.in.currencycalculator.loaders;

import android.content.Context;

import xplr.in.currencycalculator.presenters.RateComparison;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 5/31/16.
 */
public class RateComparisonLoader extends WorkingAsyncTaskLoader<RateComparison> {

    private final CurrencyRepository currencyRepository;

    public RateComparisonLoader(Context context, CurrencyRepository currencyRepository) {
        super(context);
        this.currencyRepository = currencyRepository;
    }

    @Override
    protected void releaseResources(RateComparison data) {
        // nothing to do
    }

    @Override
    public RateComparison loadInBackground() {
        return new RateComparison(
                currencyRepository.findBaseCurrency(),
                currencyRepository.findTargetCurrency());
    }
}
