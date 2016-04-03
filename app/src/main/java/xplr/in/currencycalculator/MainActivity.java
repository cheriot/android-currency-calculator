package xplr.in.currencycalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = MainActivity.class.getCanonicalName();

    private ArrayAdapter<Currency> currenciesAdapter;

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

        currenciesAdapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_currency_calculation,
                R.id.list_item_currency_calculation_text_view,
                new ArrayList<Currency>());
        listCurrencyCalculations.setAdapter(currenciesAdapter);

        listCurrencyCalculations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position);
                Currency currency = currenciesAdapter.getItem(position);
                Snackbar.make(view, currency.getCode(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new FetchCurrencyRates().execute();
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
            new FetchCurrencyRates().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchCurrencyRates extends AsyncTask<Void, Void, List<Currency>> {

        @Override
        protected List<Currency> doInBackground(Void... params) {
            // Call into a repository class that will make the network call and construct java
            // objects
            return CurrencyRepository.fetchAll();
        }

        @Override
        protected void onPostExecute(List<Currency> currencies) {
            Log.v(LOG_TAG, "Received currency update.");
            currenciesAdapter.clear();
            currenciesAdapter.addAll(currencies);
        }
    }
}
