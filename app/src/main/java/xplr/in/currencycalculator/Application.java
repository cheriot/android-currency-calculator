package xplr.in.currencycalculator;

import com.orm.SugarContext;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

}
