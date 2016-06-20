package xplr.in.currencycalculator.presenters;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.models.SelectedCurrency;

/**
 * Created by cheriot on 5/26/16.
 */
public class ComparisonPresenter {

    private static final String LOG_TAG = ComparisonPresenter.class.getSimpleName();

    private Currency baseCurrency;
    private RateCompare rateCompare;
    private TradeCompare tradeCompare;

    public ComparisonPresenter(SelectedCurrency baseCurrency, Currency target) {
        this.baseCurrency = baseCurrency;
        // Set the baseMoney amount to one so at least a market rate can be calculated.
        Money base = TextUtils.isEmpty(baseCurrency.getAmount()) ?
                new Money(baseCurrency, BigDecimal.ONE) : new Money(baseCurrency, baseCurrency.getAmountBigDecimal());
        rateCompare = new RateCompare(base, target);
        tradeCompare = new TradeCompare(base, target);
    }

    public String getMarketRate() {
        if(rateCompare.getMarketRate() != null) {
            return new DecimalFormat().format(rateCompare.getMarketRate());
        } else {
            return null;
        }
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
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
}
