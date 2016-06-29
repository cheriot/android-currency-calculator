package xplr.in.currencycalculator.loaders;

import android.content.Context;

import xplr.in.currencycalculator.presenters.ComparisonPresenter;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 5/31/16.
 */
public class RateComparisonLoader extends WorkingAsyncTaskLoader<ComparisonPresenter> {

    private final CurrencyRepository currencyRepository;

    public RateComparisonLoader(Context context, CurrencyRepository currencyRepository) {
        super(context);
        this.currencyRepository = currencyRepository;
    }

    @Override
    protected void releaseResources(ComparisonPresenter data) {
        // nothing to do
    }

    @Override
    public ComparisonPresenter loadInBackground() {
        return new ComparisonPresenter(
                currencyRepository.findBaseMoney(),
                currencyRepository.findTargetCurrency());
    }
}
