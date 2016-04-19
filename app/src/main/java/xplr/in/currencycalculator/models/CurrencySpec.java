package xplr.in.currencycalculator.models;

import com.yahoo.squidb.annotations.ColumnSpec;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * TODO Custom data type for BigDecimal
 * https://github.com/yahoo/squidb/wiki/Writing-plugins-for-custom-data-types
 *
 * Created by cheriot on 4/9/16.
 */
@TableModelSpec(className="Currency",tableName="currencies")
public class CurrencySpec {

    @ColumnSpec(constraints="not null unique")
    String code;
    @ColumnSpec(constraints="not null")
    String rate;
    // Position is null for unselected currencies.
    Integer position;
    // Name is null when there's a mismatch between meta data packaged with the app and
    // the rate response.
    String name;
    // TODO Bullshit default value because sqlite requires one. Reset DB version before first release.
    @ColumnSpec(constraints="not null",defaultValue="2")
    int minorUnits;
    // TODO Bullshit default value because sqlite requires one. Reset DB version before first release.
    @ColumnSpec(constraints="not null",defaultValue="00")
    String issuingCountryCode;

    @ModelMethod
    public static boolean isSelected(Currency c) {
        return c.getPosition() != null;
    }

    @ModelMethod
    public static String getFlagResourceName(Currency c) {
        return "flag_" + c.getIssuingCountryCode().toLowerCase();
    }

    @ModelMethod
    public static void setRate(Currency c, CurrencyRate rate) {
        c.setRate(rate.getRate().toString());
    }

    @ModelMethod
    public static void setMeta(Currency c, CurrencyMeta meta) {
        c.setName(meta.getName());
        c.setIssuingCountryCode(meta.getIssuingCountryCode());
        c.setMinorUnits(meta.getMinorUnits());
    }

    @ModelMethod
    public static boolean equals(Currency currency, Object o) {
        if (currency == o) return true;
        if (o == null || currency.getClass() != o.getClass()) return false;

        Currency that = (Currency) o;

        return currency.getId() == that.getId();

    }

    @ModelMethod
    public static int hashCode(Currency currency) {
        return new Long(currency.getId()).hashCode();
    }

    @ModelMethod
    public static String toString(Currency c) {
        return "CurrencySpec{" +
                "code='" + c.getCode() + '\'' +
                ", rate='" + c.getRate() + '\'' +
                ", position=" + c.getPosition() +
                ", name='" + c.getName() + '\'' +
                ", minorUnits=" + c.getMinorUnits() +
                ", issuingCountryCode='" + c.getIssuingCountryCode() + '\'' +
                '}';
    }
}
