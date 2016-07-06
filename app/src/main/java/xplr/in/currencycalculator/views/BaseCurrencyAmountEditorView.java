package xplr.in.currencycalculator.views;

import android.content.Context;
import android.util.AttributeSet;

import xplr.in.currencycalculator.models.OptionalMoney;

/**
 * Created by cheriot on 6/3/16.
 */
public class BaseCurrencyAmountEditorView extends CurrencyAmountEditorView {
    public BaseCurrencyAmountEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void updateAmount(OptionalMoney optionalMoney, String amount) {
        super.updateAmount(optionalMoney, amount);
        getCurrencyRepository().setBaseMoney(optionalMoney);
    }
}
