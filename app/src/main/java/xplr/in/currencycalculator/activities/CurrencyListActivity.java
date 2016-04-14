package xplr.in.currencycalculator.activities;

import android.widget.CursorAdapter;

import org.greenrobot.eventbus.EventBus;

import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/6/16.
 */
public interface CurrencyListActivity {

    CurrencyRepository getCurrencyRepository();

    CursorAdapter getCurrencyCursorAdapter();

    EventBus getEventBus();

    SelectedCurrency getBaseCurrency();
}
