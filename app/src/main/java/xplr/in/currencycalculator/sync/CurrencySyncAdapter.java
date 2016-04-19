package xplr.in.currencycalculator.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.SyncResult;
import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.repositories.CurrencyBulkRepository;

/**
 * Handle the update of currency data using Android's sync adapter framework.
 *
 * Created by cheriot on 4/7/16.
 */
@Singleton
public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    CurrencyBulkRepository currencyBulkRepository;

    public CurrencySyncAdapter(
            App context,
            boolean autoInitialize,
            boolean parallelSyncs,
            CurrencyBulkRepository currencyBulkRepository) {
        super(context, autoInitialize, parallelSyncs);
        this.currencyBulkRepository = currencyBulkRepository;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        currencyBulkRepository.updateFromRemote();
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        // TODO: cancel the request?
        // "If your adapter does not respect the cancel issued by the framework you run the risk of your app's entire process being killed."
        // - http://developer.android.com/reference/android/content/AbstractThreadedSyncAdapter.html
    }
}
