package xplr.in.currencycalculator.repositories;

import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.orm.SugarRecord;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.sources.CurrencySource;

/**
 * Created by cheriot on 4/1/16.
 */
public class CurrencyRepository {

    private static final String LOG_TAG = CurrencyRepository.class.getCanonicalName();

    @Inject
    private CurrencySource currencySource;
    @Inject
    private EventBus eventBus;

    @Inject
    public CurrencyRepository(CurrencySource currencySource, EventBus eventBus) {
        this.currencySource = currencySource;
        this.eventBus = eventBus;
    }

    public List<Currency> fetchAll() {
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
        publishDataChange();
        return currencies;
    }

    public Cursor getSelectedCursor() {
        return SugarRecord.getCursor(Currency.class, "selected = 1", null, null, null, null);
    }

    public Cursor getAllCursor() {
        return SugarRecord.getCursor(Currency.class, null, null, null, null, null);
    }

    public Currency updateSelection(int id, boolean isSelected) {
        Currency currency = SugarRecord.findById(Currency.class, id);
        currency.setSelected(isSelected);
        if (isSelected) {
            insertAtPosition(1, currency);
        } else {
            currency.setPosition(null);
        }
        SugarRecord.update(currency);
        Log.v(LOG_TAG, "updateSelection " + currency.toString());
        publishDataChange();
        return currency;
    }

    private static final String SHIFT_LIST_SQL = "update currency set position = position";
    public void insertAtPosition(int newPosition, Currency currency) {
        Integer startPos = currency.getPosition();

        if (startPos != null) {
            StringBuilder sql = new StringBuilder(SHIFT_LIST_SQL);
            if(startPos < newPosition) {
                // Move down
                // startPosition 2, newPosition 5, first shift 3, 4, 5 up by one
                sql.append("-1 where position <= ? and position > ?");
            } else if (startPos > newPosition) {
                // Move up
                // startPosition 5, newPosition 2, first shift 2, 3, 4 down by one
                sql.append("+1 where position >= ? and position < ?");
            }
            SugarRecord.executeQuery(
                    sql.toString(), Integer.toString(newPosition), startPos.toString());
        } else {
            // Initial insert
            // newPosition 3, first shift 3, 4, 5, etc down by one
            String sql = SHIFT_LIST_SQL + "+1 where position >= ?";
            SugarRecord.executeQuery(sql, Integer.toString(newPosition));
        }

        currency.setPosition(newPosition);
        SugarRecord.update(currency);
    }

    public Currency findByCode(String code) {
        List<Currency> list = SugarRecord.find(Currency.class, "code = ?", code);
        return list.isEmpty() ? null : list.get(0);
    }

    private void publishDataChange() {
        Log.v(LOG_TAG, "publishDataChange");
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
                c.update(r.getCode(), r.getRate());
                SugarRecord.save(c);
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
        private String Name;
        private String Rate;

        public boolean isValid() {
            return id.length() == 6 && id.startsWith("USD")
                    && !"N/A".equals(Rate)
                    && !"N/A".equals(Name);
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
                    ", Name='" + Name + '\'' +
                    ", Rate='" + Rate + '\'' +
                    '}';
        }
    }

    public static class InvalidRateException extends RuntimeException {
        public InvalidRateException(RateResponse r) {
            super("Invalid rate response "+r.toString());
        }
        public InvalidRateException(RateResponse r, Exception e) {
            super("Invalid rate response "+r.toString(), e);
        }
    }
}
