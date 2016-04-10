package xplr.in.currencycalculator.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.yahoo.squidb.data.SquidCursor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.databases.Currency;
import xplr.in.currencycalculator.databases.SelectedCurrency;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.loaders.SelectedCurrencyLoader;
import xplr.in.currencycalculator.repositories.CurrencyDataChangeEvent;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;

public class MainActivity extends GuiceAppCompatActivity implements CurrencyListActivity {

    private static String LOG_TAG = MainActivity.class.getCanonicalName();

    @Inject EventBus eventBus;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    CursorAdapter currenciesAdapter;
    SelectedCurrency baseCurrency;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        final Activity context = this;

        setContentView(R.layout.activity_main_calculator);

        eventBus.register(this);
        new BaseCurrencyQuery().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, SelectCurrencyActivity.class));
            }
        });

        final TextView baseCurrencyAmountView = (TextView)findViewById(R.id.base_currency_amount);
        baseCurrencyAmountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(LOG_TAG, "TEXT " + s);
                baseCurrency.setAmount(s.toString());
                currenciesAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Connect ListView to its Adapter
        ListView listCurrencyCalculations = (ListView)findViewById(R.id.list_currency_calculations);
        currenciesAdapter = new CurrencyCursorAdapter(this, R.layout.list_item_currency_calculation);
        listCurrencyCalculations.setAdapter(currenciesAdapter);
        listCurrencyCalculations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position);
                SquidCursor currencyCursor = (SquidCursor) currenciesAdapter.getItem(position);
                Currency currency = new Currency();
                currency.readPropertiesFromCursor(currencyCursor);
                currencyRepository.setBaseCurrency(currency);
            }
        });

        // Initialize Loader & its handler.
        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, SelectedCurrencyLoader.class);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);

        // TODO There's a "30 frames skipped" message. Setup the sync account on another thread?

        // Setup a scheduled sync if that hasn't happened already. This will trigger an initial
        // sync if one has not occurred.
        currencySyncTriggers.createSyncAccount(this);
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    private void setBaseCurrency(SelectedCurrency currency) {
        if (baseCurrency != null && baseCurrency.getCode().equals(currency.getCode())) return;

        Log.v(LOG_TAG, "setBaseCurrency " + currency.getCode());
        TextView codeView = (TextView)findViewById(R.id.base_currency_code);
        codeView.setText(currency.getCode());
        baseCurrency = currency;

        // Rebind ListView items so converted amounts can be calculated.
        currenciesAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onCurrencyDataChanged(CurrencyDataChangeEvent e) {
        Log.v(LOG_TAG, "onCurrencyDataChanged update base currency");
        new BaseCurrencyQuery().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_calculator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.action_refresh) {
            currencySyncTriggers.syncNow();
        }
        return super.onOptionsItemSelected(item);
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
            if (currency != null) setBaseCurrency(currency);
        }
    }
}
