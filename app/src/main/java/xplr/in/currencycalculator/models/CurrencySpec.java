package xplr.in.currencycalculator.models;

import com.yahoo.squidb.annotations.ColumnSpec;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;

import java.math.BigDecimal;

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
    // Rate can be 0 when a currency is no longer in use. Do not use them. This is because rates
    // are updated regularly, but CurrencyMeta is only updated with the application code. Trying
    // to keep downloads small and fast.
    @ColumnSpec(constraints="not null")
    String rate;
    // Position on the main screen, null for unselected currencies.
    Integer position;
    @ColumnSpec(constraints="not null")
    String name;
    @ColumnSpec(constraints="not null")
    int minorUnits;
    @ColumnSpec(constraints="not null")
    String issuingCountryCode;

    @ModelMethod
    public static boolean isSelected(Currency c) {
        return c.getPosition() != null;
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
    public static BigDecimal rateTo(Currency baseCurrency, Currency targetCurrency) {
        Money one = new Money(baseCurrency, BigDecimal.ONE);
        return one.convertTo(targetCurrency).getAmount();
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
