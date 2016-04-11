package xplr.in.currencycalculator.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.databases.Currency;
import xplr.in.currencycalculator.databases.SelectedCurrency;
import xplr.in.currencycalculator.loaders.AllCurrencyLoader;
import xplr.in.currencycalculator.loaders.CurrencyLoaderCallbacks;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends AppCompatActivity implements CurrencyListActivity {

    public static String LOG_TAG = SelectCurrencyActivity.class.getCanonicalName();

    @Inject CurrencyRepository currencyRepository;
    @Inject EventBus eventBus;
    CurrencyCursorAdapter currenciesAdapter;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.list_currency_calculations) ListView currenciesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        ((App)getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currenciesAdapter = new CurrencyCursorAdapter(this, R.layout.list_item_selectable_currency);
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

        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, AllCurrencyLoader.class);
        getLoaderManager().initLoader(CurrencyLoaderCallbacks.LOADER_ID, null, clc);
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
    public CurrencyRepository getCurrencyRepository() {
        return currencyRepository;
    }

    @Override
    public CursorAdapter getCurrencyCursorAdapter() {
        return currenciesAdapter;
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

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public SelectedCurrency getBaseCurrency() {
        return null;
    }
}
