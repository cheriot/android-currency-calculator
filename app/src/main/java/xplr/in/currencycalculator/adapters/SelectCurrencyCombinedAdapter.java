package xplr.in.currencycalculator.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.presenters.SelectableCurrencies;

/**
 * Combined adapter for popular and all lists and header rows to identify them.
 */
public class SelectCurrencyCombinedAdapter
        extends RecyclerView.Adapter<SelectCurrencyCombinedAdapter.CombinedViewHolder> {

    private static final String LOG_TAG = SelectCurrencyCombinedAdapter.class.getSimpleName();
    private static final int CURRENCY_TYPE = 1;
    private static final int HEADER_TYPE = 2;
    private final CurrencySelectionChangeListener selectionListener;
    private SelectableCurrencies selectableCurrencies;

    public SelectCurrencyCombinedAdapter(CurrencySelectionChangeListener selectionListener) {
        super();
        this.selectionListener = selectionListener;
    }

    public void setData(SelectableCurrencies selectableCurrencies) {
        this.selectableCurrencies = selectableCurrencies;
        notifyDataSetChanged();
    }

    public void resetData() {
        selectableCurrencies = null;
    }

    @Override
    public int getItemViewType(int position) {
        if(selectableCurrencies.isHeader(position)) {
            return HEADER_TYPE;
        } else {
            return CURRENCY_TYPE;
        }
    }

    @Override
    public CombinedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater
                .from(parent.getContext());
        if(viewType == HEADER_TYPE) {
            View itemView = inflater.inflate(R.layout.list_item_currency_select_header, parent, false);
            return new HeaderViewHolder(itemView);
        } else if(viewType == CURRENCY_TYPE) {
            View itemView = inflater.inflate(R.layout.list_item_currency_select, parent, false);
            return new CurrencyViewHolder(itemView, selectionListener);
        }
        throw new RuntimeException("Unknown viewType <" + viewType + ">.");
    }

    @Override
    public void onBindViewHolder(CombinedViewHolder holder, int position) {
        holder.bindView(position, selectableCurrencies);
    }

    @Override
    public int getItemCount() {
        return selectableCurrencies == null ? 0 : selectableCurrencies.getCount();
    }

    public static abstract class CombinedViewHolder extends RecyclerView.ViewHolder {
        public CombinedViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindView(int position, SelectableCurrencies selectableCurrencies);
    }

    public static class HeaderViewHolder extends CombinedViewHolder {
        @Bind(R.id.list_header) TextView listHeaderText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(int position, SelectableCurrencies selectableCurrencies) {
            if(selectableCurrencies.isPopularHeader(position)) listHeaderText.setText("POPULAR");
            else listHeaderText.setText("ALL CURRENCIES");
        }
    }

    public static class CurrencyViewHolder extends CombinedViewHolder implements View.OnClickListener {
        @Bind(R.id.icon_plus) ImageButton iconPlus;
        @Bind(R.id.icon_check) ImageButton iconCheck;
        @Bind(R.id.currency_name) TextView nameText;
        @Bind(R.id.currency_code) TextView codeText;

        private final CurrencySelectionChangeListener selectionListener;
        private Currency currency;
        private int defaultTextColor;

        public CurrencyViewHolder(View itemView, CurrencySelectionChangeListener selectionListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.selectionListener = selectionListener;
            defaultTextColor = nameText.getCurrentTextColor();
            iconPlus.setOnClickListener(this);
            iconCheck.setOnClickListener(this);
        }

        public void bindView(int position, SelectableCurrencies selectableCurrencies) {
            currency = selectableCurrencies.getCurrency(position);
            nameText.setText(currency.getName());
            codeText.setText(currency.getCode());

            itemView.setOnClickListener(this);

            if(currency.isSelected()) {
                iconCheck.setVisibility(View.VISIBLE);
                iconPlus.setVisibility(View.GONE);
                nameText.setTextColor(defaultTextColor);
            } else {
                iconCheck.setVisibility(View.GONE);
                iconPlus.setVisibility(View.VISIBLE);
                nameText.setTextColor(Color.BLACK);
            }

        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "onCheckedChanged persist selection " + currency.getCode() + " " + currency.isSelected() + " will be changed.");
            selectionListener.onCurrencySelectionChange(currency, !currency.isSelected());
        }
    }
}
