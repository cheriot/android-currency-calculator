package xplr.in.currencycalculator.repositories;

import android.telephony.TelephonyManager;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyMeta;

/**
 * Created by cheriot on 5/20/16.
 */
@Singleton
public class PopularCurrenciesRepository {
    private static final String LOG_TAG = PopularCurrenciesRepository.class.getSimpleName();

    private final CurrencyRepository currencyRepository;
    private final CurrencyMetaRepository metaRepository;
    private final TelephonyManager telephonyManager;

    @Inject
    public PopularCurrenciesRepository(
            CurrencyRepository currencyRepository,
            CurrencyMetaRepository metaRepository,
            TelephonyManager telephonyManager) {
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
        this.telephonyManager = telephonyManager;
    }

    public Currency findSimCurrency() {
        return findByCountryCode(telephonyManager.getSimCountryIso());
    }

    public Currency findNetworkCurrency() {
        return findByCountryCode(telephonyManager.getNetworkCountryIso());
    }

    public Currency findLocaleCurrency() {
        return findByCountryCode(Locale.getDefault().getCountry());
    }

    private Currency findByCountryCode(String countryCode) {
        if(countryCode == null) return null;
        CurrencyMeta meta = metaRepository.findByCountryCode(countryCode);
        if(meta == null) return null;
        return currencyRepository.findByCode(meta.getCode());
    }

    public List<Currency> findPopularCurrencies() {
        LinkedHashSet<Currency> popular = new LinkedHashSet<>();
        popular.add(findNetworkCurrency());
        popular.add(findSimCurrency());
        popular.add(findLocaleCurrency());
        popular.remove(null); // any of the above can be null

        // https://en.wikipedia.org/wiki/World_Tourism_rankings
        popular.add(currencyRepository.findByCode("EUR"));
        popular.add(currencyRepository.findByCode("CNY"));
        popular.add(currencyRepository.findByCode("TRY"));
        popular.add(currencyRepository.findByCode("GBP"));
        popular.add(currencyRepository.findByCode("RUB"));
        popular.add(currencyRepository.findByCode("MXN"));

        popular.add(currencyRepository.findByCode("HKD")); // 27.8
        popular.add(currencyRepository.findByCode("MYR")); // 27.4
        popular.add(currencyRepository.findByCode("THB")); // 24.8
//        popular.add(currencyRepository.findByCode("CAD")); // 16.5
//        popular.add(currencyRepository.findByCode("SGD")); // 15
//        popular.add(currencyRepository.findByCode("SAR")); // 15
//        popular.add(currencyRepository.findByCode("MOP")); // 14.6
//        popular.add(currencyRepository.findByCode("KRW")); // 14.2
//        popular.add(currencyRepository.findByCode("JPY")); // 13.4
//        popular.add(currencyRepository.findByCode("TWD")); // 10.4
//        popular.add(currencyRepository.findByCode("MAD")); // 10.2
//        popular.add(currencyRepository.findByCode("EGP")); // 9.6
        return Arrays.asList(popular.toArray(new Currency[popular.size()]));
    }
}
