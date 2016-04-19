package xplr.in.currencycalculator.repositories;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;
import com.yahoo.squidb.sql.Function;
import com.yahoo.squidb.sql.Query;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import static android.text.TextUtils.isEmpty;

/**
 * Created by cheriot on 4/1/16.
 */
@Singleton
public class CurrencyRepository {

    private static final String LOG_TAG = CurrencyRepository.class.getSimpleName();
    private static final int BASE_CURRENCY_POSITION = 1;

    private final SharedPreferences appSharedPrefs;
    private final RateSource localRateSource;
    private final RateSource remoteRateSource;
    private final CurrencyMetaRepository metaRepository;
    private final CurrenciesDatabase database;
    private final EventBus eventBus;

    @Inject
    public CurrencyRepository(SharedPreferences appSharedPrefs,
                              @Named("local")RateSource localRateSource,
                              @Named("remote")RateSource remoteRateSource,
                              CurrencyMetaRepository metaRepository,
                              CurrenciesDatabase database,
                              EventBus eventBus) {
        this.appSharedPrefs = appSharedPrefs;
        this.localRateSource = localRateSource;
        this.remoteRateSource = remoteRateSource;
        this.metaRepository = metaRepository;
        this.database = database;
        this.eventBus = eventBus;
    }

    public void updateFromRemote() {
        Log.v(LOG_TAG, "updateFromRemote");
        update(remoteRateSource);
        this.eventBus.post(new SyncCompleteEvent());
    }

    public void initializeData() {
        Log.v(LOG_TAG, "initializeData");
        updateOrInitializeMeta();
        update(localRateSource);
        // This initial state really needs to be customized for the user's locale.
        SelectedCurrency baseCurrency = findByCode(SelectedCurrency.class, "USD");
        // when initializing to a user specific base currency, convert $1 to that currency and round
        baseCurrency.setAmount("1");
        setBaseCurrency(baseCurrency);
        insertAtPosition(2, findByCode("EUR"));
        insertAtPosition(3, findByCode("MXN"));
        insertAtPosition(4, findByCode("CNY"));
        insertAtPosition(5, findByCode("TRY"));
        insertAtPosition(5, findByCode("RUB"));
    }

    private void update(RateSource rateSource) {
        List<CurrencyRate> rates = rates(rateSource);
        if(rates == null) return;
        Log.v(LOG_TAG, "Updating "+rates.size()+" rates.");

        for(CurrencyRate r : rates) {
            Currency c = findByCode(r.getCode());
            if(c == null) continue;
            c.setRate(r);
            if(r.getRate() == BigDecimal.ZERO) {
                deselectCurrency(c);
            }
            database.persist(c);
        }
        Log.v(LOG_TAG, "Updated currencies.");

        publishDataChange("update");
    }

    private List<CurrencyRate> rates(RateSource rateSource) {
        String json = rateSource.get();
        if(json == null) return null;
        Log.v(LOG_TAG, "Updating from json " + json.length() + " " + json);
        return new CurrencyRateParser().parse(json);
    }

    public void updateOrInitializeMeta() {
        Log.v(LOG_TAG, "updateOrInitializeMeta");
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

    static final Query INVALID_CURRENCIES = Query
            .select()
            .from(Currency.TABLE)
            .where(Currency.RATE.eq("0"))
            .freeze();

    static final Query VALID_CURRENCIES = Query
            .select()
            .from(Currency.TABLE)
            .where(Currency.RATE.isNot("0"))
            .freeze();

    static final Query SELECTED_CURRENCIES = VALID_CURRENCIES
            .where(Currency.POSITION.isNotNull())
            .orderBy(Currency.POSITION.asc())
            .freeze();

    static final Query CALCULATED_CURRENCIES = SELECTED_CURRENCIES
            .where(Currency.POSITION.gt(BASE_CURRENCY_POSITION))
            .freeze();

    static final Query BASE_CURRENCY = SELECTED_CURRENCIES
            .where(Currency.POSITION.eq(BASE_CURRENCY_POSITION))
            .freeze();

    public Cursor findSelectedCursor() {
        return database.query(Currency.class, CALCULATED_CURRENCIES);
    }

    private static final Query SEARCH_CURRENCIES = VALID_CURRENCIES.orderBy(Currency.NAME.asc()).freeze();
    public Cursor searchAllCursor(String query) {
        Log.v(LOG_TAG, "searchAllCursor for "+ query);
        if(query != null) query = query.trim();

        Query search = SEARCH_CURRENCIES.fork();
        if(!isEmpty(query)) {
            String fieldPrefix = query + "%";
            String wordPrefix = "% " + query + "%";
            search.where(
                    Currency.CODE.like(fieldPrefix)
                            .or(Function.lower(Currency.NAME).like(fieldPrefix.toLowerCase()))
                            .or(Function.lower(Currency.NAME).like(wordPrefix.toLowerCase())));
        }
        return database.query(Currency.class, search);
    }

    public Currency updateSelection(long id, boolean isSelected) {
        Currency currency = database.fetch(Currency.class, id, Currency.PROPERTIES);
        if (isSelected) {
            insertAtPosition(2, currency);
        } else {
            deselectCurrency(currency);
            publishDataChange("remove selected");
        }
        Log.v(LOG_TAG, "updateSelection " + isSelected + " " + currency.toString());
        return currency;
    }

    private void deselectCurrency(Currency currency) {
        if(currency.getPosition() == null) return;
        // Move currencies below this one up before removing it from the list.
        insertAtPosition(BASE_CURRENCY_POSITION-1, currency);
        currency.setPosition(null);
        database.persist(currency);
    }

    private static final String SHIFT_LIST_SQL = "update currencies set position = position";
    public void insertAtPosition(int newPosition, Currency currency) {
        Integer startPos = currency.getPosition();

        if (startPos != null) {
            StringBuilder sql = new StringBuilder(SHIFT_LIST_SQL);
            String[] args = new String[] {Integer.toString(newPosition), startPos.toString()};
            Criterion where;
            if(startPos < newPosition) {
                // Move down
                // startPosition 2, newPosition 5, first shift 3, 4, 5 up by one
                sql.append("-1 where position <= ? and position > ?");
                where = Currency.POSITION.lte(newPosition).and(Currency.POSITION.gt(startPos));
            } else if (startPos > newPosition) {
                // Move up
                // startPosition 5, newPosition 2, first shift 2, 3, 4 down by one
                sql.append("+1 where position >= ? and position < ?");
                where = Currency.POSITION.gte(newPosition).and(Currency.POSITION.lt(startPos));
            }
            database.tryExecSql(sql.toString(), args);
        } else {
            // Initial insert
            // newPosition 3, first shift 3, 4, 5, etc down by one
            String sql = SHIFT_LIST_SQL + "+1 where position >= ?";
            database.tryExecSql(sql.toString(), new String[] {Integer.toString(newPosition)});
        }

        currency.setPosition(newPosition);
        database.persist(currency);

        Log.v(LOG_TAG, "insertAtPosition  " + newPosition + " " + currency.toString());
        publishDataChange("insertAtPosition");
    }

    private static final String BASE_CURRENCY_AMOUNT_KEY = "base_currency_amount";
    public SelectedCurrency getBaseCurrency() {
        SelectedCurrency baseCurrency = database.fetchByQuery(SelectedCurrency.class, BASE_CURRENCY);
        baseCurrency.setAmount(appSharedPrefs.getString(BASE_CURRENCY_AMOUNT_KEY, null));
        return baseCurrency;
    }

    public void setBaseCurrency(SelectedCurrency currency) {
        setBaseAmount(currency, currency.getAmount());
        insertAtPosition(1, currency);
    }

    public void setBaseAmount(SelectedCurrency baseCurrency, String amount) {
        baseCurrency.setAmount(amount);
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putString(BASE_CURRENCY_AMOUNT_KEY, amount);
        editor.apply();
    }

    public Currency findByCode(String code) {
        return findByCode(Currency.class, code);
    }

    <T extends Currency> T findByCode(Class<T> modelClass, String code) {
        Query query = VALID_CURRENCIES.where(Currency.CODE.eq(code));
        return database.fetchByQuery(modelClass, query);
    }

    private void publishDataChange(String sourceName) {
        // TODO Can this be triggered automatically by squidb's SimpleDataChangedNotifier?
        Log.v(LOG_TAG, "publishDataChange " + sourceName);
        this.eventBus.post(new CurrencyDataChangeEvent());
    }

    private static List<CurrencyRate> parseCurrencyJson(String json) {
        return new CurrencyRateParser().parse(json);
    }

    private Currency findOrInstantiate(String code) {
        Currency currency = findByCode(code);
        if(currency == null) {
            currency = new Currency();
            currency.setCode(code);
        }
        return currency;
    }
}
