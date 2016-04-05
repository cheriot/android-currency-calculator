package xplr.in.currencycalculator.repositories;

import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.orm.SugarRecord;

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
    CurrencySource currencySource;

    @Inject
    public CurrencyRepository(CurrencySource currencySource) {
        this.currencySource = currencySource;
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
        return currencies;
    }

    public Cursor getSelectedCursor() {
        return SugarRecord.getCursor(Currency.class, null, null, null, null, null);
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

    private static List<Currency> currencyFactory(List<RateResponse> rates) {
        List<Currency> currencies = new ArrayList<Currency>(rates.size());
        for(RateResponse r : rates) {
            if(r.isValid()) {
                // TODO update without fetch
                Currency c = findOrInstantiate("code = ?", r.getCode());
                c.update(r.getCode(), r.getRate());
                SugarRecord.save(c);
                currencies.add(c);
            }
        }
        return currencies;
    }

    private static Currency findOrInstantiate(String whereClause, String... args) {
        List<Currency> found = SugarRecord.find(Currency.class, whereClause, args);
        return found.size() > 0 ? found.get(0) : new Currency();
    }

    class CurrencyResponse {
        private QueryResponse query;

        public QueryResponse getQuery() { return query; }
    }

    class QueryResponse {
        private int count;
        private Date created;
        private ResultsResponse results;

        public ResultsResponse getResults() { return results; }
    }

    class ResultsResponse {
        private List<RateResponse> rate;

        public List<RateResponse> getRate() {
            return rate;
        }
    }

    public class RateResponse {
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

    public class InvalidRateException extends RuntimeException {
        public InvalidRateException(RateResponse r) {
            super("Invalid rate response "+r.toString());
        }
        public InvalidRateException(RateResponse r, Exception e) {
            super("Invalid rate response "+r.toString(), e);
        }
    }
}
