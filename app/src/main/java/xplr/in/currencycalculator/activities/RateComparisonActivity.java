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
import xplr.in.currencycalculator.views.TradeFormView;

/**
 * Created by cheriot on 5/24/16.
 */
public class RateComparisonActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ComparisonPresenter>, CurrencyAmountEditorView.CurrencyAmountChangeListener {

    private static final String LOG_TAG = RateComparisonActivity.class.getSimpleName();
    private static final int COMPARISON_LOADER_ID = 1;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencyMetaRepository metaRepository;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyEditorView;
    // Rate form
    @Bind(R.id.rate_prompt_question) TextView ratePromptQuestion;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.base_currency_code) TextView baseCurrencyCode;
    @Bind(R.id.target_currency_code) TextView targetCurrencyCode;
    @Bind(R.id.rate_to_compare) ClearableEditText rateToCompare;
    @Bind(R.id.rate_compare_button) Button rateCompareButton;
    @Bind(R.id.rate_result_text) TextView rateResultText;
    // Trade form
    @Bind(R.id.trade_form) TradeFormView tradeFormView;

    private ComparisonPresenter comparisonPresenter;
    private TextView.OnEditorActionListener rateKeyboardDoneListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.v(LOG_TAG, "rateKeybaordDonListener");
            compareRate(rateCompareButton);
            return true;
        }
    };
    private TextView.OnEditorActionListener tradeKeyboardDoneListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.v(LOG_TAG, "tradeKeyboardDoneListener");
            tradeFormView.compare();
            return true;
        }
    };

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

        // Rate form
        rateForm.setVisibility(View.GONE);
        rateResultText.setVisibility(View.GONE);
        rateCompareButton.setEnabled(false);
        rateToCompare.getEditText().addTextChangedListener(new RateInputChangeListener());
        rateToCompare.setOnEditorActionListener(rateKeyboardDoneListener);

        // Trade form
        tradeFormView.init(currencyRepository, metaRepository, tradeKeyboardDoneListener);
        tradeFormView.setVisibility(View.GONE);
        tradeFormView.showInstructions(R.string.trade_input_prompt_fees);

        getLoaderManager().initLoader(COMPARISON_LOADER_ID, null, this);

        // improve display of result text
        // calculateRate on keyboard's enter
    }

    public void setFeesYes(View view) {
        Log.v(LOG_TAG, "setFeesYes");
        rateForm.setVisibility(View.GONE);
        tradeFormView.setVisibility(View.VISIBLE);
        baseCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(tradeKeyboardDoneListener);
    }

    public void setFeesNo(View view) {
        Log.v(LOG_TAG, "setFeesNo");
        rateForm.setVisibility(View.VISIBLE);
        tradeFormView.setVisibility(View.GONE);
        baseCurrencyEditorView.getCurrencyAmount().setOnEditorActionListener(rateKeyboardDoneListener);
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

    @Override
    public Loader<ComparisonPresenter> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<ComparisonPresenter> loader, ComparisonPresenter data) {
        comparisonPresenter = data;
        baseCurrencyEditorView.setSelectedCurrency((SelectedCurrency)data.getBaseCurrency());

        // Rate form
        ratePromptQuestion.setText(data.getBaseCurrency().getName());
        baseCurrencyCode.setText(data.getBaseCurrency().getCode());
        targetCurrencyCode.setText(data.getTargetCurrency().getCode());
        if(data.getMarketRate() != null ) rateToCompare.setHint(data.getMarketRate());
        invalidateNoFeeResults();

        // Trade form
        tradeFormView.populate(data.getTradeCompare(), data.getTargetCurrency());
        // Data will be reloaded when the base amount changes.
        tradeFormView.invalidateResults();
    }

    @Override
    public void onLoaderReset(Loader<ComparisonPresenter> loader) {
        // nothing to do
    }

    @Override
    public void onCurrencyAmountChange() {
        Log.v(LOG_TAG, "onCurrencyAmountChange base");
        getLoaderManager().restartLoader(COMPARISON_LOADER_ID, null, this);
    }

    public void invalidateNoFeeResults() {
        Log.v(LOG_TAG, "invalidateNoFeeResults");
        // is the new value actually different? 7, 7., 7.0???
        if(comparisonPresenter.getRateCompare().isSameComparison(rateToCompare.getText())) return;
        rateCompareButton.setEnabled(!TextUtils.isEmpty(rateToCompare.getText()));
        rateResultText.setVisibility(View.GONE);
        comparisonPresenter.getRateCompare().clearResults();
    }

    private void hideKeyboard() {
        // TODO kill this copy
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
}
