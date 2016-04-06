package xplr.in.currencycalculator;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import xplr.in.currencycalculator.activities.GuiceAppCompatActivity;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.loaders.AllCurrencyLoader;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends GuiceAppCompatActivity implements CurrencyListActivity {

    public static String LOG_TAG = SelectCurrencyActivity.class.getCanonicalName();

    @Inject CurrencyRepository currencyRepository;
    @Inject EventBus eventBus;
    CursorAdapter currenciesAdapter;
    ListView currenciesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currenciesListView = (ListView)findViewById(R.id.list_currency_calculations);
        currenciesAdapter = new CurrencyCursorAdapter(this, R.layout.list_item_selectable_currency);
        currenciesListView.setAdapter(currenciesAdapter);

        currenciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked currency " + position + " " + view.getClass());
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.currency_selected);
                checkBox.setChecked(!checkBox.isChecked());
                persistSelection(position, checkBox);
            }
        });

        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, AllCurrencyLoader.class);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);
    }

    public void onCheckboxClicked(View view) {
        CheckBox checkBox = (CheckBox)view;
        Log.v(LOG_TAG, "Clicked checkbox " + checkBox.isChecked());
        int position = currenciesListView.getPositionForView(checkBox);
        persistSelection(position, checkBox);
    }

    private void persistSelection(int position, CheckBox checkBox) {
        Cursor currencyCursor = (Cursor) currenciesAdapter.getItem(position);
        boolean isSelected = checkBox.isChecked();
        int currencyId = currencyCursor.getInt(currencyCursor.getColumnIndexOrThrow("ID"));
        new PersistCurrencySelection(currencyId, isSelected).execute();
    }

    @Override
    public int getListItemLayout() {
        return R.layout.list_item_selectable_currency;
    }

    @Override
    public CurrencyRepository getCurrencyRepository() {
        return currencyRepository;
    }

    @Override
    public CursorAdapter getCurrencyCursorAdapter() {
        return currenciesAdapter;
    }

    public class PersistCurrencySelection extends AsyncTask<Void, Void, Currency> {

        private int currencyId;
        private boolean isSelected;

        public PersistCurrencySelection(int currencyId, boolean isSelected) {
            this.currencyId = currencyId;
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

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
