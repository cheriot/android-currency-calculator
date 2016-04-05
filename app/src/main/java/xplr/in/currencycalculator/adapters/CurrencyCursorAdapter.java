package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    public static String LOG_TAG = CurrencyCursorAdapter.class.getCanonicalName();

    public CurrencyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.list_item_currency_calculation, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView codeText = (TextView) view.findViewById(R.id.currency_code);
        TextView rateText = (TextView) view.findViewById(R.id.currency_rate);
        // Extract properties from cursor
        String body = cursor.getString(cursor.getColumnIndexOrThrow("CODE"));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow("RATE"));
        // Populate fields with extracted properties
        codeText.setText(body);
        rateText.setText(priority);
    }
}
