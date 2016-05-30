package xplr.in.currencycalculator.loaders;

import android.content.Context;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.presenters.SelectableCurrencies;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.repositories.PopularCurrenciesRepository;

/**
 * Created by cheriot on 5/30/16.
 */
public class SelectableCurrenciesLoader extends WorkingAsyncTaskLoader<SelectableCurrencies> {

    private final CurrencyRepository currencyRepository;
    private final PopularCurrenciesRepository popularCurrenciesRepository;

    private String searchQuery;

    public SelectableCurrenciesLoader(Context context,
                                      PopularCurrenciesRepository popularCurrenciesRepository,
                                      CurrencyRepository currencyRepository,
                                      String searchQuery) {
        super(context);
        this.popularCurrenciesRepository = popularCurrenciesRepository;
        this.currencyRepository = currencyRepository;
        this.searchQuery = searchQuery;
    }

    @Override
    protected void releaseResources(SelectableCurrencies data) {
        // close cursor?
    }

    @Override
    public SelectableCurrencies loadInBackground() {
        boolean isSearching = !TextUtils.isEmpty(searchQuery);
        List<Currency> popular = isSearching ?
                Collections.<Currency>emptyList() : popularCurrenciesRepository.findPopularCurrencies();
        return new SelectableCurrencies(popular, currencyRepository.searchAllCursor(searchQuery), isSearching);
    }
}
