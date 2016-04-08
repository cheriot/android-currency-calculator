package xplr.in.currencycalculator.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.loaders.SelectedCurrencyLoader;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;

public class MainActivity extends GuiceAppCompatActivity implements CurrencyListActivity {

    private static String LOG_TAG = MainActivity.class.getCanonicalName();

    @Inject CurrencyRepository currencyRepository;
    @Inject EventBus eventBus;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    CursorAdapter currenciesAdapter;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity context = this;

        setContentView(R.layout.activity_main_calculator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SelectCurrencyActivity.class);
                startActivity(intent);
            }
        });

        ListView listCurrencyCalculations = (ListView)findViewById(R.id.list_currency_calculations);

        currenciesAdapter = new CurrencyCursorAdapter(this, R.layout.list_item_currency_calculation);
        listCurrencyCalculations.setAdapter(currenciesAdapter);

        listCurrencyCalculations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position);
                Cursor currencyCursor = (Cursor) currenciesAdapter.getItem(position);
                String code = currencyCursor.getString(currencyCursor.getColumnIndexOrThrow("CODE"));
                Snackbar.make(view, code, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, SelectedCurrencyLoader.class);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);

        // TODO There's a "30 frames skipped" message. Setup the sync account on another thread?

        // Setup a scheduled sync if that hasn't happened already. This will trigger an initial
        // sync if one has not occurred.
        // TODO: EventBus events are not received after sync
        currencySyncTriggers.createSyncAccount(this);
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
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            currencySyncTriggers.syncNow();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getListItemLayout() {
        return R.layout.list_item_currency_calculation;
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
}
