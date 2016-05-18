package xplr.in.currencycalculator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 5/18/16.
 */
public class CurrencyArrayAdapter extends ArrayAdapter<Currency> {

    private final int resource;

    public CurrencyArrayAdapter(Context context, int resource, List<Currency> currencies) {
        super(context, resource, currencies);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }
        if(convertView.getTag() == null) {
            convertView.setTag(new SelectableCurrencyViewHolder(convertView));
        }

        SelectableCurrencyViewHolder viewHolder = (SelectableCurrencyViewHolder) convertView.getTag();
        Currency currency = getItem(position);
        viewHolder.bindView(currency);

        return convertView;
    }
}
