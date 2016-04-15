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

import java.math.BigDecimal;
import java.text.DecimalFormat;

import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.activities.CurrencyListActivity;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;

/**
 * Created by cheriot on 4/5/16.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    public static String LOG_TAG = CurrencyCursorAdapter.class.getCanonicalName();

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

        CurrencyMeta meta = currencyMetaRepository.findByCode(currency.getCode());
        String name = meta != null? meta.getName() : currency.getCode();
        int minorUnits = meta != null? meta.getMinorUnits() : 2;

        if(codeText != null) codeText.setText(name);
        if(checkBox != null) checkBox.setChecked(currency.isSelected());

        SelectedCurrency baseCurrency = this.currencyListActivity.getBaseCurrency();
        if(rateText != null) {
            BigDecimal amount = currency.convertFrom(baseCurrency);
            if (amount != null) {
                // DecimalFormat will use the default locale. This still ignores the currency symbol
                // and which side of the number it goes on. Also, Locale.Category is not available.
                DecimalFormat format = new DecimalFormat();
                format.setMaximumFractionDigits(minorUnits);
                rateText.setText(format.format(amount));
            } else {
                rateText.setText("-");
            }
        }

        //CurrencyMetaRepository metaRepository = this.currencyListActivity;
        Log.v(LOG_TAG, "baseCurrency in bindView    " + baseCurrency);
    }
}
