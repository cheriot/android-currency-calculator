package xplr.in.currencycalculator.views;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.models.Currency;
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
    private Analytics.TradeCompareAnalytics analytics;

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
    public void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, TextView.OnEditorActionListener listener, final Analytics.TradeCompareAnalytics analytics) {
        tradeForCurrencyEditorView.init(currencyRepository, metaRepository);
        tradeForCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(listener);
        this.analytics = analytics;
    }

    @Override
    public void populate(final TradeCompare tradeCompare, Currency targetCurrency) {
        this.tradeCompare = tradeCompare;
        tradeForCurrencyEditorView.setMoney(targetCurrency);
        tradeForCurrencyEditorView.getCurrencyAmount().setHint(tradeCompare.getMarketRateTargetMoney().getAmountFormatted());
        tradeForCurrencyEditorView.getCurrencyAmount().setTextClearListener(new ClearableEditText.TextClearListener() {
            @Override
            public void onTextCleared() {
                analytics.recordClearTradeFor(
                        tradeCompare.getBaseMoney(),
                        tradeCompare.getTargetCurrency());
            }
        });
    }

    @Override
    public void compare() {
        Log.v(LOG_TAG, "compareTrade");
        String userInput = tradeForCurrencyEditorView.getOptionalMoney().getAmount();
        boolean success = tradeCompare.calculate(userInput);
        if(success) {
            int strId = tradeCompare.isResultAGain() ?
                    R.string.rate_compare_result_gain : R.string.rate_compare_result_lose;
            String template = getContext().getString(strId);
            String msg = tradeCompare.formatResults(template);
            tradeResultText.setText(Html.fromHtml(msg));
            tradeResultText.setVisibility(View.VISIBLE);
            tradeCompareButton.setEnabled(false);
            hideKeyboard();
            analytics.recordRateCompareSuccess(
                    tradeCompare.getBaseMoney(),
                    tradeCompare.getTargetCurrency());
        } else {
            Log.e(LOG_TAG, "Unable to compareTrade.");
            tradeResultText.setVisibility(View.GONE);
            analytics.recordRateCompareFailure(
                    tradeCompare.getBaseMoney(),
                    tradeCompare.getTargetCurrency());
        }
    }

    @Override
    public void invalidateResults() {
        Log.v(LOG_TAG, "invalidateYesFeeResults");
        String userInput = tradeForCurrencyEditorView.getOptionalMoney().getAmount();
        if(tradeCompare.isSameComparison(userInput)) return;
        Log.v(LOG_TAG, "New userInput " + userInput);
        tradeCompareButton.setEnabled(!TextUtils.isEmpty(userInput));
        if(tradeResultText.getVisibility() == View.VISIBLE) {
            analytics.recordInvalidateResult(tradeCompare.getBaseMoney().getCurrency(), tradeCompare.getTargetCurrency());
        }
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
        tradeFormInstructionsView.setGravity(Gravity.NO_GRAVITY);
    }

    class TradeAmountChangeListener implements CurrencyAmountEditorView.CurrencyAmountChangeListener {
        @Override
        public void onCurrencyAmountChange() {
            Log.v(LOG_TAG, "onCurrencyAmountChange targetCurrency");
            invalidateResults();
        }
    }
}
