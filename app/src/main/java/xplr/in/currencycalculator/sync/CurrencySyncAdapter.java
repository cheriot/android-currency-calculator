package xplr.in.currencycalculator.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.repositories.CurrencyBulkRepository;

/**
 * Handle the update of currency data using Android's sync adapter framework.
 *
 * Created by cheriot on 4/7/16.
 */
public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {

    private final CurrencyBulkRepository currencyBulkRepository;
    private final Analytics analytics;

    public CurrencySyncAdapter(
            App context,
            boolean autoInitialize,
            boolean parallelSyncs,
            CurrencyBulkRepository currencyBulkRepository,
            Analytics analytics) {
        super(context, autoInitialize, parallelSyncs);
        this.currencyBulkRepository = currencyBulkRepository;
        this.analytics = analytics;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        // If there's a way to distinguish scheduled syncs from manually triggered syncs, do that.
        boolean isManual = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        analytics.getSyncAnalytics().recordSync(isManual);
        currencyBulkRepository.updateFromRemote();
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        // Cancel the request?
        // "If your adapter does not respect the cancel issued by the framework you run the risk of your app's entire process being killed."
        // - http://developer.android.com/reference/android/content/AbstractThreadedSyncAdapter.html
    }
}
