package xplr.in.currencycalculator.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyRecyclerAdapter;
import xplr.in.currencycalculator.loaders.SelectedCurrencyLoader;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyDataChangeEvent;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;
import xplr.in.currencycalculator.sync.SyncCompleteEvent;

public class MainActivity extends AppCompatActivity implements CurrencyListActivity, LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 1;

    @Inject EventBus eventBus;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    @Inject CurrencyMetaRepository currencyMetaRepository;
    @Inject @Named("calculate") CurrencyRecyclerAdapter currenciesAdapter;
    SelectedCurrency baseCurrency;

    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.base_currency_name) TextView baseCurrencyName;
    @Bind(R.id.base_currency_amount) EditText baseCurrencyAmount;
    @Bind(R.id.base_currency_amount_clear) ImageButton baseCurrencyAmountClear;
    @Bind(R.id.base_currency_flag) ImageView baseCurrencyFlag;
    @Bind(R.id.list_currency_calculations) RecyclerView currencyCalculationsRecyclerView;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_calculator);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        eventBus.register(this);
        new BaseCurrencyQuery().execute();

        final Activity thisActivity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(thisActivity, SelectCurrencyActivity.class));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Make sure to call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                // See the eventBus subscriber on this class.
                currencySyncTriggers.syncNow();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Replace with butterknife's @OnTextChange?
        baseCurrencyAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if(baseCurrency != null && !text.equals(baseCurrency.getAmount())) {
                    Log.v(LOG_TAG, "TEXT " + text);
                    currencyRepository.setBaseAmount(baseCurrency, text);
                    currenciesAdapter.notifyDataSetChanged();
                }
                if(s.length() == 0) {
                    baseCurrencyAmountClear.setVisibility(View.INVISIBLE);
                } else {
                    baseCurrencyAmountClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        baseCurrencyAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(baseCurrencyAmount.getWindowToken(), 0);
                return true;
            }
        });

        currencyCalculationsRecyclerView.setAdapter(currenciesAdapter);
        currencyCalculationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyCalculationsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize Loader & its handler.
        getLoaderManager().initLoader(LOADER_ID, null, this);

        // TODO There's a "30 frames skipped" message...
        // Setup the sync account on another thread?
        // Create/Upgrade/Init database on another thread?
        // Init filesystem repositories on another thread?
        // too many bindViews of CurrencyCursorAdapter?

        // Setup a scheduled sync if that hasn't happened already. This will trigger an initial
        // sync if one has not occurred.
        currencySyncTriggers.createSyncAccount(this);
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    public void clearBaseAmount(View view) {
        baseCurrencyAmount.getText().clear();
        baseCurrencyAmount.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(baseCurrencyAmount, InputMethodManager.SHOW_FORCED);
        Log.v(LOG_TAG, "Cleared, now show keyboard.");
    }

    private void displayBaseCurrency(SelectedCurrency currency, CurrencyMeta meta) {
        if (baseCurrency != null
                && baseCurrency.sameDisplay(currency)) return;

        Log.v(LOG_TAG, "displayBaseCurrency " + currency.getCode());
        baseCurrency = currency;
        baseCurrencyName.setText(currency.getName());
        if(meta != null) {
            Drawable drawable = getResources().getDrawable(meta.getFlagResourceId(CurrencyMeta.FlagSize.NORMAL));
            baseCurrencyFlag.setImageDrawable(drawable);
        }
        baseCurrencyAmount.setText(currency.getAmount());
        // Move the cursor to the end as if the amount had just been typed.
        baseCurrencyAmount.setSelection(baseCurrencyAmount.length());
        // Rebind RecyclerView items so converted amounts are updated.
        currenciesAdapter.setBaseCurrency(baseCurrency);
        currenciesAdapter.notifyDataSetChanged();

        currencyCalculationsRecyclerView.scrollToPosition(0);
    }

    @Subscribe(threadMode=ThreadMode.BACKGROUND)
    public void onCurrencyDataChanged(CurrencyDataChangeEvent e) {
        Log.v(LOG_TAG, "onCurrencyDataChanged update base currency");
        new BaseCurrencyQuery().execute();
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onSyncComplete(SyncCompleteEvent e) {
        if(swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public CurrencyRepository getCurrencyRepository() {
        return currencyRepository;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public SelectedCurrency getBaseCurrency() {
        return baseCurrency;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SelectedCurrencyLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished " + data);
        currenciesAdapter.swapCursor((SquidCursor)data);
        currenciesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "ONLOADERRESET");
        currenciesAdapter.swapCursor(null);
    }

    public class BaseCurrencyQuery extends AsyncTask<Void, Void, SelectedCurrency> {

        @Override
        protected SelectedCurrency doInBackground(Void... params) {
            return currencyRepository.getBaseCurrency();
        }

        @Override
        protected void onPostExecute(SelectedCurrency currency) {
            Log.v(LOG_TAG, "Base currency is " + currency);
            if (currency != null) {
                displayBaseCurrency(currency, currencyMetaRepository.findByCode(currency.getCode()));
            }
        }
    }
}
