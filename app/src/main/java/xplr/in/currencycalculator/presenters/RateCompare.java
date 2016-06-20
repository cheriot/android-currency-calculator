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

    public RateCompare(Money base, Currency target) {
        super(base, target);
    }

    public boolean calculate(String userInput, boolean isRateDirectionNormal) {
        BigDecimal userRate = calculableNumber(userInput);
        if(userRate == null) return false;
        Log.v(LOG_TAG, "calculateRate " + userRate + " isRateDirectionNormal "  + isRateDirectionNormal);

        BigDecimal baseToTargetRateToCompare = resolveRateDirection(userRate, isRateDirectionNormal);

        // Calculate the bank's revenue on the transaction.
        revenueRate = getMarketRate().subtract(baseToTargetRateToCompare)
                .divide(getMarketRate(), Money.MATH_CONTEXT);
        revenueBaseCurrency = baseMoney.multiply(revenueRate);
        revenueTargetCurrency = revenueBaseCurrency.convertTo(targetCurrency);

        // Success!
        return true;
    }

    private BigDecimal resolveRateDirection(BigDecimal userRate, boolean isRateDirectionNormal) {
        // By default, we accept the rate from the base currency to the target currency. If the
        // user has reversed that, convert.
        if(isRateDirectionNormal) {
            return userRate;
        }
        return BigDecimal.ONE.divide(userRate, Money.MATH_CONTEXT);
    }
}
