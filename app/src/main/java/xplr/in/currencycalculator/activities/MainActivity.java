package xplr.in.currencycalculator.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.loaders.SelectedCurrencyLoader;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyDataChangeEvent;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;
import xplr.in.currencycalculator.sync.SyncCompleteEvent;

public class MainActivity extends AppCompatActivity implements CurrencyListActivity {

    private static String LOG_TAG = MainActivity.class.getCanonicalName();

    @Inject EventBus eventBus;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    @Inject CurrencyMetaRepository currencyMetaRepository;
    @Inject @Named("calculate") CurrencyCursorAdapter currenciesAdapter;
    SelectedCurrency baseCurrency;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.base_currency_code) TextView baseCurrencyCode;
    @Bind(R.id.base_currency_amount) EditText baseCurrencyAmount;
    @Bind(R.id.base_currency_flag) ImageView baseCurrencyFlag;
    @Bind(R.id.list_currency_calculations) ListView currencyCalculationsListView;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_calculator);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        eventBus.register(this);
        new BaseCurrencyQuery().execute();

        setSupportActionBar(toolbar);

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
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        currencyCalculationsListView.setAdapter(currenciesAdapter);
        currencyCalculationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position);
                SquidCursor currencyCursor = (SquidCursor) currenciesAdapter.getItem(position);
                SelectedCurrency currency = new SelectedCurrency();
                currency.readPropertiesFromCursor(currencyCursor);
                // This currency's amount will become the new selected amount.
                currency.convertFrom(baseCurrency);
                String formattedAmount = ((TextView)view.findViewById(R.id.currency_rate))
                        .getText()
                        .toString();
                currency.parse(formattedAmount);
                currencyRepository.setBaseCurrency(currency);
                currencyCalculationsListView.smoothScrollToPosition(0);
            }
        });

        // Initialize Loader & its handler.
        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, SelectedCurrencyLoader.class);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);

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

    private void displayBaseCurrency(SelectedCurrency currency, CurrencyMeta meta) {
        if (baseCurrency != null
                && baseCurrency.sameDisplay(currency)) return;

        Log.v(LOG_TAG, "displayBaseCurrency " + currency.getCode());
        baseCurrency = currency;
        baseCurrencyCode.setText(currency.getCode() + " " + currency.getName());
        if(meta != null) {
            Drawable drawable = getResources().getDrawable(meta.getFlagResourceId());
            baseCurrencyFlag.setImageDrawable(drawable);
        }
        baseCurrencyAmount.setText(currency.getAmount());
        // Move the cursor to the end as if the amount had just been typed.
        baseCurrencyAmount.setSelection(baseCurrencyAmount.length());
        // Rebind ListView items so converted amounts are updated.
        currenciesAdapter.notifyDataSetChanged();
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
    public CursorAdapter getCurrencyCursorAdapter() {
        return currenciesAdapter;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public SelectedCurrency getBaseCurrency() {
        return baseCurrency;
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
