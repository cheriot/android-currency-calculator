package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.loaders.RateComparisonLoader;
import xplr.in.currencycalculator.presenters.ComparisonPresenter;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyAmountEditorView;
import xplr.in.currencycalculator.views.CurrencyAmountEditorView;
import xplr.in.currencycalculator.views.TradeFormView;

/**
 * Created by cheriot on 5/24/16.
 */
public class TradeComparisonActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ComparisonPresenter>, CurrencyAmountEditorView.CurrencyAmountChangeListener {
    private static final String LOG_TAG = TradeComparisonActivity.class.getSimpleName();
    private static final int TRADE_COMPARISON_LOADER_ID = 5;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencyMetaRepository metaRepository;
    @Inject Analytics analytics;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyEditorView;
    @Bind(R.id.trade_form) TradeFormView tradeFormView;

    TextView.OnEditorActionListener tradeKeyboardDoneListener = new TextView.OnEditorActionListener() {
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
        setContentView(R.layout.activity_offer_comparison);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseCurrencyEditorView.init(currencyRepository, metaRepository);
        baseCurrencyEditorView.setCurrencyAmountChangeListener(this);
        tradeFormView.showInstructions(R.string.trade_input_prompt);

        tradeFormView.init(currencyRepository, metaRepository, tradeKeyboardDoneListener, analytics.getTradeCompareAnalytics());
        getLoaderManager().initLoader(TRADE_COMPARISON_LOADER_ID, null, this);
    }

    @Override
    public void onCurrencyAmountChange() {
        Log.v(LOG_TAG, "onCurrencyAmountChange instantiateBaseMoney");
        getLoaderManager().restartLoader(TRADE_COMPARISON_LOADER_ID, null, this);
    }

    @Override
    public Loader<ComparisonPresenter> onCreateLoader(int id, Bundle args) {
        return new RateComparisonLoader(this, currencyRepository);
    }

    @Override
    public void onLoadFinished(Loader<ComparisonPresenter> loader, ComparisonPresenter data) {
        baseCurrencyEditorView.setMoney(data.getOptionalMoney());

        // Trade form
        tradeFormView.populate(data.getTradeCompare(), data.getTargetCurrency());
        // Data will be reloaded when the instantiateBaseMoney amount changes.
        tradeFormView.invalidateResults();
    }

    @Override
    public void onLoaderReset(Loader<ComparisonPresenter> loader) {
        // nothing to do
    }
}
