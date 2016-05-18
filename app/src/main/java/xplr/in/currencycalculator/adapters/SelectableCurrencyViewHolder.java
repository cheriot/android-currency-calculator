package xplr.in.currencycalculator.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 5/18/16.
 */
public class SelectableCurrencyViewHolder {

    @Bind(R.id.currency_name) TextView nameText;
    @Bind(R.id.currency_code) TextView codeText;
    @Bind(R.id.currency_selected) CheckBox checkBox;

    public SelectableCurrencyViewHolder(View view) {
        ButterKnife.bind(this, view);
    }

    public void bindView(Currency currency) {
        nameText.setText(currency.getName());
        codeText.setText(currency.getCode());
        checkBox.setChecked(currency.isSelected());
    }
}
