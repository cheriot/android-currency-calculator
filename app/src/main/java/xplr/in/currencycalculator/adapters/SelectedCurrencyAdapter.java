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
import xplr.in.currencycalculator.activities.RateComparisonActivity;
import xplr.in.currencycalculator.activities.TradeComparisonActivity;
import xplr.in.currencycalculator.analytics.Analytics;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.OptionalMoney;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;
import xplr.in.currencycalculator.views.BaseCurrencyAmountEditorView;
import xplr.in.currencycalculator.views.ClearableEditText;
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
    private final Analytics analytics;
    private SquidCursor cursor;
    private OptionalMoney baseMoney;

    public SelectedCurrencyAdapter(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, Analytics analytics) {
        super();
        setHasStableIds(true);
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
        this.analytics = analytics;
    }

    public void swapCursor(SquidCursor newCursor) {
        this.cursor = newCursor;
        if(this.cursor != null) {
            cursor.moveToFirst(); // The base currency is first in the result set.
            baseMoney = currencyRepository.instantiateBaseMoney(cursor);
        }
    }

    public void setBaseMoney(OptionalMoney baseMoney) {
        Log.v(LOG_TAG, "setBaseMoney " + baseMoney);
        this.baseMoney = baseMoney;
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
            return new BaseCurrencyViewHolder(this, itemView, currencyRepository, metaRepository, analytics);
        }
        if(viewType == TARGET_CURRENCY_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_target, parent, false);
            return new TargetCurrencyViewHolder(itemView, currencyRepository, metaRepository, analytics);
        }
        if(viewType == ACTIONS_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_actions, parent, false);
            return new ActionsViewHolder(itemView, currencyRepository, metaRepository, analytics);
        }
        // OTHER_CURRENCY_TYPE
        View itemView = inflater.inflate(R.layout.list_item_currency_calculate_other, parent, false);
        return new CurrencyViewHolder(itemView, currencyRepository, metaRepository, analytics);
    }

    @Override
    public void onBindViewHolder(AbstractCurrencyViewHolder holder, int position) {
        Currency currency = getCurrency(position);
        Log.v(LOG_TAG, "onBindViewHolder " + position + " " + getItemId(position) + " " + currency.getCode());
        holder.bindView(currency, baseMoney);
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
        if(position == ACTIONS_TYPE_POSITION) return Long.MAX_VALUE; // any number that's not a list position
        return getCurrency(position).getId();
    }

    private Currency getCurrency(int viewPosition) {
        // offset by 1 to account for the actions row
        int dataPosition = viewPosition < ACTIONS_TYPE_POSITION ? viewPosition : viewPosition - 1;
        Currency currency = new Currency();
        cursor.moveToPosition(dataPosition);
        currency.readPropertiesFromCursor(cursor);
        return currency;
    }

    public static abstract class AbstractCurrencyViewHolder extends RecyclerView.ViewHolder {
        protected final CurrencyRepository currencyRepository;
        protected final CurrencyMetaRepository metaRepository;
        protected final Analytics analytics;

        public AbstractCurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, Analytics analytics) {
            super(itemView);
            this.currencyRepository = currencyRepository;
            this.metaRepository = metaRepository;
            this.analytics = analytics;
        }

        abstract void bindView(Currency currency, OptionalMoney baseMoney);
    }

    public static class BaseCurrencyViewHolder extends AbstractCurrencyViewHolder {
        @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyAmountEditorView;
        public BaseCurrencyViewHolder(final RecyclerView.Adapter adapter,
                                      View itemView,
                                      CurrencyRepository currencyRepository,
                                      CurrencyMetaRepository metaRepository,
                                      final Analytics analytics) {
            super(itemView, currencyRepository, metaRepository, analytics);
            ButterKnife.bind(this, itemView);
            baseCurrencyAmountEditorView.init(currencyRepository, metaRepository);
            baseCurrencyAmountEditorView.setCurrencyAmountChangeListener(new CurrencyAmountEditorView.CurrencyAmountChangeListener() {
                @Override
                public void onCurrencyAmountChange() {
                    // BaseCurrencyAmountEditorView will have persisted the new base amount. Trigger
                    // a rebind so target and other rows will convert the new amount.
                    adapter.notifyDataSetChanged();
                }
            });
            baseCurrencyAmountEditorView.getCurrencyAmount().setTextClearListener(new ClearableEditText.TextClearListener() {
                @Override
                public void onTextCleared() {
                    analytics.getMainActivityAnalytics().recordClearBaseAmount();
                }
            });
        }

        @Override
        public void bindView(Currency currency, OptionalMoney optionalMoney) {
            Log.v(LOG_TAG, "base bindView " + optionalMoney);
            baseCurrencyAmountEditorView.setMoney(optionalMoney);
        }
    }

    public static class CurrencyViewHolder extends AbstractCurrencyViewHolder implements View.OnClickListener {
        @Bind(R.id.currency_name) TextView nameText;
        @Bind(R.id.currency_rate) TextView rateText;
        @Bind(R.id.currency_flag) ImageView flagImage;
        private OptionalMoney convertedMoney;

        public CurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, Analytics analytics) {
            super(itemView, currencyRepository, metaRepository, analytics);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindView(Currency currency, OptionalMoney baseOptionalMoney) {
            nameText.setText(currency.getName());

            CurrencyMeta meta = metaRepository.findByCode(currency.getCode());
            int resourceId = meta.getFlagResourceId(flagSize());
            Drawable drawable = itemView.getResources().getDrawable(resourceId);
            flagImage.setImageDrawable(drawable);

            convertedMoney = baseOptionalMoney.convertTo(currency);
            rateText.setText(convertedMoney.getAmountFormatted());
        }

        public void onSwipe() {
            analytics.getMainActivityAnalytics().recordSwipeRemovedCurrency(convertedMoney.getCurrency());
            currencyRepository.updateSelection(convertedMoney.getCurrency().getId(), false);
        }

        @Override
        public void onClick(View v) {
            analytics.getMainActivityAnalytics().recordSelectCurrency(convertedMoney.getCurrency());
            Log.v(LOG_TAG, "Select a new base " + convertedMoney);
            // The calculated amount likely has more decimal places than we display. Set the base
            // amount to be what the user sees.
            convertedMoney.roundToCurrency();
            currencyRepository.setBaseMoney(convertedMoney);
        }

        public CurrencyMeta.FlagSize flagSize() {
            return CurrencyMeta.FlagSize.NORMAL;
        }
    }

    public static class TargetCurrencyViewHolder extends CurrencyViewHolder {
        public TargetCurrencyViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, Analytics analytics) {
            super(itemView, currencyRepository, metaRepository, analytics);
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
        public ActionsViewHolder(View itemView, CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, final Analytics analytics) {
            super(itemView, currencyRepository, metaRepository, analytics);
            ButterKnife.bind(this, itemView);
            rateComparisonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    analytics.getMainActivityAnalytics().recordExchangeButtonPress();
                    v.getContext().startActivity(new Intent(v.getContext(), RateComparisonActivity.class));
                }
            });
            tradeComparisonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    analytics.getMainActivityAnalytics().recordTradeButtonPress();
                    v.getContext().startActivity(new Intent(v.getContext(), TradeComparisonActivity.class));
                }
            });
        }

        @Override
        void bindView(Currency currency, OptionalMoney baseMoney) {
            // nothing to bind
        }
    }
}
