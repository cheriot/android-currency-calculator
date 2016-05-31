package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.loaders.RateComparisonLoader;
import xplr.in.currencycalculator.presenters.RateComparison;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyView;
import xplr.in.currencycalculator.views.ClearableEditText;

/**
 * Created by cheriot on 5/24/16.
 */
public class RateComparisonActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<RateComparison> {

    private static final String LOG_TAG = RateComparisonActivity.class.getSimpleName();
    private static final int RATE_COMPARISON_LOADER_ID = 1;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencyMetaRepository metaRepository;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.base_currency) BaseCurrencyView baseCurrencyView;
    @Bind(R.id.purchase_question_name) TextView purchaseQuestionNameText;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.rate_to_compare) ClearableEditText rateToCompare;
    @Bind(R.id.trade_form) View tradeForm;
    @Bind(R.id.results) View results;
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
        getLoaderManager().initLoader(RATE_COMPARISON_LOADER_ID, null, this);

        // Grab the base currency
        // Grab the desired currency
        // Hide/show based on fees and taxes
        // Accept rate input
        // Calculate!

        // Better display:
        // Use material guidelines to show example text while typing (market exchange rate)
        //
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

    @Override
    public Loader<RateComparison> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<RateComparison> loader, RateComparison data) {
        rateComparison = data;
        baseCurrencyView.setBaseCurrency(rateComparison.getBaseCurrency());
        purchaseQuestionNameText.setText(rateComparison.getBaseCurrency().getName());
        // TODO calculate the rate from baseCurrency
        rateToCompare.setHint(rateComparison.getTargetCurrency().getRate());
    }

    @Override
    public void onLoaderReset(Loader<RateComparison> loader) {
        this.rateComparison = null;
    }
}
