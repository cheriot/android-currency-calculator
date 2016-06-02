package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
    @Bind(R.id.base_currency) CurrencyAmountEditorView baseCurrencyEditorView;
    // Exchange form views
    @Bind(R.id.purchase_question_name) TextView purchaseQuestionNameText;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.base_currency_code) TextView baseCurrencyCode;
    @Bind(R.id.target_currency_code) TextView targetCurrencyCode;
    @Bind(R.id.rate_to_compare) ClearableEditText rateToCompare;
    @Bind(R.id.compare_button) Button compareButton;
    @Bind(R.id.exchange_result_text) TextView exchangeResultText;
    // Trade form views
    @Bind(R.id.trade_form) View tradeForm;
    @Bind(R.id.trade_for_currency) CurrencyAmountEditorView tradeForCurrencyEditorView;
    @Bind(R.id.trade_result_text) TextView tradeResultText;

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
        tradeForm.setVisibility(View.GONE);
        exchangeResultText.setVisibility(View.GONE);
        compareButton.setEnabled(false);

        // Trade form
        tradeResultText.setVisibility(View.GONE);
        rateToCompare.getEditText().addTextChangedListener(new RateInputChangeListener());

        getLoaderManager().initLoader(RATE_COMPARISON_LOADER_ID, null, this);

        // Add offer input text.
        // Add new calculateRate button and result text.

        // improve display of result text
        // highlight/dim calculateRate button to reflect calculated state
        // hide keyboard on calculateRate
        // calculateRate on keyboard's enter
    }

    public void setFeesYes(View view) {
        Log.v(LOG_TAG, "setFeesYes");
        rateForm.setVisibility(View.GONE);
        tradeForm.setVisibility(View.VISIBLE);
    }

    public void setFeesNo(View view) {
        Log.v(LOG_TAG, "setFeesNo");
        rateForm.setVisibility(View.VISIBLE);
        tradeForm.setVisibility(View.GONE);
    }

    public void compareRate(View view) {
        boolean success = comparisonPresenter.getRateCompare().calculate(rateToCompare.getText());
        if(success) {
            String template = getString(R.string.rate_compare_result);
            String msg = comparisonPresenter.getRateCompare().formatResults(template);
            Log.v(LOG_TAG, msg);
            exchangeResultText.setText(msg);
            exchangeResultText.setVisibility(View.VISIBLE);
            compareButton.setEnabled(false);
        } else {
            Log.e(LOG_TAG, "Unable to calculateRate a result.");
            exchangeResultText.setVisibility(View.GONE);
        }
    }

    public void compareTrade(View view) {
        // TODO get the amount from CurrencyAmountEditorView
        // boolean success = comparisonPresenter.getTradeCompare().calculate("");
        tradeResultText.setVisibility(View.VISIBLE);
        tradeResultText.setText("Results go here.");
    }

    @Override
    public Loader<ComparisonPresenter> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<ComparisonPresenter> loader, ComparisonPresenter data) {
        comparisonPresenter = data;
        baseCurrencyEditorView.setSelectedCurrency((SelectedCurrency)data.getBaseCurrency());
        tradeForCurrencyEditorView.setSelectedCurrency((SelectedCurrency)data.getTargetCurrency());
        purchaseQuestionNameText.setText(data.getBaseCurrency().getName());
        baseCurrencyCode.setText(data.getBaseCurrency().getCode());
        targetCurrencyCode.setText(data.getTargetCurrency().getCode());
        rateToCompare.setHint(data.getMarketRate());
        compareRate(compareButton);
    }

    @Override
    public void onLoaderReset(Loader<ComparisonPresenter> loader) {
        // nothing to do
    }

    @Override
    public void onCurrencyAmountChange() {
        getLoaderManager().restartLoader(RATE_COMPARISON_LOADER_ID, null, this);
    }

    public void invalidateNoFeeResults() {
        // is the new value actually different? 7, 7., 7.0???
        if(comparisonPresenter.getRateCompare().isSameComparison(rateToCompare.getText())) return;
        compareButton.setEnabled(!TextUtils.isEmpty(rateToCompare.getText()));
        exchangeResultText.setVisibility(View.GONE);
        comparisonPresenter.getRateCompare().clearResults();
    }

    public void invalidateYesFeeResults() {
        //if(comparisonPresenter.sameTradeToCompare(tradeForCurrencyEditorView))
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
            invalidateYesFeeResults();
        }
    }
}
