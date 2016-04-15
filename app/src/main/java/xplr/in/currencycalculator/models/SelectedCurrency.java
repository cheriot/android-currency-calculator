package xplr.in.currencycalculator.models;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Base and calculated currencies have a transient field to hold the amount displayed on screen.
 *
 * Created by cheriot on 4/10/16.
 */
public class SelectedCurrency extends Currency {

    // TODO How does this whole Parcelable thing work?
    public static final Creator<SelectedCurrency> CREATOR = new ModelCreator<SelectedCurrency>(SelectedCurrency.class);

    private String amount;

    public BigDecimal convertFrom(SelectedCurrency base) {
        if (base != null
                && base.getAmount() != null
                && !TextUtils.isEmpty(base.getAmount().trim())) {
            BigDecimal baseRate = new BigDecimal(base.getRate());
            BigDecimal baseAmount = new BigDecimal(base.getAmount());
            BigDecimal baseDollars = baseAmount.divide(baseRate, MathContext.DECIMAL128);

            BigDecimal myRate = new BigDecimal(getRate());
            BigDecimal myAmount = baseDollars.multiply(myRate);
            amount = myAmount.toString();
            return myAmount;
        } else {
            return null;
        }
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String parse(String formattedAmount) {
        if(!formattedAmount.equals("-")) {
            try {
                amount = getFormatter(null).parse(formattedAmount).toString();
            }catch (ParseException pe) {
                String msg = "Error parsing " + getCode() + " " + formattedAmount + ".";
                throw new RuntimeException(msg, pe);
            }
        }
        return amount;
    }

    public String format(CurrencyMeta meta) {
        if(amount != null) {
            return getFormatter(meta).format(new BigDecimal(amount));
        } else {
            return "-";
        }
    }

    private DecimalFormat getFormatter(CurrencyMeta meta) {
        // DecimalFormat will use the default locale. This still ignores the currency symbol
        // and which side of the number it goes on. Also, Locale.Category is not available.
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(meta != null ? meta.getMinorUnits() : 2);
        return format;
    }

    public boolean sameDisplay(SelectedCurrency selectedCurrency) {
        return equals(selectedCurrency)
                && amount != null
                && amount.equals(selectedCurrency.getAmount());
    }
}
