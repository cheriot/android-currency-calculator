package xplr.in.currencycalculator.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by cheriot on 4/14/16.
 */
@JsonObject
public class CurrencyMeta {
    @JsonField String code;
    @JsonField String name;
    @JsonField int minorUnits;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getMinorUnits() {
        return minorUnits;
    }
}
