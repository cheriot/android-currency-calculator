package xplr.in.currencycalculator.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 *
 * Based on http://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 * Created by cheriot on 4/7/16.
 */
public class CurrencySyncService extends Service {

    private static CurrencySyncAdapter currencySyncAdapter = null;
    private static final Object syncAdapterLock = new Object();

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (syncAdapterLock) {
            if (currencySyncAdapter == null) {
                currencySyncAdapter = new CurrencySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return currencySyncAdapter.getSyncAdapterBinder();
    }
}
