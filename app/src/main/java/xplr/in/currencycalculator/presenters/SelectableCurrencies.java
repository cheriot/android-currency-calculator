package xplr.in.currencycalculator.presenters;

import android.database.Cursor;

import com.yahoo.squidb.data.SquidCursor;

import java.util.List;

import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 5/30/16.
 */
public class SelectableCurrencies {

    private static final String LOG_TAG = SelectableCurrencies.class.getSimpleName();
    private static final int MIN_SELECTED = 2;

    private final List<Currency> popular;
    private final Cursor all;
    private final int selectedCount;
    // If we limit the display by a search query then hide headers and the popular list.
    private final boolean isSearching;

    public SelectableCurrencies(List<Currency> popular, Cursor all, int selectedCount, boolean isSearching) {
        this.popular = popular;
        this.all = all;
        this.selectedCount = selectedCount;
        this.isSearching = isSearching;
    }

    public void close() {
        all.close();
    }

    public int getCount() {
        if(isSearching) {
            return all.getCount();
        } else {
            return popular.size() + all.getCount() + 2; // +2 for the headers
        }
    }

    public boolean allowDeselect() {
        return selectedCount - MIN_SELECTED > 0;
    }

    public boolean isHeader(int displayPosition) {
        // let popular.size = 2
        // 0: header
        // 1: currency
        // 2: currency
        // 3: header
        // ...: currency
        // ...: currency
        return !isSearching && (displayPosition == 0 || displayPosition == popular.size()+1);
    }

    public boolean isPopularHeader(int displayPosition) {
        return !isSearching && displayPosition == 0;
    }

    public Currency getCurrency(int displayPosition) {
        if(isSearching) {
            // We are only showing the all list so displayPosition == queryPosition.
            return getCurrencyFromAll(displayPosition);
        }
        else if(displayPosition <= popular.size()) {
            return popular.get(displayPosition-1); // -1 to account for the first header
        } else {
            int queryPosition = displayPosition - 2 - popular.size(); // -2 for headers
            return getCurrencyFromAll(queryPosition);
        }
    }

    private Currency getCurrencyFromAll(int queryPosition) {
        Currency currency = new Currency();
        SquidCursor cursor = (SquidCursor)all;
        cursor.moveToPosition(queryPosition);
        currency.readPropertiesFromCursor(cursor);
        return currency;
    }
}
