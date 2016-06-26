package xplr.in.currencycalculator.presenters;

import android.util.Log;

import java.math.BigDecimal;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;

/**
 * Created by cheriot on 6/2/16.
 */
public class RateCompare extends BaseCompare {

    private static final String LOG_TAG = ComparisonPresenter.class.getSimpleName();

    private Money receiveMoney;
    private int multiplier;
    private boolean isRateDirectionNormal;

    public RateCompare(Money base, Currency target) {
        super(base, target);
    }

    public boolean calculate(int multiplier, String userInput, boolean isRateDirectionNormal) {
        this.multiplier = multiplier;
        this.isRateDirectionNormal = isRateDirectionNormal;
        BigDecimal userRate = calculableNumber(userInput);
        if(userRate == null) return false;
        BigDecimal marketRate = getMarketRate(multiplier, isRateDirectionNormal);
        Log.v(LOG_TAG, "calculateRate " + userRate
                + " isRateDirectionNormal "  + isRateDirectionNormal
                + " marketRate " + marketRate);

        // Calculate the bank's revenue on the transaction.
        revenueRate = marketRate.subtract(userRate).divide(marketRate, Money.MATH_CONTEXT);
        revenueBaseCurrency = baseMoney.multiply(revenueRate);
        revenueTargetCurrency = revenueBaseCurrency.convertTo(targetCurrency);
        BigDecimal receiveAmount = baseMoney.divide(multiplier).multiply(userRate);
        receiveMoney = new Money(targetCurrency, receiveAmount);

        // Success!
        return true;
    }

    public boolean isSameComparison(int multiplier, String userInput, boolean isRateDirectionNormal) {
        return this.multiplier == multiplier
                && this.isRateDirectionNormal == isRateDirectionNormal
                && super.isSameComparison(userInput);
    }

    public String getReceiveMoney() {
        return receiveMoney.getAmountFormatted() + " " + targetCurrency.getCode();
    }
}
