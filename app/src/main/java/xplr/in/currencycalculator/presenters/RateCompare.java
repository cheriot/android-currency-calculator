package xplr.in.currencycalculator.presenters;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;

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

    @Override
    public boolean calculate(BigDecimal rateToCompare) {
        Log.v(LOG_TAG, "calculateRate " + rateToCompare);
        if(rateToCompare.equals(BigDecimal.ZERO)) return false;

        BigDecimal marketRate = base.rateTo(target);
        revenueRate = marketRate.subtract(rateToCompare)
                .divide(marketRate, MathContext.DECIMAL128);
        revenueBaseCurrency = base.multiply(revenueRate);
        revenueTargetCurrency = revenueBaseCurrency.convertTo(target);

        return true;
    }
}
