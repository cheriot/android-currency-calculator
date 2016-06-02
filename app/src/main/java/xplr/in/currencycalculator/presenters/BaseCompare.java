package xplr.in.currencycalculator.presenters;

import android.text.TextUtils;
import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;

/**
 * Created by cheriot on 6/2/16.
 */
public abstract class BaseCompare {

    private static final String LOG_TAG = BaseCompare.class.getSimpleName();

    protected Money base;
    protected Currency target;
    protected BigDecimal marketRate;

    protected BigDecimal revenueRate;
    protected Money revenueBaseCurrency;
    protected Money revenueTargetCurrency;

    private boolean hasResult;
    private BigDecimal userNumber;

    public BaseCompare(Money base, Currency target) {
        this.base = base;
        this.target = target;
        this.hasResult = false;
    }

    public boolean calculate(String userInput) {
        userNumber = parseUserInputNumber(userInput);
        hasResult = calculate(userNumber);
        return hasResult;
    }

    public abstract boolean calculate(BigDecimal userInput);

    private BigDecimal parseUserInputNumber(String str) {
        try {
            Number n = new DecimalFormat().parse(str);
            return new BigDecimal(n.toString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing the rate to compareRate <" + str + ">.", e);
            return BigDecimal.ZERO;
        }
    }

    public boolean isSameComparison(String userInput) {
        Log.v(LOG_TAG, userInput);
        if(TextUtils.isEmpty(userInput)) return false;
        if(userNumber == null) return false;
        BigDecimal newRate = parseUserInputNumber(userInput);
        return userInput.equals(newRate);
    }

    public String formatResults(String template) {
        return String.format(
                template,
                formatPercent(revenueRate),
                revenueBaseCurrency.getAmountFormatted(),
                base.getCurrency().getCode(),
                revenueTargetCurrency.getAmountFormatted(),
                target.getCode());
    }

    private String formatPercent(BigDecimal d) {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);
        return format.format(d);
    }

    public void clearResults() {
        revenueRate = null;
        revenueBaseCurrency = null;
        revenueTargetCurrency = null;
        hasResult = false;
    }

    public BigDecimal getMarketRate() {
        return base.rateTo(target);
    }

    public Money getBase() {
        return base;
    }

    public Currency getTarget() {
        return target;
    }
}
