package xplr.in.currencycalculator.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yahoo.squidb.data.SquidCursor;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.activities.TradeComparisonActivity;
import xplr.in.currencycalculator.activities.RateComparisonActivity;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyAmountEditorView;
import xplr.in.currencycalculator.views.CurrencyAmountEditorView;

/**
 * Created by cheriot on 5/9/16.
 */
public class SelectedCurrencyAdapter extends RecyclerView.Adapter<SelectedCurrencyAdapter.AbstractCurrencyViewHolder> {

    private static final String LOG_TAG = SelectedCurrencyAdapter.class.getSimpleName();

    public static final int BASE_CURRENCY_TYPE_POSITION = 0;
    private static final int TARGET_CURRENCY_TYPE_POSITION = 1;
    public static final int ACTIONS_TYPE_POSITION = 2;
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
        if(position == BASE_CURRENCY_TYPE_POSITION) return BASE_CURRENCY_TYPE_POSITION;
        if(position == TARGET_CURRENCY_TYPE_POSITION) return TARGET_CURRENCY_TYPE_POSITION;
        if(position == ACTIONS_TYPE_POSITION) return ACTIONS_TYPE_POSITION;
        return OTHER_CURRENCY_TYPE;
    }

    @Override
    public AbstractCurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == BASE_CURRENCY_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_base, parent, false);
            return new BaseCurrencyViewHolder(this, itemView, currencyRepository, metaRepository);
        }
        if(viewType == TARGET_CURRENCY_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_target, parent, false);
            return new TargetCurrencyViewHolder(itemView, currencyRepository, metaRepository);
        }
        if(viewType == ACTIONS_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_actions, parent, false);
            return new ActionsViewHolder(itemView, currencyRepository, metaRepository);
        }
        // OTHER_CURRENCY_TYPE
        View itemView = inflater.inflate(R.layout.list_item_currency_calculate_other, parent, false);
        return new CurrencyViewHolder(itemView, currencyRepository, metaRepository);
    }

    @Override
    public void onBindViewHolder(AbstractCurrencyViewHolder holder, int position) {
        SelectedCurrency currency = getCurrency(position);
        holder.bindView(currency, baseCurrency);
    }

    @Override
    public int getItemCount() {
        if(cursor != null) {
            return cursor.getCount() + 1; // +1 for the actions row
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if(position == ACTIONS_TYPE_POSITION) return -1; // any number that's not a list position
        return getCurrency(position).getId();
    }

    private SelectedCurrency getCurrency(int viewPosition) {
        // offset by 1 to account for the actions row
        int dataPosition = viewPosition < ACTIONS_TYPE_POSITION ? viewPosition : viewPosition - 1;
        SelectedCurrency currency = new SelectedCurrency();
        cursor.moveToPosition(dataPosition);
        currency.readPropertiesFromCursor(cursor);
        return currency;
    }

    public static abstract class AbstractCurrencyViewHolder extends RecyclerView.ViewHolder {
        protected final CurrencyRepository currencyRepository;
        protected final CurrencyMetaRepository metaRepository;

        public AbstractCurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
            super(itemView);
            this.currencyRepository = currencyRepository;
            this.metaRepository = metaRepository;
        }

        abstract void bindView(SelectedCurrency currency, SelectedCurrency baseCurrency);
    }

    public static class BaseCurrencyViewHolder extends AbstractCurrencyViewHolder {
        @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyAmountEditorView;
        public BaseCurrencyViewHolder(final RecyclerView.Adapter adapter,
                                      View itemView,
                                      CurrencyRepository currencyRepository,
                                      CurrencyMetaRepository metaRepository) {
            super(itemView, currencyRepository, metaRepository);
            ButterKnife.bind(this, itemView);
            baseCurrencyAmountEditorView.init(currencyRepository, metaRepository);
            baseCurrencyAmountEditorView.setCurrencyAmountChangeListener(new CurrencyAmountEditorView.CurrencyAmountChangeListener() {
                @Override
                public void onCurrencyAmountChange() {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void bindView(SelectedCurrency currency, SelectedCurrency baseCurrency) {
            // The baseCurrency has #amount set.
            baseCurrencyAmountEditorView.setSelectedCurrency(baseCurrency != null ? baseCurrency : currency);
        }
    }

    public static class CurrencyViewHolder extends AbstractCurrencyViewHolder implements View.OnClickListener {
        @Bind(R.id.currency_name) TextView nameText;
        @Bind(R.id.currency_rate) TextView rateText;
        @Bind(R.id.currency_flag) ImageView flagImage;
        private SelectedCurrency currency;

        public CurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
            super(itemView, currencyRepository, metaRepository);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindView(SelectedCurrency currency, SelectedCurrency baseCurrency) {
            Log.v(LOG_TAG, "bindView " + currency.getCode());
            this.currency = currency;
            nameText.setText(currency.getName());

            CurrencyMeta meta = metaRepository.findByCode(currency.getCode());
            int resourceId = meta.getFlagResourceId(flagSize());
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

        public CurrencyMeta.FlagSize flagSize() {
            return CurrencyMeta.FlagSize.SQUARE;
        }
    }

    public static class TargetCurrencyViewHolder extends CurrencyViewHolder {
        public TargetCurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
            super(itemView, currencyRepository, metaRepository);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public CurrencyMeta.FlagSize flagSize() {
            return CurrencyMeta.FlagSize.SQUARE;
        }
    }

    public static class ActionsViewHolder extends AbstractCurrencyViewHolder {
        @Bind(R.id.rate_comparison_button) Button rateComparisonButton;
        @Bind(R.id.trade_comparison_button) Button tradeComparisonButton;
        public ActionsViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
            super(itemView, currencyRepository, metaRepository);
            ButterKnife.bind(this, itemView);
            rateComparisonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), RateComparisonActivity.class));
                }
            });
            tradeComparisonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), TradeComparisonActivity.class));
                }
            });
        }

        @Override
        void bindView(SelectedCurrency currency, SelectedCurrency baseCurrency) {
            // nothing to bind
        }
    }
}
