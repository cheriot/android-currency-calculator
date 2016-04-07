package xplr.in.currencycalculator.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Ways to trigger currency data to sync: scheduled and immediate.
 *
 * Based on the sample project in
 * http://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 * Created by cheriot on 4/7/16.
 */
public class CurrencySyncTriggers {

    private static final String LOG_TAG = CurrencySyncTriggers.class.getName();
    private static final long SYNC_FREQUENCY = 60 * 60 * 24;  // 1 day (in seconds)
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    public static final String ACCOUNT = "public_account";
    public static final String ACCOUNT_TYPE = "xplr.in.currencycalculator";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public void createSyncAccount(Context context) {
        Log.v(LOG_TAG, "createSyncAccount");
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = getAccount();
        AccountManager accountManager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, StubContentProvider.CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, StubContentProvider.CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, StubContentProvider.CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            syncNow();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public void syncNow() {
        Log.v(LOG_TAG, "syncNow");
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                getAccount(),                            // Sync account
                StubContentProvider.CONTENT_AUTHORITY,   // Content authority
                b);                                      // Extras
    }

    private Account getAccount() {
        return new Account(ACCOUNT, ACCOUNT_TYPE);
    }
}
