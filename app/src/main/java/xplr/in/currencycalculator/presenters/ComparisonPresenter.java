package xplr.in.currencycalculator.presenters;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.models.OptionalMoney;

/**
 * Created by cheriot on 5/26/16.
 */
public class ComparisonPresenter {

    private static final String LOG_TAG = ComparisonPresenter.class.getSimpleName();

    private OptionalMoney optionalMoney;
    private RateCompare rateCompare;
    private TradeCompare tradeCompare;

    public ComparisonPresenter(OptionalMoney optionalMoney, Currency target) {
        this.optionalMoney = optionalMoney;
        // Set the instantiateBaseMoney amount to one so at least a market rate can be calculated.
        Money base = optionalMoney.isEmpty() ?
                new Money(optionalMoney.getCurrency(), BigDecimal.ONE) : optionalMoney.getMoney();
        rateCompare = new RateCompare(base, target);
        tradeCompare = new TradeCompare(base, target);
    }

    public String getMarketRate(int multiplier, boolean isRateDirectionNormal) {
        BigDecimal rate = rateCompare.getMarketRate(isRateDirectionNormal).multiply(BigDecimal.valueOf(multiplier));
        if(rate != null) {
            return new DecimalFormat().format(rate);
        } else {
            return null;
        }
    }

    public OptionalMoney getOptionalMoney() {
        return optionalMoney;
    }

    public Currency getTargetCurrency() {
        return rateCompare.getTargetCurrency();
    }

    public RateCompare getRateCompare() {
        return rateCompare;
    }

    public TradeCompare getTradeCompare() {
        return tradeCompare;
    }

    public Currency getBaseCurrency() {
        return optionalMoney.getCurrency();
    }
}
