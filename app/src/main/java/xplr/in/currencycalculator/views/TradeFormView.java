package xplr.in.currencycalculator.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.presenters.TradeCompare;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 6/4/16.
 */
public class TradeFormView extends AbstractCompareFormView<TradeCompare> implements View.OnClickListener {

    private static final String LOG_TAG = TradeFormView.class.getSimpleName();

    @Bind(R.id.trade_form_instructions) TextView tradeFormInstructionsView;
    @Bind(R.id.trade_for_currency) CurrencyAmountEditorView tradeForCurrencyEditorView;
    @Bind(R.id.trade_compare_button) Button tradeCompareButton;
    @Bind(R.id.trade_result_text) TextView tradeResultText;

    private TradeCompare tradeCompare;

    public TradeFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_trade_form, this, true);
        ButterKnife.bind(this);

        tradeForCurrencyEditorView.setCurrencyAmountChangeListener(new TradeAmountChangeListener());
        tradeResultText.setVisibility(View.GONE);
        tradeCompareButton.setEnabled(false);
        tradeCompareButton.setOnClickListener(this);
    }

    @Override
    public void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, TextView.OnEditorActionListener listener) {
        tradeForCurrencyEditorView.init(currencyRepository, metaRepository);
        tradeForCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(listener);
    }

    @Override
    public void populate(TradeCompare tradeCompare, Currency targetCurrency) {
        this.tradeCompare = tradeCompare;
        if(tradeForCurrencyEditorView.getSelectedCurrency() == null) {
            // Hacky: the amount on the trade currency is not persisted so don't set it if there's already a value
            tradeForCurrencyEditorView.setSelectedCurrency((SelectedCurrency) targetCurrency);
        }
        tradeForCurrencyEditorView.getCurrencyAmount().setHint(tradeCompare.getMarketRateTargetMoney().getAmountFormatted());
    }

    @Override
    public void compare() {
        Log.v(LOG_TAG, "compareTrade");
        String userInput = tradeForCurrencyEditorView.getSelectedCurrency().getAmount();
        boolean success = tradeCompare.calculate(userInput);
        if(success) {
            String template = getContext().getString(R.string.rate_compare_result);
            String msg = tradeCompare.formatResults(template);
            tradeResultText.setText(msg);
            tradeResultText.setVisibility(View.VISIBLE);
            tradeCompareButton.setEnabled(false);
            hideKeyboard();
        } else {
            Log.e(LOG_TAG, "Unable to compareTrade.");
            tradeResultText.setVisibility(View.GONE);
        }
    }

    @Override
    public void invalidateResults() {
        Log.v(LOG_TAG, "invalidateYesFeeResults");
        String userInput = tradeForCurrencyEditorView.getSelectedCurrency().getAmount();
        if(tradeCompare.isSameComparison(userInput)) return;
        Log.v(LOG_TAG, "New userInput " + userInput);
        tradeCompareButton.setEnabled(!TextUtils.isEmpty(userInput));
        tradeResultText.setVisibility(View.GONE);
        tradeCompare.clearResults();
    }

    @Override
    public void onClick(View v) {
        compare();
    }

    public void showInstructions(int stringId) {
        String str = getResources().getString(stringId);
        tradeFormInstructionsView.setText(str);
    }

    class TradeAmountChangeListener implements CurrencyAmountEditorView.CurrencyAmountChangeListener {
        @Override
        public void onCurrencyAmountChange() {
            Log.v(LOG_TAG, "onCurrencyAmountChange target");
            invalidateResults();
        }
    }
}
