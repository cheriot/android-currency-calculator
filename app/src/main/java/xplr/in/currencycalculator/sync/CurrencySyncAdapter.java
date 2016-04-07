package xplr.in.currencycalculator.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.inject.Inject;

import roboguice.RoboGuice;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Handle the update of currency data using Android's sync adapter framework.
 *
 * Created by cheriot on 4/7/16.
 */
public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject CurrencyRepository currencyRepository;

    public CurrencySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);
    }

    public CurrencySyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        currencyRepository.fetchAll();
    }
}
