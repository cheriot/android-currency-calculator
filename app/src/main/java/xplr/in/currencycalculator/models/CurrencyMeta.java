package xplr.in.currencycalculator.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cheriot on 4/14/16.
 */
@JsonObject
public class CurrencyMeta {

    public enum FlagSize {
        SQUARE("1x1"), NORMAL("4x3");
        private String value;

        FlagSize(String value) {
            this.value = value;
        }
    }

    @JsonField String code;
    @JsonField String name;
    @JsonField int minorUnits;
    @JsonField String issuingCountryCode;
    @JsonField List<Country> countries;
    Map<FlagSize,Integer> flagResourceIds;

    public CurrencyMeta() {
        this.flagResourceIds = new HashMap<>(400); // 400 > # of currencies * # of flag sizes
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getMinorUnits() {
        return minorUnits;
    }

    public String getIssuingCountryCode() {
        return issuingCountryCode;
    }

    public int getFlagResourceId(FlagSize flagSize) {
        return flagResourceIds.get(flagSize);
    }

    public void setFlagResourceId(FlagSize flagSize, int flagResourceId) {
        flagResourceIds.put(flagSize, flagResourceId);
    }

    public String getResourceName(FlagSize flagSize) {
        return "flag_" + flagSize.value + "_" + getIssuingCountryCode().toLowerCase();
    }

    public List<Country> getCountries() {
        return countries;
    }

    @JsonObject
    public static class Country {
        @JsonField String name;
        @JsonField String code;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }
}
