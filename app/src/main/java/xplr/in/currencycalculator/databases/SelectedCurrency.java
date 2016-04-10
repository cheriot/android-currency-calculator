package xplr.in.currencycalculator.databases;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Base and calculated currencies have a transient field to hold the amount displayed on screen.
 *
 * Created by cheriot on 4/10/16.
 */
public class SelectedCurrency extends Currency {

    // TODO How does this whole Parcelable thing work?
    public static final Creator<SelectedCurrency> CREATOR = new ModelCreator<SelectedCurrency>(SelectedCurrency.class);

    private String amount;

    public String convertFrom(SelectedCurrency base) {
        if (base.getAmount() != null) {
            BigDecimal baseRate = new BigDecimal(base.getRate());
            BigDecimal baseAmount = new BigDecimal(base.getAmount());
            BigDecimal baseDollars = baseAmount.divide(baseRate, MathContext.DECIMAL128);

            BigDecimal myRate = new BigDecimal(getRate());
            BigDecimal myAmount = baseDollars.multiply(myRate);
            amount = myAmount.toString();
            return amount;
        } else {
            return "0";
        }
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
