package xplr.in.currencycalculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Money;

/**
 * Created by cheriot on 6/25/16.
 */
public class MoneyArraySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private ArrayList<Money> data;

    public MoneyArraySpinnerAdapter(List<Money> data) {
        this.data = new ArrayList<>(data);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getAmount().longValue();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View bindableView = convertView != null ? convertView : createView(parent);
        ViewHolder viewHolder = bindableView.getTag() != null ?
                (ViewHolder)bindableView.getTag() : new ViewHolder(bindableView);
        viewHolder.bindView(data.get(position));
        return bindableView;
    }

    private View createView(ViewGroup parent) {
        return LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.spinner_item_simple, parent, false);
    }

    public static class ViewHolder {
        @Bind(R.id.amount) TextView amountText;
        @Bind(R.id.code) TextView codeText;
        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }

        public void bindView(Money money) {
            amountText.setText(money.getAmountFormatted());
            codeText.setText(money.getCurrency().getCode());
        }
    }
}
