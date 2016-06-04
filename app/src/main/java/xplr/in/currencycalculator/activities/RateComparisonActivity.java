package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.loaders.RateComparisonLoader;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.presenters.ComparisonPresenter;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyAmountEditorView;
import xplr.in.currencycalculator.views.CurrencyAmountEditorView;
import xplr.in.currencycalculator.views.ClearableEditText;

/**
 * Created by cheriot on 5/24/16.
 */
public class RateComparisonActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ComparisonPresenter>, CurrencyAmountEditorView.CurrencyAmountChangeListener {

    private static final String LOG_TAG = RateComparisonActivity.class.getSimpleName();
    private static final int RATE_COMPARISON_LOADER_ID = 1;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencyMetaRepository metaRepository;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyEditorView;
    // Exchange form views
    @Bind(R.id.rate_prompt_question) TextView ratePromptQuestion;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.base_currency_code) TextView baseCurrencyCode;
    @Bind(R.id.target_currency_code) TextView targetCurrencyCode;
    @Bind(R.id.rate_to_compare) ClearableEditText rateToCompare;
    @Bind(R.id.rate_compare_button) Button rateCompareButton;
    @Bind(R.id.rate_result_text) TextView rateResultText;
    // Trade form views
    @Bind(R.id.trade_form) View tradeForm;
    @Bind(R.id.trade_for_currency) CurrencyAmountEditorView tradeForCurrencyEditorView;
    @Bind(R.id.trade_compare_button) Button tradeCompareButton;
    @Bind(R.id.trade_result_text) TextView tradeResultText;

    TextView.OnEditorActionListener rateKeyboardDoneListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            compareRate(rateCompareButton);
            return true;
        }
    };
    TextView.OnEditorActionListener tradeKeyboardDoneListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            compareTrade(tradeCompareButton);
            return true;
        }
    };

    private ComparisonPresenter comparisonPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_comparison);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseCurrencyEditorView.init(currencyRepository, metaRepository);
        baseCurrencyEditorView.setCurrencyAmountChangeListener(this);
        tradeForCurrencyEditorView.init(currencyRepository, metaRepository);
        tradeForCurrencyEditorView.setCurrencyAmountChangeListener(new TradeAmountChangeListener());

        // Rate form
        rateForm.setVisibility(View.GONE);
        rateResultText.setVisibility(View.GONE);
        rateCompareButton.setEnabled(false);

        // Trade form
        tradeForm.setVisibility(View.GONE);
        tradeResultText.setVisibility(View.GONE);
        tradeCompareButton.setEnabled(false);
        rateToCompare.getEditText().addTextChangedListener(new RateInputChangeListener());

        getLoaderManager().initLoader(RATE_COMPARISON_LOADER_ID, null, this);

        // improve display of result text
        // calculateRate on keyboard's enter
    }

    public void setFeesYes(View view) {
        Log.v(LOG_TAG, "setFeesYes");
        rateForm.setVisibility(View.GONE);
        tradeForm.setVisibility(View.VISIBLE);
        baseCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(tradeKeyboardDoneListener);
        tradeForCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(tradeKeyboardDoneListener);
    }

    public void setFeesNo(View view) {
        Log.v(LOG_TAG, "setFeesNo");
        rateForm.setVisibility(View.VISIBLE);
        tradeForm.setVisibility(View.GONE);
        baseCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(rateKeyboardDoneListener);
        rateToCompare.setOnEditorActionListener(rateKeyboardDoneListener    );
    }

    public void compareRate(View view) {
        Log.v(LOG_TAG, "compareRate");
        boolean success = comparisonPresenter.getRateCompare().calculate(rateToCompare.getText());
        if(success) {
            String template = getString(R.string.rate_compare_result);
            String msg = comparisonPresenter.getRateCompare().formatResults(template);
            Log.v(LOG_TAG, msg);
            rateResultText.setText(msg);
            rateResultText.setVisibility(View.VISIBLE);
            rateCompareButton.setEnabled(false);
            hideKeyboard();
        } else {
            Log.e(LOG_TAG, "Unable to compareTrade.");
            rateResultText.setVisibility(View.GONE);
        }
    }

    public void compareTrade(View view) {
        Log.v(LOG_TAG, "compareTrade");
        // TODO get the amount from CurrencyAmountEditorView
        String userInput = tradeForCurrencyEditorView.getSelectedCurrency().getAmount();
        boolean success = comparisonPresenter.getTradeCompare().calculate(userInput);
        if(success) {
            String template = getString(R.string.rate_compare_result);
            String msg = comparisonPresenter.getTradeCompare().formatResults(template);
            tradeResultText.setText(msg);
            tradeResultText.setVisibility(View.VISIBLE);
            hideKeyboard();
        } else {
            Log.e(LOG_TAG, "Unable to compareTrade.");
            tradeResultText.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<ComparisonPresenter> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<ComparisonPresenter> loader, ComparisonPresenter data) {
        comparisonPresenter = data;
        baseCurrencyEditorView.setSelectedCurrency((SelectedCurrency)data.getBaseCurrency());
        if(tradeForCurrencyEditorView.getSelectedCurrency() == null) {
            // HACK: the amount on the trade currency is not persisted so don't set it if there's alredy a value
            tradeForCurrencyEditorView.setSelectedCurrency((SelectedCurrency) data.getTargetCurrency());
        }
        ratePromptQuestion.setText(data.getBaseCurrency().getName());
        baseCurrencyCode.setText(data.getBaseCurrency().getCode());
        targetCurrencyCode.setText(data.getTargetCurrency().getCode());
        if(data.getMarketRate() != null ) rateToCompare.setHint(data.getMarketRate());
        compareRate(rateCompareButton);
    }

    @Override
    public void onLoaderReset(Loader<ComparisonPresenter> loader) {
        // nothing to do
    }

    @Override
    public void onCurrencyAmountChange() {
        Log.v(LOG_TAG, "onCurrencyAmountChange base");
        getLoaderManager().restartLoader(RATE_COMPARISON_LOADER_ID, null, this);
    }

    public void invalidateNoFeeResults() {
        Log.v(LOG_TAG, "invalidateNoFeeResults");
        // is the new value actually different? 7, 7., 7.0???
        if(comparisonPresenter.getRateCompare().isSameComparison(rateToCompare.getText())) return;
        rateCompareButton.setEnabled(!TextUtils.isEmpty(rateToCompare.getText()));
        rateResultText.setVisibility(View.GONE);
        comparisonPresenter.getRateCompare().clearResults();
    }

    public void invalidateYesFeeResults() {
        Log.v(LOG_TAG, "invalidateYesFeeResults");
        String userInput = tradeForCurrencyEditorView.getSelectedCurrency().getAmount();
        if(comparisonPresenter.getTradeCompare().isSameComparison(userInput)) return;
        Log.v(LOG_TAG, "New userInput " + userInput);
        tradeCompareButton.setEnabled(!TextUtils.isEmpty(userInput));
        tradeResultText.setVisibility(View.GONE);
        comparisonPresenter.getTradeCompare().clearResults();
    }

    private void hideKeyboard() {
        if(getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            Log.v(LOG_TAG, "Keyboard hidden.");
        }
    }

    class RateInputChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            invalidateNoFeeResults();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    class TradeAmountChangeListener implements CurrencyAmountEditorView.CurrencyAmountChangeListener {
        @Override
        public void onCurrencyAmountChange() {
            Log.v(LOG_TAG, "onCurrencyAmountChange target");
            invalidateYesFeeResults();
        }
    }
}
