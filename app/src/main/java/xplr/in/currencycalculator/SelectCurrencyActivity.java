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

import xplr.in.currencycalculator.activities.GuiceAppCompatActivity;
import xplr.in.currencycalculator.adapters.CurrencyCursorAdapter;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

public class SelectCurrencyActivity extends GuiceAppCompatActivity {

    public static String LOG_TAG = SelectCurrencyActivity.class.getCanonicalName();

    @Inject
    CurrencyRepository currencyRepository;
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

        CurrencyLoaderCallbacks clc = new CurrencyLoaderCallbacks(this, currencyRepository, currenciesAdapter);
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
        new UpdateCurrenciesFromServer(currencyId, isSelected).execute();
    }

    public class UpdateCurrenciesFromServer extends AsyncTask<Void, Void, Currency> {

        private int currencyId;
        private boolean isSelected;

        public UpdateCurrenciesFromServer(int currencyId, boolean isSelected) {
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
            // currenciesAdapter.notifyDataSetChanged();
            getLoaderManager().getLoader(CurrencyLoaderCallbacks.LOADER_ID).forceLoad();
            String message = (currency.isSelected() ? "Selected " : "Removed ") + currency.getCode() + ".";
            Snackbar.make(currenciesListView, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
