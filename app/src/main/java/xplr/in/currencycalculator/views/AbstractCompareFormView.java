package xplr.in.currencycalculator.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.presenters.BaseCompare;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 6/4/16.
 */
public abstract class AbstractCompareFormView<T extends BaseCompare> extends LinearLayout {

    private static final String LOG_TAG = AbstractCompareFormView.class.getSimpleName();

    public AbstractCompareFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, TextView.OnEditorActionListener listener, Analytics.TradeCompareAnalytics analytics);

    public abstract void populate(T compare, Currency target);

    public abstract void compare();

    public abstract void invalidateResults();

    protected void hideKeyboard() {
        View currentFocus = ((Activity)getContext()).getCurrentFocus();
        if(currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            Log.v(LOG_TAG, "Keyboard hidden.");
        }
    }
}
