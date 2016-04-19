package xplr.in.currencycalculator.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.math.BigDecimal;

/**
 * Created by cheriot on 4/19/16.
 */
@JsonObject
public class CurrencyRate {

    @JsonField String id;
    @JsonField String Rate;

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

    public static class InvalidRateException extends RuntimeException {
        public InvalidRateException(CurrencyRate r) {
            super("Invalid rate response "+r.toString());
        }
    }
}
