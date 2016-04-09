package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.databases.Currency;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    public static String LOG_TAG = CurrencyCursorAdapter.class.getCanonicalName();

    private int listItemLayout;

    public CurrencyCursorAdapter(Context context, int listItemLayout) {
        super(context, null, 0);
        this.listItemLayout = listItemLayout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
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
        Currency currency = new Currency();
        currency.readPropertiesFromCursor((SquidCursor)cursor);

        TextView codeText = (TextView) view.findViewById(R.id.currency_code);
        TextView rateText = (TextView) view.findViewById(R.id.currency_rate);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.currency_selected);

        if(codeText != null) codeText.setText(currency.getCode());
        if(rateText != null) rateText.setText(currency.getRate());
        if(checkBox != null) checkBox.setChecked(currency.isSelected());
    }
}
