package xplr.in.currencycalculator.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import xplr.in.currencycalculator.App;

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

    private CurrencySyncAdapter currencySyncAdapter = null;

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
        ((App)getApplication()).getAppComponent().inject(this);
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
