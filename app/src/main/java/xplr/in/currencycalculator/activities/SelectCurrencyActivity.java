package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.AllCurrencyLoader;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends AppCompatActivity implements CurrencyListActivity, LoaderManager.LoaderCallbacks<Cursor> {

    public static String LOG_TAG = SelectCurrencyActivity.class.getCanonicalName();
    public final static int CURRENCY_LOADER_ID = 0;

    @Inject CurrencyRepository currencyRepository;
    @Inject EventBus eventBus;
    @Inject @Named("select") CurrencyCursorAdapter currenciesAdapter;

    private CurrencyLoaderCallbacks currencyLoaderCallbacks;
    private String searchQuery = null;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.list_currency_calculations) ListView currenciesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleSearch(getIntent());

        currenciesListView.setAdapter(currenciesAdapter);
        currenciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position + " " + view.getClass());
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.currency_selected);
                checkBox.setChecked(!checkBox.isChecked());
                toggleCurrencySelection(position, checkBox);
            }
        });

        getLoaderManager().initLoader(CURRENCY_LOADER_ID, null, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearch(intent);
    }

    private void handleSearch(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent)) {
            String q = intent.getStringExtra(SearchManager.QUERY);
            // The OnQueryTextListener will prevent the intent from being sent.
            // Handle searches there.
            Log.e(LOG_TAG, "ERROR: " + intent.getAction() + " received, but not handled: " + q);
        }
    }

    public void onCheckboxClicked(View view) {
        CheckBox checkBox = (CheckBox)view;
        Log.v(LOG_TAG, "Clicked checkbox " + checkBox.isChecked());
        int position = currenciesListView.getPositionForView(checkBox);
        toggleCurrencySelection(position, checkBox);
    }

    private void toggleCurrencySelection(int position, CheckBox checkBox) {
        Currency currency = currenciesAdapter.getCurrency(position);
        boolean isSelected = checkBox.isChecked();
        new PersistCurrencySelection(currency, isSelected).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_currency_menu, menu);

        // Associate searchable configuration with the SearchView
        // searchView.setIconifiedByDefault(true) ?
        // item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
        //        | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        // The above two statements from http://developer.android.com/reference/android/app/LoaderManager.html
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return search(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return search(newText);
            }
        });

        return true;
    }

    private boolean search(String query) {
        Log.v(LOG_TAG, "SEARCH CHANGE" + query);
        searchQuery = query;
        getLoaderManager().restartLoader(CURRENCY_LOADER_ID, null, this);
        // Return true to preempt the default behavior or sending an intent.
        currenciesListView.smoothScrollToPosition(0);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AllCurrencyLoader(this, searchQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        currenciesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        currenciesAdapter.swapCursor(null);
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
        return null;
    }

    public class PersistCurrencySelection extends AsyncTask<Void, Void, Currency> {

        private long currencyId;
        private boolean isSelected;

        public PersistCurrencySelection(Currency currency, boolean isSelected) {
            this.currencyId = currency.getId();
            this.isSelected = isSelected;
        }

        @Override
        protected Currency doInBackground(Void... params) {
            Log.v(LOG_TAG, "Update currency " + currencyId + " " + isSelected);
            return currencyRepository.updateSelection(currencyId, isSelected);
        }

        @Override
        protected void onPostExecute(Currency currency) {
            Log.v(LOG_TAG, "Completed update.");
            String message = (currency.isSelected() ? "Selected " : "Removed ") + currency.getCode() + ".";
            Snackbar.make(currenciesListView, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
