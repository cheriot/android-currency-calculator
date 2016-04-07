package xplr.in.currencycalculator.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 *
 * Based on code in http://developer.android.com/training/sync-adapters/creating-authenticator.html
 *
 * Created by cheriot on 4/7/16.
 */
public class StubAuthenticatorService extends Service {

    private StubAuthenticator stubAuthenticator;

    @Override
    public void onCreate() {
        stubAuthenticator = new StubAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return stubAuthenticator.getIBinder();
    }
}
