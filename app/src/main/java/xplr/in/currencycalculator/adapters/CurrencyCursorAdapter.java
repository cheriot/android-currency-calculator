package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.activities.CurrencyListActivity;
import xplr.in.currencycalculator.databases.Currency;
import xplr.in.currencycalculator.databases.SelectedCurrency;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    public static String LOG_TAG = CurrencyCursorAdapter.class.getCanonicalName();

    private int listItemLayout;
    private CurrencyListActivity currencyListActivity;

    public CurrencyCursorAdapter(Context context, int listItemLayout) {
        super(context, null, 0);
        this.listItemLayout = listItemLayout;
        this.currencyListActivity = (CurrencyListActivity)context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Is newView called significantly less than bindView? If so, create a view holder and
        // use setTag/getTag.
        return LayoutInflater
                .from(context)
                .inflate(listItemLayout, parent, false);
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

        TextView codeText = (TextView) view.findViewById(R.id.currency_code);
        TextView rateText = (TextView) view.findViewById(R.id.currency_rate);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.currency_selected);

        if(codeText != null) codeText.setText(currency.getCode());
        if(checkBox != null) checkBox.setChecked(currency.isSelected());

        SelectedCurrency baseCurrency = this.currencyListActivity.getBaseCurrency();
        if(baseCurrency != null && baseCurrency.getAmount() != null && rateText != null) {
            rateText.setText(currency.convertFrom(baseCurrency));
        }
        Log.v(LOG_TAG, "baseCurrency in bindView    " + baseCurrency);
    }
}
