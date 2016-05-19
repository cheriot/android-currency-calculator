package xplr.in.currencycalculator.repositories;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.yahoo.squidb.data.TableModel;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.CurrencyRate;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.sources.CurrencyRateParser;
import xplr.in.currencycalculator.sources.RateSource;
import xplr.in.currencycalculator.sync.SyncCompleteEvent;

/**
 * Currency data manipulation for app creation, init, and sync.
 *
 * Created by cheriot on 4/19/16.
 */
@Singleton
public class CurrencyBulkRepository {

    private static final String LOG_TAG = CurrencyBulkRepository.class.getSimpleName();

    private final CurrenciesDatabase database;
    private final CurrencyRepository currencyRepository;
    private final CurrencyMetaRepository metaRepository;
    private final TelephonyManager telephonyManager;
    private final RateSource localRateSource;
    private final RateSource remoteRateSource;
    private final EventBus eventBus;

    @Inject
    public CurrencyBulkRepository(CurrenciesDatabase database,
                                  CurrencyRepository currencyRepository,
                                  CurrencyMetaRepository metaRepository,
                                  @Named("local")RateSource localRateSource,
                                  @Named("remote")RateSource remoteRateSource,
                                  TelephonyManager telephonyManager,
                                  EventBus eventBus) {
        this.database = database;
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
        this.telephonyManager = telephonyManager;
        this.localRateSource = localRateSource;
        this.remoteRateSource = remoteRateSource;
        this.eventBus = eventBus;
    }

    /**
     * Populate and empty database and default selections based on the information available.
     */
    public void initializeDefaultSelections() {
        Log.v(LOG_TAG, "initializeDefaultSelections");
        updateOrInitMeta();

        insertAtTop("TRY");
        insertAtTop("MXN");
        insertAtTop("GBP");
        insertAtTop("EUR");
        insertAtTop("USD");

        CurrencyMeta locale = metaRepository.findByCountryCode(Locale.getDefault().getCountry());
        CurrencyMeta sim = metaRepository.findByCountryCode(telephonyManager.getSimCountryIso());
        CurrencyMeta network = metaRepository.findByCountryCode(telephonyManager.getNetworkCountryIso());
        if(locale != null) insertAtTop(locale.getCode());
        if(sim != null) insertAtTop(sim.getCode());
        if(network != null) insertAtTop(network.getCode());
        Log.i(LOG_TAG, "Locale " + locale);
        Log.i(LOG_TAG, "SIM " + sim);
        Log.i(LOG_TAG, "Network " + network);

        // When initializing a base currency, convert $1 to that currency
        // and round to make it pretty.
        SelectedCurrency baseCurrency = currencyRepository.findBaseCurrency();
        SelectedCurrency usd = currencyRepository.findByCode(SelectedCurrency.class, "USD");
        usd.setAmount("10");
        String amount = baseCurrency.roundNumberCloseTo(usd);
        currencyRepository.setBaseAmount(baseCurrency, amount);
    }

    public void updateFromRemote() {
        Log.v(LOG_TAG, "updateFromRemote");
        update(remoteRateSource);
        this.eventBus.post(new SyncCompleteEvent());
    }

    private void update(RateSource rateSource) {
        List<CurrencyRate> rates = rates(rateSource);
        if(rates == null) return;
        Log.v(LOG_TAG, "Updating "+rates.size()+" rates.");

        for(CurrencyRate r : rates) {
            Currency c = currencyRepository.findByCode(r.getCode());
            if(c == null) continue;
            c.setRate(r);
            if(r.getRate() == BigDecimal.ZERO) {
                currencyRepository.deselectCurrency(c);
            }
            database.persist(c);
        }
        Log.v(LOG_TAG, "Updated currencies.");

        currencyRepository.publishDataChange("update");
    }

    private List<CurrencyRate> rates(RateSource rateSource) {
        String json = rateSource.get();
        if(json == null) return null;
        Log.v(LOG_TAG, "Updating from json length " + json.length());
        return new CurrencyRateParser().parse(json);
    }

    public void updateOrInitMeta() {
        // Insert all currencies we have metadata for. Delete any that have been removed.
        Log.v(LOG_TAG, "updateOrInitMeta");
        List<CurrencyRate> rates = rates(localRateSource);
        Collection<CurrencyMeta> metas = metaRepository.findAll();
        Collection<Long> updatedIds = new ArrayList<>(metas.size());
        for(CurrencyMeta meta : metas) {
            Currency c = findOrInstantiate(meta.getCode());
            c.setMeta(meta);
            if(c.getId() == TableModel.NO_ID) {
                // Only if the DB doesn't have a more up to date rate, set the one that shipped with
                // the app.
                CurrencyRate rate = findRate(rates, meta.getCode());
                if(rate != null) c.setRate(rate);
                else c.setRate("0");
            }
            database.persist(c);
            updatedIds.add(c.getId());
        }

        int delCount = database.deleteWhere(Currency.class, Currency.ID.notIn(updatedIds));
        Log.v(LOG_TAG, "Deleted " + delCount);
    }

    private CurrencyRate findRate(List<CurrencyRate> rates, String code) {
        for(CurrencyRate rate : rates) {
            if(rate.getCode().equals(code)) return rate;
        }
        return null;
    }

    private void insertAtTop(String currencyCode) {
        currencyRepository.insertAtPosition(1, currencyRepository.findByCode(currencyCode));
    }

    private Currency findOrInstantiate(String code) {
        Currency currency = currencyRepository.findByCode(code);
        if(currency == null) {
            currency = new Currency();
            currency.setCode(code);
        }
        return currency;
    }
}
