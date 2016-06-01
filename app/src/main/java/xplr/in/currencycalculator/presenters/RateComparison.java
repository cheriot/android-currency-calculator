package xplr.in.currencycalculator.presenters;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.ParseException;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.models.SelectedCurrency;

/**
 * Created by cheriot on 5/26/16.
 */
public class RateComparison {

    private static final String LOG_TAG = RateComparison.class.getSimpleName();
    private static final BigDecimal BD100 = new BigDecimal(100);

    private final Money baseMoney;
    private final Currency targetCurrency;

    private BigDecimal marketRate;
    private BigDecimal bankRevenueRate;
    private Money bankRevenueBaseCurrency;
    private Money bankRevenueTargetCurrency;

    public RateComparison(SelectedCurrency baseCurrency, SelectedCurrency targetCurrency) {
        this.baseMoney = new Money(baseCurrency, baseCurrency.getAmountBigDecimal());
        this.targetCurrency = targetCurrency;
        marketRate = baseCurrency.rateTo(targetCurrency);
    }

    public String getMarketRate() {
        return new DecimalFormat().format(marketRate);
    }

    public boolean calculate(String rateToCompareStr) {
        Log.v(LOG_TAG, "calculate " + rateToCompareStr);
        BigDecimal rateToCompare = parseUserInputNumber(rateToCompareStr);
        if(rateToCompare.equals(BigDecimal.ZERO)) return false;

        bankRevenueRate = marketRate.subtract(rateToCompare)
                .divide(marketRate, MathContext.DECIMAL128);
        bankRevenueBaseCurrency = baseMoney.multiply(bankRevenueRate);
        bankRevenueTargetCurrency = bankRevenueBaseCurrency.convertTo(targetCurrency);
        return true;
    }

    private BigDecimal parseUserInputNumber(String str) {
        try {
            Number n = new DecimalFormat().parse(str);
            return new BigDecimal(n.toString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing the rate to compare <" + str + ">.", e);
            return BigDecimal.ZERO;
        }
    }

    private String formatPercent(BigDecimal d) {
        BigDecimal wholeNumber = d.multiply(BD100);
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(2);
        return format.format(wholeNumber);
    }

    public Currency getBaseCurrency() {
        return baseMoney.getCurrency();
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public String getBankRevenuePercent() {
        return formatPercent(bankRevenueRate);
    }

    public String getBankRevenueBaseCurrency() {
        return bankRevenueBaseCurrency.getAmountFormatted();
    }

    public String getBankRevenueTargetCurrency() {
        return bankRevenueTargetCurrency.getAmountFormatted();
    }
}
