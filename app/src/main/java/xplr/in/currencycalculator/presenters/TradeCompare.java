package xplr.in.currencycalculator.presenters;

import android.util.Log;

import java.math.BigDecimal;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;

/**
 * Created by cheriot on 6/2/16.
 */
public class TradeCompare extends BaseCompare {

    public static final String LOG_TAG = TradeCompare.class.getSimpleName();

    public TradeCompare(Money base, Currency target) {
        super(base, target);
    }

    public boolean calculate(String userInput) {
        BigDecimal tradeToCompare = calculableNumber(userInput);
        if(tradeToCompare == null) return false;
        Log.v(LOG_TAG, "calculate " + tradeToCompare + " instead of " + getMarketRateTargetMoney());
        Money tradeMoney = new Money(getTargetCurrency(), tradeToCompare);

        revenueTargetCurrency = getMarketRateTargetMoney().subtract(tradeMoney);
        revenueBaseCurrency = revenueTargetCurrency.convertTo(getBaseMoney().getCurrency());
        revenueRate = revenueBaseCurrency.divide(getBaseMoney());
        return true;
    }
}
