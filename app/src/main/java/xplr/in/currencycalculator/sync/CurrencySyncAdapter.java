package xplr.in.currencycalculator.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.SyncResult;
import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Handle the update of currency data using Android's sync adapter framework.
 *
 * Created by cheriot on 4/7/16.
 */
@Singleton
public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    CurrencyRepository currencyRepository;

    public CurrencySyncAdapter(
            App context,
            boolean autoInitialize,
            boolean parallelSyncs,
            CurrencyRepository currencyRepository) {
        super(context, autoInitialize, parallelSyncs);
        this.currencyRepository = currencyRepository;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        currencyRepository.updateFromRemote();
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        // TODO: cancel the request?
        // "If your adapter does not respect the cancel issued by the framework you run the risk of your app's entire process being killed."
        // - http://developer.android.com/reference/android/content/AbstractThreadedSyncAdapter.html
    }
}
