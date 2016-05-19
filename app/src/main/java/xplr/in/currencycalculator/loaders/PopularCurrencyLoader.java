package xplr.in.currencycalculator.loaders;

import android.content.Context;

import java.util.List;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 5/19/16.
 */
public class PopularCurrencyLoader extends WorkingAsyncTaskLoader<List<Currency>>  {

    private final CurrencyRepository currencyRepository;

    public PopularCurrencyLoader(Context context, CurrencyRepository currencyRepository) {
        super(context);
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> loadInBackground() {
        return currencyRepository.findPopularCurrencies();
    }

    @Override
    protected void releaseResources(List<Currency> data) {
        // nothing required
    }
}
