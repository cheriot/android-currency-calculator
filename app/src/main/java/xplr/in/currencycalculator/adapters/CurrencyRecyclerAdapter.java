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

import com.yahoo.squidb.data.SquidCursor;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;

/**
 * Created by cheriot on 5/9/16.
 */
public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder> {

    private static final String LOG_TAG = CurrencyRecyclerAdapter.class.getSimpleName();
    private final int rLayout;
    private final CurrencyMetaRepository metaRepository;
    private SquidCursor cursor;

    public CurrencyRecyclerAdapter(int rLayout, CurrencyMetaRepository metaRepository) {
        super();
        this.rLayout = rLayout;
        this.metaRepository = metaRepository;
    }

    public void swapCursor(SquidCursor newCursor) {
        this.cursor = newCursor;
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
        SelectedCurrency currency = getCurrency(position);
        Log.v(LOG_TAG, "onBindViewHolder " + currency.getCode());
        holder.bindView(currency);
    }

    @Override
    public int getItemCount() {
        if(cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    private SelectedCurrency getCurrency(int position) {
        SelectedCurrency currency = new SelectedCurrency();
        cursor.moveToPosition(position);
        currency.readPropertiesFromCursor(cursor);
        return currency;
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
            Log.v(LOG_TAG, "bindView " + currency);
            nameText.setText(currency.getName());
            rateText.setText(currency.getRate());
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
