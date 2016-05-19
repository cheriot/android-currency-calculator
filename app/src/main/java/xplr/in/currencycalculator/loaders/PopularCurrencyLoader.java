package xplr.in.currencycalculator.loaders;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
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
        // return getActivity().getCurrencyRepository().findPopularCurrencies();

        Currency a = new Currency();
        a.setCode("AAA");
        a.setName("A");
        a.setRate("1");
        a.setPosition(null);
        a.setMinorUnits(2);
        a.setIssuingCountryCode("aa");
        Currency b = new Currency();
        b.setCode("BBB");
        b.setName("B");
        b.setRate("2");
        b.setPosition(null);
        b.setMinorUnits(2);
        b.setIssuingCountryCode("bb");
        return new ArrayList<>(Arrays.asList(new Currency[] {a, b}));
    }

    @Override
    protected void releaseResources(List<Currency> data) {
        // nothing required
    }
}
