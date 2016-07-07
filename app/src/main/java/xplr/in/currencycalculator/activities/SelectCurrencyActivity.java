package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencySelectionChangeListener;
import xplr.in.currencycalculator.adapters.SelectCurrencyCombinedAdapter;
import xplr.in.currencycalculator.loaders.SelectableCurrenciesLoader;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.presenters.SelectableCurrencies;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.repositories.PopularCurrenciesRepository;

public class SelectCurrencyActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<SelectableCurrencies>, CurrencySelectionChangeListener {

    private static final String LOG_TAG = SelectCurrencyActivity.class.getSimpleName();
    private final static int COMBINED_CURRENCIES_LOADER_ID = 2;
    public final static int INSERT_RESULT_CODE = 1;
    public final static int REMOVE_RESULT_CODE = 2;
    public final static String PARAM_POSITION = "position";

    @Inject PopularCurrenciesRepository popularCurrenciesRepository;
    @Inject CurrencyRepository currencyRepository;
    @Inject EventBus eventBus;
    @Inject SelectCurrencyCombinedAdapter currenciesAdapter;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.list_selectable_currencies) RecyclerView listSelectableCurrencies;

    // Temporary variable to hold query between when the user types and when the LoaderManager
    // triggers a new fetch.
    private String searchQuery =  null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);
        
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listSelectableCurrencies.setAdapter(currenciesAdapter);
        listSelectableCurrencies.setLayoutManager(new LinearLayoutManager(this));
        listSelectableCurrencies.setItemAnimator(new DefaultItemAnimator());

        getLoaderManager().initLoader(COMBINED_CURRENCIES_LOADER_ID, null, this);
    }

    @Override
    public Loader<SelectableCurrencies> onCreateLoader(int id, Bundle args) {
        return new SelectableCurrenciesLoader(this, popularCurrenciesRepository, currencyRepository, searchQuery);
    }

    @Override
    public void onLoadFinished(Loader<SelectableCurrencies> loader, SelectableCurrencies data) {
        currenciesAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<SelectableCurrencies> loader) {
        currenciesAdapter.resetData();
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
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        searchView.setImeOptions(EditorInfo.IME_ACTION_NONE|EditorInfo.IME_FLAG_FORCE_ASCII);
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
        Log.v(LOG_TAG, "SEARCH CHANGE " + query);
        searchQuery = query;
        getLoaderManager().restartLoader(COMBINED_CURRENCIES_LOADER_ID, null, this);
        if(!TextUtils.isEmpty(query)) {
            listSelectableCurrencies.smoothScrollToPosition(0);
        }
        // Return true to preempt the default behavior of sending an intent.
        return true;
    }

    @Override
    public void onCurrencySelectionChange(Currency currency, boolean isSelected) {
        new PersistCurrencySelection(currency, isSelected).execute();
    }

    public class PersistCurrencySelection extends AsyncTask<Void, Void, Currency> {
        private long currencyId;
        private Integer originalPosition;
        private Integer updatedPosition;
        private boolean isSelected;

        public PersistCurrencySelection(Currency currency, boolean isSelected) {
            this.currencyId = currency.getId();
            Log.v(LOG_TAG, "Persist " + currency);
            this.originalPosition = currency.getPosition();
            this.isSelected = isSelected;
        }

        @Override
        protected Currency doInBackground(Void... params) {
            Log.v(LOG_TAG, "Update currency " + currencyId + " " + isSelected);
            Currency updated = currencyRepository.updateSelection(currencyId, isSelected);
            this.updatedPosition = updated.getPosition();
            // Give the checkbox animation time to finish before onBackPressed() takes the user
            SystemClock.sleep(200);
            return updated;
        }

        @Override
        protected void onPostExecute(Currency currency) {
            Log.v(LOG_TAG, "Completed update.");
            // Will this loader get reused (need restart)?
            getLoaderManager().restartLoader(COMBINED_CURRENCIES_LOADER_ID, null, SelectCurrencyActivity.this);
            Intent intent = new Intent();
            if(isSelected) {
                intent.putExtra(PARAM_POSITION, this.updatedPosition);
                setResult(INSERT_RESULT_CODE, intent);
            } else {
                intent.putExtra(PARAM_POSITION, this.originalPosition);
                setResult(REMOVE_RESULT_CODE, intent);
            }
            finish();
        }
    }
}
