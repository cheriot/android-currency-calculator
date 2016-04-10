package xplr.in.currencycalculator.repositories;

import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yahoo.squidb.sql.Criterion;
import com.yahoo.squidb.sql.Query;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.databases.CurrenciesDatabase;
import xplr.in.currencycalculator.databases.Currency;
import xplr.in.currencycalculator.databases.SelectedCurrency;
import xplr.in.currencycalculator.sources.CurrencySource;

/**
 * Created by cheriot on 4/1/16.
 */
@Singleton
public class CurrencyRepository {

    private static final String LOG_TAG = CurrencyRepository.class.getCanonicalName();

    @Inject CurrencySource currencySource;
    @Inject CurrenciesDatabase database;
    @Inject EventBus eventBus;

    @Inject
    public CurrencyRepository(CurrencySource currencySource, CurrenciesDatabase database, EventBus eventBus) {
        this.currencySource = currencySource;
        this.database = database;
        this.eventBus = eventBus;
    }

    public List<Currency> fetchAll() {
        Log.v(LOG_TAG, "fetchAll");

        String json = currencySource.get();
        if(json == null) {
            return Collections.emptyList();
        }
        Log.v(LOG_TAG, "Downloaded json "+json.substring(0, 200));

        List<RateResponse> rates = parseCurrencyJson(json);
        if(rates == null) {
            return Collections.emptyList();
        }
        Log.v(LOG_TAG, "Updated "+rates.size()+" currencies.");

        List<Currency> currencies = currencyFactory(rates);
        publishDataChange("fetchAll");
        return currencies;
    }

    static final Query ALL_CURRENCIES = Query
            .select()
            .from(Currency.TABLE)
            .freeze();

    static final Query SELECTED_CURRENCIES = ALL_CURRENCIES
            .where(Currency.POSITION.isNotNull())
            .orderBy(Currency.POSITION.asc())
            .freeze();

    static final Query CALCULATED_CURRENCIES = SELECTED_CURRENCIES
            .where(Currency.POSITION.gt(1))
            .freeze();

    static final Query BASE_CURRENCY = SELECTED_CURRENCIES
            .where(Currency.POSITION.eq(1))
            .freeze();

    public Cursor getSelectedCursor() {
        return database.query(Currency.class, CALCULATED_CURRENCIES);
    }

    public Cursor getAllCursor() {
        return database.query(Currency.class, ALL_CURRENCIES);
    }

    public Currency updateSelection(long id, boolean isSelected) {
        Currency currency = database.fetch(Currency.class, id, Currency.PROPERTIES);
        if (isSelected) {
            insertAtPosition(2, currency);
        } else {
            currency.setPosition(null);
            database.persist(currency);
            publishDataChange("remove selected");
        }
        Log.v(LOG_TAG, "updateSelection " + isSelected + " " + currency.toString());
        return currency;
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

    public SelectedCurrency getBaseCurrency() {
        return database.fetchByQuery(SelectedCurrency.class, BASE_CURRENCY);
    }

    public void setBaseCurrency(Currency currency) {
        insertAtPosition(1, currency);
    }

    public Currency findByCode(String code) {
        Query query = ALL_CURRENCIES.where(Currency.CODE.eq(code));
        return database.fetchByQuery(Currency.class, query);
    }

    private void publishDataChange(String sourceName) {
        Log.v(LOG_TAG, "publishDataChange " + sourceName);
        this.eventBus.post(new CurrencyDataChangeEvent());
    }

    private static List<RateResponse> parseCurrencyJson(String json) {
        try {
            Gson gson = new GsonBuilder().create();
            CurrencyResponse response = gson.fromJson(json, CurrencyResponse.class);
            return response.getQuery().getResults().getRate();
        }
        catch (JsonSyntaxException jse) {
            Log.e(LOG_TAG, "Error parsing new currency rates", jse);
            return null;
        }
    }

    private List<Currency> currencyFactory(List<RateResponse> rates) {
        List<Currency> currencies = new ArrayList<Currency>(rates.size());
        for(RateResponse r : rates) {
            if(r.isValid()) {
                Currency c = findOrInstantiate(r.getCode());
                c.setCode(r.getCode());
                c.setRate(r.getRate().toString());
                database.persist(c);
                currencies.add(c);
            }
        }
        return currencies;
    }

    private Currency findOrInstantiate(String code) {
        Currency currency = findByCode(code);
        return currency != null ? currency : new Currency();
    }

    private static class CurrencyResponse {
        private QueryResponse query;

        public QueryResponse getQuery() { return query; }
    }

    private static class QueryResponse {
        private int count;
        private Date created;
        private ResultsResponse results;

        public ResultsResponse getResults() { return results; }
    }

    private static class ResultsResponse {
        private List<RateResponse> rate;

        public List<RateResponse> getRate() {
            return rate;
        }
    }

    private static class RateResponse {
        private String id;
        private String Rate;

        public boolean isValid() {
            return id.length() == 6 && id.startsWith("USD")
                    && !"N/A".equals(Rate);
        }

        public String getCode() {
            return id.substring(3);
        }

        public BigDecimal getRate() {
            if(!isValid()) throw new InvalidRateException(this);
            return new BigDecimal(Rate);
        }

        @Override
        public String toString() {
            return "RateResponse{" +
                    "id='" + id + '\'' +
                    ", Rate='" + Rate + '\'' +
                    '}';
        }
    }

    public static class InvalidRateException extends RuntimeException {
        public InvalidRateException(RateResponse r) {
            super("Invalid rate response "+r.toString());
        }
    }
}
