package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.activities.CurrencyListActivity;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;

/**
 * Adapter for both currency list screens.
 *
 * To manage multiple view types override getItemViewType and getViewTypeCount
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

        View view = LayoutInflater
                .from(context)
                .inflate(listItemLayout, parent, false);
        view.setTag(new ViewHolder(view));
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

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        CurrencyMeta meta = currencyMetaRepository.findByCode(currency.getCode());
        String name = meta != null? meta.getName() : currency.getCode();

        if(viewHolder.codeText != null) viewHolder.codeText.setText(name);
        if(viewHolder.checkBox != null) viewHolder.checkBox.setChecked(currency.isSelected());

        SelectedCurrency baseCurrency = this.currencyListActivity.getBaseCurrency();
        if(viewHolder.rateText != null) {
            currency.convertFrom(baseCurrency);
            viewHolder.rateText.setText(currency.format(meta));
        }

        //CurrencyMetaRepository metaRepository = this.currencyListActivity;
        Log.v(LOG_TAG, "baseCurrency in bindView    " + baseCurrency);
    }

    static class ViewHolder {
        @Bind(R.id.currency_code) TextView codeText;
        @Nullable @Bind(R.id.currency_rate) TextView rateText;
        @Nullable @Bind(R.id.currency_selected) CheckBox checkBox;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
