package xplr.in.currencycalculator.activities;

import org.greenrobot.eventbus.EventBus;

import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/6/16.
 */
public interface CurrencyListActivity {

    CurrencyRepository getCurrencyRepository();

    EventBus getEventBus();

    SelectedCurrency getBaseCurrency();
}
