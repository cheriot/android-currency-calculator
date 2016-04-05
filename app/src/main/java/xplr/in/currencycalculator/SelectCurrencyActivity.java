package xplr.in.currencycalculator;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.google.inject.Inject;

import xplr.in.currencycalculator.activities.GuiceAppCompatActivity;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends GuiceAppCompatActivity {

    public static String LOG_TAG = SelectCurrencyActivity.class.getCanonicalName();

    @Inject
    CurrencyRepository currencyRepository;
    CursorAdapter currenciesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listCurrencyCalculations = (ListView)findViewById(R.id.list_currency_calculations);

        currenciesAdapter = new CurrencyCursorAdapter(this, null);
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

        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, currencyRepository, currenciesAdapter);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);
    }

}
