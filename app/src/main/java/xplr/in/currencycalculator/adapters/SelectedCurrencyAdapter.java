package xplr.in.currencycalculator.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 5/9/16.
 */
public class SelectedCurrencyAdapter extends RecyclerView.Adapter<SelectedCurrencyAdapter.CurrencyViewHolder> {

    private static final String LOG_TAG = SelectedCurrencyAdapter.class.getSimpleName();
    private static final int BASE_CURRENCY_TYPE = 1;
    private static final int TARGET_CURRENCY_TYPE = 2;
    private static final int OTHER_CURRENCY_TYPE = 3;
    private final CurrencyRepository currencyRepository;
    private final CurrencyMetaRepository metaRepository;
    private SquidCursor cursor;
    private SelectedCurrency baseCurrency;

    public SelectedCurrencyAdapter(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
        super();
        setHasStableIds(true);
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
    }

    public void swapCursor(SquidCursor newCursor) {
        this.cursor = newCursor;
    }

    public void setBaseCurrency(SelectedCurrency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    @Override
    public int getItemViewType(int position) {
        //if(position == 0) return BASE_CURRENCY_TYPE;
        if(position == 0) return TARGET_CURRENCY_TYPE;
        return OTHER_CURRENCY_TYPE;
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater
                .from(parent.getContext());
//        if(viewType == BASE_CURRENCY_TYPE) {
//            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_other, parent, false);
//            return new CurrencyViewHolder(itemView, currencyRepository);
//        }
        if(viewType == TARGET_CURRENCY_TYPE) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_target, parent, false);
            return new CurrencyViewHolder(itemView, currencyRepository);
        }
        // OTHER_CURRENCY_TYPE
        View itemView = inflater.inflate(R.layout.list_item_currency_calculate_other, parent, false);
        return new CurrencyViewHolder(itemView, currencyRepository);
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, int position) {
        SelectedCurrency currency = getCurrency(position);
        holder.bindView(currency, baseCurrency, metaRepository.findByCode(currency.getCode()));
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
        return getCurrency(position).getId();
    }

    private SelectedCurrency getCurrency(int position) {
        SelectedCurrency currency = new SelectedCurrency();
        cursor.moveToPosition(position);
        currency.readPropertiesFromCursor(cursor);
        return currency;
    }

    public static class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.currency_container) View container;
        @Bind(R.id.currency_name) TextView nameText;
        @Bind(R.id.currency_rate) TextView rateText;
        @Bind(R.id.currency_flag) ImageView flagImage;
        private SelectedCurrency currency;
        private CurrencyRepository currencyRepository;

        public CurrencyViewHolder(View itemView, CurrencyRepository currencyRepository) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
            this.currencyRepository = currencyRepository;
        }

        public void bindView(SelectedCurrency currency, SelectedCurrency baseCurrency, CurrencyMeta meta) {
            Log.v(LOG_TAG, "bindView " + currency.getCode());
            this.currency = currency;
            nameText.setText(currency.getName());

            int resourceId = meta.getFlagResourceId(CurrencyMeta.FlagSize.SQUARE);
            Drawable drawable = itemView.getResources().getDrawable(resourceId);
            flagImage.setImageDrawable(drawable);

            if(baseCurrency != null) {
                currency.convertFrom(baseCurrency);
                rateText.setText(currency.format());
            }
        }

        public void onSwipe() {
            currencyRepository.updateSelection(currency.getId(), false);
        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "CurrencyViewHolder#onClick ");
            currencyRepository.setBaseCurrency(currency);
        }
    }
}
