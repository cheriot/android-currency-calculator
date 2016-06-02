package xplr.in.currencycalculator.presenters;

import java.math.BigDecimal;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;

/**
 * Created by cheriot on 6/2/16.
 */
public class TradeCompare extends BaseCompare {

    public TradeCompare(Money base, Currency target) {
        super(base, target);
    }

    @Override
    public boolean calculate(BigDecimal userInput) {
        return false;
    }
}
