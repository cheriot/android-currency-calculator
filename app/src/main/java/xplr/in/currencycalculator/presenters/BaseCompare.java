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

    protected Money baseMoney;
    protected Currency targetCurrency;

    private BigDecimal marketRate;
    private Money marketRateTargetMoney;

    protected BigDecimal revenueRate;
    protected Money revenueBaseCurrency;
    protected Money revenueTargetCurrency;

    private boolean hasResult;
    private BigDecimal userNumber;

    public BaseCompare(Money baseMoney, Currency targetCurrency) {
        this.baseMoney = baseMoney;
        this.targetCurrency = targetCurrency;
        this.hasResult = false;
        if(baseMoney != null) {
            marketRate = baseMoney.rateTo(targetCurrency);
            marketRateTargetMoney = baseMoney.convertTo(targetCurrency);
        }
    }

    protected BigDecimal calculableNumber(String userInput) {
        if(baseMoney == null) {
            clearResults();
            return null;
        }

        BigDecimal parsed = parseUserInputNumber(userInput);

        if(parsed.equals(BigDecimal.ZERO)) {
            clearResults();
            return null;
        }

        userNumber = parsed;
        return parsed;
    }

    public BigDecimal parseUserInputNumber(String userInput) {
        BigDecimal parsed = null;
        try {
            Number n = new DecimalFormat().parse(userInput);
            parsed = new BigDecimal(n.toString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing the rate to compareRate <" + userInput + ">.", e);
            parsed = BigDecimal.ZERO;
        }
        return parsed;
    }

    public boolean isSameComparison(String userInput) {
        Log.v(LOG_TAG, "isSameComparison " + userInput + " == " + userNumber);
        if(TextUtils.isEmpty(userInput)) return false;
        if(userNumber == null) return false;
        BigDecimal newRate = parseUserInputNumber(userInput);
        return userNumber.equals(newRate);
    }

    public String formatResults(String template) {
        return String.format(
                template,
                formatPercent(revenueRate),
                revenueBaseCurrency.getAmountFormatted(),
                baseMoney.getCurrency().getCode(),
                revenueTargetCurrency.getAmountFormatted(),
                targetCurrency.getCode());
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

    public BigDecimal getMarketRate(int multiplier, boolean isRateDirectionNormal) {
        // By default, we accept the rate from the base currency to the target currency. If the
        // user has reversed that, convert.
        BigDecimal rate = isRateDirectionNormal ?
                marketRate : BigDecimal.ONE.divide(marketRate, Money.MATH_CONTEXT);
        Log.v(LOG_TAG, "getMarketRate " + multiplier + " " + isRateDirectionNormal + " " + marketRate);
        return rate.multiply(new BigDecimal(multiplier));
    }

    public Money getMarketRateTargetMoney() {
        return marketRateTargetMoney;
    }

    public Money getBaseMoney() {
        return baseMoney;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }
}
