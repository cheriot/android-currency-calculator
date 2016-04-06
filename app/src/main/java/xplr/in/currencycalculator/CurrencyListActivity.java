package xplr.in.currencycalculator;

import android.widget.CursorAdapter;

import org.greenrobot.eventbus.EventBus;

import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/6/16.
 */
public interface CurrencyListActivity {

    int getListItemLayout();

    CurrencyRepository getCurrencyRepository();

    CursorAdapter getCurrencyCursorAdapter();

    EventBus getEventBus();
}
