package xplr.in.currencycalculator.presenters;

import java.text.DecimalFormat;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.models.SelectedCurrency;

/**
 * Created by cheriot on 5/26/16.
 */
public class ComparisonPresenter {

    private static final String LOG_TAG = ComparisonPresenter.class.getSimpleName();

    private RateCompare rateCompare;
    private TradeCompare tradeCompare;

    public ComparisonPresenter(SelectedCurrency baseCurrency, Currency target) {
        Money base = new Money(baseCurrency, baseCurrency.getAmountBigDecimal());
        rateCompare = new RateCompare(base, target);
        tradeCompare = new TradeCompare(base, target);
    }

    public String getMarketRate() {
        return new DecimalFormat().format(rateCompare.getMarketRate());
    }

    public boolean calculateRate(String rateToCompareStr) {
        return rateCompare.calculate(rateToCompareStr);
    }

    public boolean calculateTrade(String tradeToCompareStr) {
        return tradeCompare.calculate(tradeToCompareStr);
    }

    public Currency getBaseCurrency() {
        return rateCompare.getBase().getCurrency();
    }

    public Currency getTargetCurrency() {
        return rateCompare.getTarget();
    }

    public RateCompare getRateCompare() {
        return rateCompare;
    }

    public TradeCompare getTradeCompare() {
        return tradeCompare;
    }
}
