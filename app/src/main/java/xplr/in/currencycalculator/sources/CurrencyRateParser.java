package xplr.in.currencycalculator.sources;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import xplr.in.currencycalculator.models.CurrencyRate;

/**
 * Created by cheriot on 4/19/16.
 */
public class CurrencyRateParser {

    @Inject
    public CurrencyRateParser() {
    }

    public List<CurrencyRate> parse(String json) {
        try {
            return LoganSquare
                    .parse(json, CurrencyRateResponse.class)
                    .getQuery()
                    .getResults()
                    .getRates();

        } catch (IOException ioe) {
            FirebaseCrash.report(ioe);
            throw new RuntimeException("Error parsing CurrencyMeta", ioe);
        }

    }

    @JsonObject
    static class CurrencyRateResponse {
        @JsonField QueryResponse query;

        public QueryResponse getQuery() {
            return query;
        }
    }

    @JsonObject
    static class QueryResponse {
        @JsonField int count;
        @JsonField Date created;
        @JsonField ResultsResponse results;

        public ResultsResponse getResults() {
            return results;
        }
    }

    @JsonObject
    static class ResultsResponse {
        @JsonField List<CurrencyRate> rate;

        public List<CurrencyRate> getRates() {
            List<CurrencyRate> validRates = new ArrayList<>(rate.size());
            for(CurrencyRate r : rate) {
                if(r.isValid()) validRates.add(r);
            }
            return validRates;
        }
    }
}
