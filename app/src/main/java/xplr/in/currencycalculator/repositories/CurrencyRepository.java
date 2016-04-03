package xplr.in.currencycalculator.repositories;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.orm.SugarRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 4/1/16.
 */
public class CurrencyRepository {

    private static final String LOG_TAG = CurrencyRepository.class.getCanonicalName();
    private static final String URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22USDBTC%22%2C%22USDAED%22%2C%20%22USDAFN%22%2C%20%22USDALL%22%2C%20%22USDAMD%22%2C%20%22USDANG%22%2C%20%22USDAOA%22%2C%20%22USDARS%22%2C%20%22USDAUD%22%2C%20%22USDAWG%22%2C%20%22USDAZN%22%2C%20%22USDBAM%22%2C%20%22USDBBD%22%2C%20%22USDBDT%22%2C%20%22USDBGN%22%2C%20%22USDBHD%22%2C%20%22USDBIF%22%2C%20%22USDBMD%22%2C%20%22USDBND%22%2C%20%22USDBOB%22%2C%20%22USDBRL%22%2C%20%22USDBSD%22%2C%20%22USDBTC%22%2C%20%22USDBTN%22%2C%20%22USDBWP%22%2C%20%22USDBYR%22%2C%20%22USDBZD%22%2C%20%22USDCAD%22%2C%20%22USDCDF%22%2C%20%22USDCHF%22%2C%20%22USDCLF%22%2C%20%22USDCLP%22%2C%20%22USDCNY%22%2C%20%22USDCOP%22%2C%20%22USDCRC%22%2C%20%22USDCUC%22%2C%20%22USDCUP%22%2C%20%22USDCVE%22%2C%20%22USDCZK%22%2C%20%22USDDJF%22%2C%20%22USDDKK%22%2C%20%22USDDOP%22%2C%20%22USDDZD%22%2C%20%22USDEEK%22%2C%20%22USDEGP%22%2C%20%22USDERN%22%2C%20%22USDETB%22%2C%20%22USDEUR%22%2C%20%22USDFJD%22%2C%20%22USDFKP%22%2C%20%22USDGBP%22%2C%20%22USDGEL%22%2C%20%22USDGGP%22%2C%20%22USDGHS%22%2C%20%22USDGIP%22%2C%20%22USDGMD%22%2C%20%22USDGNF%22%2C%20%22USDGTQ%22%2C%20%22USDGYD%22%2C%20%22USDHKD%22%2C%20%22USDHNL%22%2C%20%22USDHRK%22%2C%20%22USDHTG%22%2C%20%22USDHUF%22%2C%20%22USDIDR%22%2C%20%22USDILS%22%2C%20%22USDIMP%22%2C%20%22USDINR%22%2C%20%22USDIQD%22%2C%20%22USDIRR%22%2C%20%22USDISK%22%2C%20%22USDJEP%22%2C%20%22USDJMD%22%2C%20%22USDJOD%22%2C%20%22USDJPY%22%2C%20%22USDKES%22%2C%20%22USDKGS%22%2C%20%22USDKHR%22%2C%20%22USDKMF%22%2C%20%22USDKPW%22%2C%20%22USDKRW%22%2C%20%22USDKWD%22%2C%20%22USDKYD%22%2C%20%22USDKZT%22%2C%20%22USDLAK%22%2C%20%22USDLBP%22%2C%20%22USDLKR%22%2C%20%22USDLRD%22%2C%20%22USDLSL%22%2C%20%22USDLTL%22%2C%20%22USDLVL%22%2C%20%22USDLYD%22%2C%20%22USDMAD%22%2C%20%22USDMDL%22%2C%20%22USDMGA%22%2C%20%22USDMKD%22%2C%20%22USDMMK%22%2C%20%22USDMNT%22%2C%20%22USDMOP%22%2C%20%22USDMRO%22%2C%20%22USDMUR%22%2C%20%22USDMVR%22%2C%20%22USDMWK%22%2C%20%22USDMXN%22%2C%20%22USDMYR%22%2C%20%22USDMZN%22%2C%20%22USDNAD%22%2C%20%22USDNGN%22%2C%20%22USDNIO%22%2C%20%22USDNOK%22%2C%20%22USDNPR%22%2C%20%22USDNZD%22%2C%20%22USDOMR%22%2C%20%22USDPAB%22%2C%20%22USDPEN%22%2C%20%22USDPGK%22%2C%20%22USDPHP%22%2C%20%22USDPKR%22%2C%20%22USDPLN%22%2C%20%22USDPYG%22%2C%20%22USDQAR%22%2C%20%22USDRON%22%2C%20%22USDRSD%22%2C%20%22USDRUB%22%2C%20%22USDRWF%22%2C%20%22USDSAR%22%2C%20%22USDSBD%22%2C%20%22USDSCR%22%2C%20%22USDSDG%22%2C%20%22USDSEK%22%2C%20%22USDSGD%22%2C%20%22USDSHP%22%2C%20%22USDSLL%22%2C%20%22USDSOS%22%2C%20%22USDSRD%22%2C%20%22USDSTD%22%2C%20%22USDSVC%22%2C%20%22USDSYP%22%2C%20%22USDSZL%22%2C%20%22USDTHB%22%2C%20%22USDTJS%22%2C%20%22USDTMT%22%2C%20%22USDTND%22%2C%20%22USDTOP%22%2C%20%22USDTRY%22%2C%20%22USDTTD%22%2C%20%22USDTWD%22%2C%20%22USDTZS%22%2C%20%22USDUAH%22%2C%20%22USDUGX%22%2C%20%22USDUSD%22%2C%20%22USDUYU%22%2C%20%22USDUZS%22%2C%20%22USDVEF%22%2C%20%22USDVND%22%2C%20%22USDVUV%22%2C%20%22USDWST%22%2C%20%22USDXAF%22%2C%20%22USDXAG%22%2C%20%22USDXAU%22%2C%20%22USDXCD%22%2C%20%22USDXDR%22%2C%20%22USDXOF%22%2C%20%22USDXPF%22%2C%20%22USDYER%22%2C%20%22USDZAR%22%2C%20%22USDZMK%22%2C%20%22USDZMW%22%2C%20%22USDZWL%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&format=json";

    public static List<Currency> fetchAll() {
        String json = requestCurrencyJson();
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

    private static String requestCurrencyJson() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(URL).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
        catch (IOException ioe) {
            Log.e(LOG_TAG, "Error requesting new currency rates.", ioe);
            return null;
        }
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
            return !("N/A".equals(Rate) || "N/A".equals("Name"));
        }

        public String getCode() {
            return id;
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
