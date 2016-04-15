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
    Integer position; // null for unselected currencies

    @ModelMethod
    public static String toString(Currency currency) {
        return "CurrencySpec{" +
                "id=" + currency.getId() +
                ", code='" + currency.getCode() + '\'' +
                ", rate=" + currency.getRate() +
                ", position=" + currency.getPosition() +
                '}';
    }

    @ModelMethod
    public static boolean isSelected(Currency currency) {
        return currency.getPosition() != null;
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
}
