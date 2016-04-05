package xplr.in.currencycalculator;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
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

import java.util.List;

import xplr.in.currencycalculator.activities.GuiceAppCompatActivity;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.WorkingAsyncTaskLoader;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class MainActivity extends GuiceAppCompatActivity {

    public static String LOG_TAG = MainActivity.class.getCanonicalName();

    @Inject
    CurrencyRepository currencyRepository;
    CursorAdapter currenciesAdapter;
    CurrencyLoaderCallbacks currencyLoaderCallbacks;

    public MainActivity() {
        currencyLoaderCallbacks = new CurrencyLoaderCallbacks(this);
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

        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, currencyLoaderCallbacks);
        new UpdateCurrenciesFromServer().execute();
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
            new UpdateCurrenciesFromServer().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    public class CurrencyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        public final static int LOADER_ID = 0;
        private Context context;

        public CurrencyLoaderCallbacks(Context context) {
            this.context = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CurrencyLoader(context, currencyRepository);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            currenciesAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            currenciesAdapter.swapCursor(null);
        }
    }

    public static class CurrencyLoader extends WorkingAsyncTaskLoader<Cursor> {

        private CurrencyRepository currencyRepository;

        public CurrencyLoader(Context context, CurrencyRepository currencyRepository) {
            super(context);
            this.currencyRepository = currencyRepository;
        }

        @Override
        protected void releaseResources(Cursor data) {
            if(!data.isClosed()) data.close();
        }

        @Override
        public Cursor loadInBackground() {
            return currencyRepository.getSelectedCursor();
        }
    }

    public class UpdateCurrenciesFromServer extends AsyncTask<Void, Void, List<Currency>> {
        @Override
        protected List<Currency> doInBackground(Void... params) {
            // Call into a repository class that will make the network call and construct java
            // objects
            return currencyRepository.fetchAll();
        }

        @Override
        protected void onPostExecute(List<Currency> currencies) {
            Log.v(LOG_TAG, "Received currency update.");
            currenciesAdapter.notifyDataSetChanged();
        }
    }
}
