package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.yahoo.squidb.data.SquidCursor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.adapters.SelectedCurrencyAdapter;
import xplr.in.currencycalculator.loaders.SelectedCurrenciesLoader;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyDataChangeEvent;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;
import xplr.in.currencycalculator.sync.SyncCompleteEvent;

public class MainActivity extends AppCompatActivity implements CurrencyListActivity, LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 1;

    @Inject EventBus eventBus;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    @Inject CurrencyMetaRepository currencyMetaRepository;
    @Inject SelectedCurrencyAdapter currenciesAdapter;
    SelectedCurrency baseCurrency;

    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.list_currency_calculations) RecyclerView listCurrencyCalculations;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_calculator);

        ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        eventBus.register(this);
        new BaseCurrencyQuery().execute();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SelectCurrencyActivity.class));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Make sure to call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                // See the eventBus subscriber on this class.
                currencySyncTriggers.syncNow();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        listCurrencyCalculations.setAdapter(currenciesAdapter);
        listCurrencyCalculations.setLayoutManager(new LinearLayoutManager(this));
        listCurrencyCalculations.setItemAnimator(new DefaultItemAnimator());

        // http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary/
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // no drag and drop
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // Don't swipe away the base currency until that is supported.
                if(viewHolder.getLayoutPosition() == 0) return 0;
                // Return 0 to prevent swipe on the target currency. Two currencies must always be
                // selected (base and target) for the Rate and Trade screens to work.
                if(recyclerView.getAdapter().getItemCount() <= 2) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Log.v(LOG_TAG, "onSwiped " + swipeDir + " " + viewHolder.getItemId());
                ((SelectedCurrencyAdapter.CurrencyViewHolder)viewHolder).onSwipe();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(listCurrencyCalculations);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        // TODO There's a "30 frames skipped" message...
        // Setup the sync account on another thread?
        // Create/Upgrade/Init database on another thread?
        // Init filesystem repositories on another thread?
        // too many bindViews of CurrencyCursorAdapter?

        // Setup a scheduled sync if that hasn't happened already. This will trigger an initial
        // sync if one has not occurred.
        currencySyncTriggers.initSyncAccount(this);
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    private void displayBaseCurrency(SelectedCurrency currency) {
        Log.v(LOG_TAG, "displayBaseCurrency " + currency);
        if (baseCurrency != null && baseCurrency.sameDisplay(currency)) return;
        baseCurrency = currency;

        currenciesAdapter.setBaseCurrency(baseCurrency);
        // Rebind RecyclerView items so converted amounts are updated.
        currenciesAdapter.notifyDataSetChanged();

        listCurrencyCalculations.scrollToPosition(0);
    }

    @Subscribe(threadMode=ThreadMode.BACKGROUND)
    public void onCurrencyDataChanged(CurrencyDataChangeEvent e) {
        Log.v(LOG_TAG, "onCurrencyDataChanged update base currency");
        new BaseCurrencyQuery().execute();
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onSyncComplete(SyncCompleteEvent e) {
        if(swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SelectedCurrenciesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished " + data);
        currenciesAdapter.swapCursor((SquidCursor)data);
        currenciesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset");
        currenciesAdapter.swapCursor(null);
    }

    public class BaseCurrencyQuery extends AsyncTask<Void, Void, SelectedCurrency> {
        @Override
        protected SelectedCurrency doInBackground(Void... params) {
            return currencyRepository.findBaseCurrency();
        }

        @Override
        protected void onPostExecute(SelectedCurrency currency) {
            Log.v(LOG_TAG, "Base currency is " + currency);
            if (currency != null) {
                displayBaseCurrency(currency);
            }
        }
    }
}
