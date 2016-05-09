package xplr.in.currencycalculator.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 5/9/16.
 */
public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder> {

    private static final String LOG_TAG = CurrencyRecyclerAdapter.class.getSimpleName();
    private final int rLayout;

    public CurrencyRecyclerAdapter(int rLayout) {
        super();
        this.rLayout = rLayout;
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(rLayout, parent, false);
        return new CurrencyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, int position) {
        Log.v(LOG_TAG, "CurrencyRecyclerAdapter#onBindViewHolder");
        holder.bindView(null);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.currency_name) TextView nameText;
        @Nullable @Bind(R.id.currency_code) TextView codeText;
        @Nullable @Bind(R.id.currency_rate) TextView rateText;
        @Nullable @Bind(R.id.currency_selected) CheckBox checkBox;
        @Nullable @Bind(R.id.currency_flag) ImageView flagImage;

        public CurrencyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindView(Currency currency) {

        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "CurrencyViewHolder#onClick");
            // Log.v(LOG_TAG, "Clicked currency " + position);
//            SquidCursor currencyCursor = (SquidCursor) currenciesAdapter.getItem(position);
//            SelectedCurrency currency = new SelectedCurrency();
//            currency.readPropertiesFromCursor(currencyCursor);
//            // This currency's amount will become the new selected amount.
//            currency.convertFrom(baseCurrency);
//            String formattedAmount = ((TextView)view.findViewById(R.id.currency_rate))
//                    .getText()
//                    .toString();
//            currency.parse(formattedAmount);
//            currencyRepository.setBaseCurrency(currency);
//            currencyCalculationsListView.smoothScrollToPosition(0);
        }
    }
}
