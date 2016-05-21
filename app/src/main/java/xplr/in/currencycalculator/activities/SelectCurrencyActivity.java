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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyArrayAdapter;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.AllCurrencyLoader;
import xplr.in.currencycalculator.loaders.PopularCurrencyLoader;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends AppCompatActivity implements CurrencyListActivity {

    private static String LOG_TAG = SelectCurrencyActivity.class.getSimpleName();
    private final static int ALL_CURRENCIES_LOADER_ID = 0;
    private final static int POPULAR_CURRENCIES_LOADER_ID = 1;

    private final LoaderManager.LoaderCallbacks<Cursor> allCurrenciesCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AllCurrencyLoader(SelectCurrencyActivity.this, searchQuery);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            currenciesAdapter.swapCursor(data);
            ListUtils.setDynamicHeight(allListView);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            currenciesAdapter.swapCursor(null);
            ListUtils.setDynamicHeight(allListView);
        }
    };

    private final LoaderManager.LoaderCallbacks<List<Currency>> popularCurrenciesCallbacks = new LoaderManager.LoaderCallbacks<List<Currency>>() {
        @Override
        public Loader<List<Currency>> onCreateLoader(int id, Bundle args) {
            return popularCurrencyLoader;
        }

        @Override
        public void onLoadFinished(Loader<List<Currency>> loader, List<Currency> data) {
            suggestedListView.setAdapter(new CurrencyArrayAdapter(SelectCurrencyActivity.this, R.layout.list_item_currency_select, data));
            ListUtils.setDynamicHeight(suggestedListView);
        }

        @Override
        public void onLoaderReset(Loader<List<Currency>> loader) {
            suggestedListView.setAdapter(new CurrencyArrayAdapter(SelectCurrencyActivity.this, R.layout.list_item_currency_select, Collections.<Currency>emptyList()));
            ListUtils.setDynamicHeight(suggestedListView);
        }
    };

    @Inject CurrencyRepository currencyRepository;
    @Inject PopularCurrencyLoader popularCurrencyLoader;
    @Inject EventBus eventBus;
    @Inject @Named("select") CurrencyCursorAdapter currenciesAdapter;
    // @Inject CurrencyArrayAdapter currencyArrayAdapter;

    private String searchQuery = null;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.list_suggested_currencies) ListView suggestedListView;
    @Bind(R.id.list_all_currencies) ListView allListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);
        
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleSearch(getIntent());

        allListView.setAdapter(currenciesAdapter);
        allListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggleAllListSelection(view, position);
            }
        });
        ListUtils.setDynamicHeight(allListView);

        suggestedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggleSuggestedListSelection(view, position);
            }
        });

        getLoaderManager().initLoader(ALL_CURRENCIES_LOADER_ID, null, allCurrenciesCallbacks);
        getLoaderManager().initLoader(POPULAR_CURRENCIES_LOADER_ID, null, popularCurrenciesCallbacks);
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

    private void toggleAllListSelection(View view, int position) {
        Log.v(LOG_TAG, "Clicked currency " + position + " " + view.getClass());
        CheckBox checkBox = toggleCheckbox(view);
        Currency currency = currenciesAdapter.getCurrency(position);
        persistCheckboxState(checkBox, currency);
    }

    private void toggleSuggestedListSelection(View view, int position) {
        Log.v(LOG_TAG, "Clicked currency " + position + " " + view.getClass());
        CheckBox checkBox = toggleCheckbox(view);
        Currency currency = (Currency)suggestedListView.getAdapter().getItem(position);
        persistCheckboxState(checkBox, currency);
    }

    private CheckBox toggleCheckbox(View view) {
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.currency_selected);
        checkBox.setChecked(!checkBox.isChecked());
        return checkBox;
    }

    private void persistCheckboxState(CheckBox checkBox, Currency currency) {
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
        Log.v(LOG_TAG, "SEARCH CHANGE" + query);
        searchQuery = query;
        getLoaderManager().restartLoader(ALL_CURRENCIES_LOADER_ID, null, allCurrenciesCallbacks);
        allListView.smoothScrollToPosition(0);
        // Return true to preempt the default behavior of sending an intent.
        return true;
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
            Snackbar.make(allListView, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * If this gets any more complicated, use a single recylcerview whose adapter has multiple
     * view types.
     */
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null || mListAdapter.getCount() <= 0) {
                return;
            }

            // Height of the list *** assuming all list items are the same height ***
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            View firstListItem = mListAdapter.getView(0, null, mListView);
            firstListItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            int height = firstListItem.getMeasuredHeight() * mListAdapter.getCount();

            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
}
