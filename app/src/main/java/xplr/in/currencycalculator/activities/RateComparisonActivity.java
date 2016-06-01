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
import xplr.in.currencycalculator.presenters.RateComparison;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyView;
import xplr.in.currencycalculator.views.ClearableEditText;

/**
 * Created by cheriot on 5/24/16.
 */
public class RateComparisonActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<RateComparison>, BaseCurrencyView.CurrencyAmountChangeListener {

    private static final String LOG_TAG = RateComparisonActivity.class.getSimpleName();
    private static final int RATE_COMPARISON_LOADER_ID = 1;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencyMetaRepository metaRepository;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.base_currency) BaseCurrencyView baseCurrencyView;
    @Bind(R.id.purchase_question_name) TextView purchaseQuestionNameText;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.base_currency_code) TextView baseCurrencyCode;
    @Bind(R.id.target_currency_code) TextView targetCurrencyCode;
    @Bind(R.id.rate_to_compare) ClearableEditText rateToCompare;
    @Bind(R.id.trade_form) View tradeForm;
    @Bind(R.id.compare_button) Button compareButton;
    @Bind(R.id.results) View results;
    @Bind(R.id.result_text) TextView resultText;
    private RateComparison rateComparison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_comparison);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseCurrencyView.init(currencyRepository, metaRepository);
        rateForm.setVisibility(View.GONE);
        tradeForm.setVisibility(View.GONE);
        results.setVisibility(View.GONE);
        compareButton.setEnabled(false);

        baseCurrencyView.setCurrencyAmountChangeListener(this);
        rateToCompare.getEditText().addTextChangedListener(new RateInputChangeListener());
        getLoaderManager().initLoader(RATE_COMPARISON_LOADER_ID, null, this);

        // Add offer input text.
        // Add new calculate button and result text.

        // improve display of result text
        // highlight/dim calculate button to reflect calculated state
        // hide keyboard on calculate
        // calculate on keyboard's enter
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

    public void compare(View view) {
        if(TextUtils.isEmpty(rateToCompare.getEditText().getText())) return;

        boolean success = rateComparison.calculate(rateToCompare.getText());
        if(success) {
            String msg = rateComparison.getBankRevenuePercent() + "%, "
                    + rateComparison.getBankRevenueBaseCurrency() + " " + rateComparison.getBaseCurrency().getCode() + ", or "
                    + rateComparison.getBankRevenueTargetCurrency() + " " + rateComparison.getTargetCurrency().getCode();
            Log.v(LOG_TAG, msg);
            resultText.setText(msg);
            results.setVisibility(View.VISIBLE);
            compareButton.setEnabled(false);
        } else {
            Log.e(LOG_TAG, "Unable to calculate a result.");
            results.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<RateComparison> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<RateComparison> loader, RateComparison data) {
        rateComparison = data;
        baseCurrencyView.setBaseCurrency((SelectedCurrency)data.getBaseCurrency());
        purchaseQuestionNameText.setText(data.getBaseCurrency().getName());
        baseCurrencyCode.setText(data.getBaseCurrency().getCode());
        targetCurrencyCode.setText(data.getTargetCurrency().getCode());
        rateToCompare.setHint(data.getMarketRate());
        compare(compareButton);
    }

    @Override
    public void onLoaderReset(Loader<RateComparison> loader) {
        // nothing to do
    }

    @Override
    public void onCurrencyAmountChange() {
        getLoaderManager().restartLoader(RATE_COMPARISON_LOADER_ID, null, this);
    }

    public void invalidateNoFeeResults() {
        // is the new value actually different? 7, 7., 7.0???
        if(rateComparison.sameRateToCompare(rateToCompare.getText())) return;
        compareButton.setEnabled(!TextUtils.isEmpty(rateToCompare.getText()));
        results.setVisibility(View.GONE);
        rateComparison.clearResults();
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
