package xplr.in.currencycalculator.presenters;

import android.database.Cursor;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 4/14/16.
 */
public class CurrencyListPresenter {

    private final CurrencyRepository currencyRepository;
    private final EventBus eventBus;

    @Inject
    public CurrencyListPresenter(CurrencyRepository currencyRepository, EventBus eventBus) {
        this.currencyRepository = currencyRepository;
        this.eventBus = eventBus;
    }

    public void swapCursor(Cursor cursor) {
    }
}
