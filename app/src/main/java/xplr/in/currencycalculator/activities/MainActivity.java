package xplr.in.currencycalculator.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import xplr.in.currencycalculator.adapters.OnItemDragListener;
import xplr.in.currencycalculator.adapters.PendingNotify;
import xplr.in.currencycalculator.adapters.SelectedCurrencyAdapter;
import xplr.in.currencycalculator.adapters.SelectedCurrencyItemAnimator;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.loaders.SelectedCurrenciesLoader;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.sync.CurrencySyncTriggers;
import xplr.in.currencycalculator.sync.SyncCompleteEvent;

public class MainActivity extends AppCompatActivity
        implements CurrencyListActivity, LoaderManager.LoaderCallbacks<Cursor>, OnItemDragListener {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int LOADER_ID = 1;
    private static final int SELECT_CURRENCY_REQUEST_CODE = 1;

    @Inject EventBus eventBus;
    @Inject CurrencyRepository currencyRepository;
    @Inject CurrencySyncTriggers currencySyncTriggers;
    @Inject CurrencyMetaRepository currencyMetaRepository;
    @Inject SelectedCurrencyAdapter currenciesAdapter;
    @Inject Analytics analytics;

    private ItemTouchHelper itemTouchHelper;

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analytics.getMainActivityAnalytics().recordFabClick();
                startActivityForResult(
                        new Intent(MainActivity.this, SelectCurrencyActivity.class),
                        SELECT_CURRENCY_REQUEST_CODE);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                analytics.getMainActivityAnalytics().recordRefreshRequested();
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
        listCurrencyCalculations.setItemAnimator(new SelectedCurrencyItemAnimator(SelectedCurrencyAdapter.ACTIONS_TYPE_POSITION));

        itemTouchHelper = new ItemTouchHelper(new CalculatorItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listCurrencyCalculations);
        currenciesAdapter.setOnItemDragListener(this);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        // TODO There's a "30 frames skipped" message on the emulator...
        // Setup the sync account on another thread?
        // Create/Upgrade/Init database on another thread?
        // Init filesystem repositories on another thread?
        // too many bindViews of CurrencyCursorAdapter?

        // Setup a scheduled sync if that hasn't happened already. This will trigger an initial
        // sync if one has not occurred.
        currencySyncTriggers.initSyncAccount(this);

        analytics.recordUserDefaultLocale();
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Keep
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "onActivityResult " + requestCode + " " + resultCode + " " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_CURRENCY_REQUEST_CODE) {
            int position = data.getIntExtra(SelectCurrencyActivity.PARAM_POSITION, -1);
            if(resultCode == SelectCurrencyActivity.INSERT_RESULT_CODE) {
                currenciesAdapter.pendingNotifyCurrencyInserted(position);
            }
            if(resultCode == SelectCurrencyActivity.REMOVE_RESULT_CODE) {
                currenciesAdapter.pendingNotifyCurrencyRemoved(position);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SelectedCurrenciesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        SquidCursor cursor = (SquidCursor)data;
        currenciesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset");
        currenciesAdapter.swapCursor(null);
    }

    @Override
    public void onItemDrag(RecyclerView.ViewHolder viewHolder) {
        Log.v(LOG_TAG, "onItemDrag " + viewHolder.getAdapterPosition() + " " + viewHolder.itemView.getTop());
        itemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Specify and coordinate swipe and drag.
     */
    private class CalculatorItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {
        public CalculatorItemTouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Log.v(LOG_TAG, "onMove " + viewHolder.getAdapterPosition() + " to " + target.getAdapterPosition());

            Currency inMotionCurrency = ((SelectedCurrencyAdapter.AbstractCurrencyViewHolder)viewHolder).getCurrency();
            Currency destinationCurrency = ((SelectedCurrencyAdapter.AbstractCurrencyViewHolder)target).getCurrency();
            // FYI, returning false tells ItemTouchHelper to recall this method.
            if(destinationCurrency == null) return false; // Moving past the action buttons.
            if(currenciesAdapter.hasPendingNotifies()) return false; // There may be a swap in progress.
            currencyRepository.swap(inMotionCurrency, destinationCurrency);
            int swapOriginPosition = viewHolder.getAdapterPosition();
            int swapDestinationPosition = target.getAdapterPosition();
            currenciesAdapter.pendingNotifyItemMovedWithFixedRow(swapOriginPosition, swapDestinationPosition);
            return true;
        }

        @Override
        public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(viewHolder.getLayoutPosition() == SelectedCurrencyAdapter.ACTIONS_TYPE_POSITION) return 0;
            return super.getDragDirs(recyclerView, viewHolder);
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            // Don't swipe away the base currency until that is supported.
            if(viewHolder.getLayoutPosition() == SelectedCurrencyAdapter.BASE_CURRENCY_TYPE_POSITION) return 0;
            // Don't swipe away the action buttons.
            if(viewHolder.getLayoutPosition() == SelectedCurrencyAdapter.ACTIONS_TYPE_POSITION) return 0;
            // Return 0 to prevent swipe on the targetCurrency currency. Two currencies must always be
            // selected (base and targetCurrency) for the Rate and Trade screens to work.
            // +1 for the action buttons
            if(recyclerView.getAdapter().getItemCount() <= 3) return 0;
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            Log.v(LOG_TAG, "onSwiped " + swipeDir + " " + viewHolder.getItemId());
            ((SelectedCurrencyAdapter.CurrencyViewHolder)viewHolder).onSwipe();
            currenciesAdapter.addPendingNotify(new PendingNotify.Removed(viewHolder.getAdapterPosition()));
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            // swipeRefreshLayout interferes with drag/swipe of RecyclerView rows. Turn it off
            // during RecyclerView changes.
            swipeRefreshLayout.setEnabled(viewHolder == null);
            super.onSelectedChanged(viewHolder, actionState);
        }
    }
}
