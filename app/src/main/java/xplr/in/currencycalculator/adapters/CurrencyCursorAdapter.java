package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.yahoo.squidb.data.SquidCursor;

import xplr.in.currencycalculator.activities.CurrencyListActivity;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;

/**
 * Adapter for both currency list screens.
 *
 * To manage multiple view types override getItemViewType and getViewTypeCount
 * Created by cheriot on 4/5/16.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    public static String LOG_TAG = CurrencyCursorAdapter.class.getSimpleName();

    private int listItemLayout;
    private final CurrencyListActivity currencyListActivity;
    private final CurrencyMetaRepository currencyMetaRepository;

    public CurrencyCursorAdapter(Context context, int listItemLayout, CurrencyMetaRepository currencyMetaRepository) {
        super(context, null, 0);
        this.listItemLayout = listItemLayout;
        this.currencyListActivity = (CurrencyListActivity)context;
        this.currencyMetaRepository = currencyMetaRepository;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater
                .from(context)
                .inflate(listItemLayout, parent, false);
        view.setTag(new SelectableCurrencyViewHolder(view));
        return view;
    }

    public Currency getCurrency(int position) {
        Currency currency = new Currency();
        currency.readPropertiesFromCursor((SquidCursor)getItem(position));
        return currency;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SelectedCurrency currency = new SelectedCurrency();
        currency.readPropertiesFromCursor((SquidCursor)cursor);

        SelectableCurrencyViewHolder viewHolder = (SelectableCurrencyViewHolder) view.getTag();
        viewHolder.bindView(currency);
    }
}
