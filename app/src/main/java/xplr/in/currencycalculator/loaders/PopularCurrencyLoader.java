package xplr.in.currencycalculator.loaders;

import android.content.Context;

import java.util.List;

import javax.inject.Inject;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.modules.ActivityScope;
import xplr.in.currencycalculator.repositories.PopularCurrenciesRepository;

/**
 * Created by cheriot on 5/19/16.
 */
@ActivityScope
public class PopularCurrencyLoader extends WorkingAsyncTaskLoader<List<Currency>>  {

    private final PopularCurrenciesRepository popularCurrenciesRepository;

    @Inject
    public PopularCurrencyLoader(Context context, PopularCurrenciesRepository popularCurrenciesRepository) {
        super(context);
        this.popularCurrenciesRepository = popularCurrenciesRepository;
    }

    @Override
    public List<Currency> loadInBackground() {
        return popularCurrenciesRepository.findPopularCurrencies();
    }

    @Override
    protected void releaseResources(List<Currency> data) {
        // nothing required
    }
}
