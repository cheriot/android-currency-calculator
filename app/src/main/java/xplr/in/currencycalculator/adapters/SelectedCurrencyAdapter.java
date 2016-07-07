package xplr.in.currencycalculator.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import xplr.in.currencycalculator.views.CurrencyAmountChangeListener;

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
    private OnItemDragListener onItemDragListener;

    public SelectedCurrencyAdapter(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository, Analytics analytics) {
        super();
        setHasStableIds(true);
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
        this.analytics = analytics;
    }

    public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
        this.onItemDragListener = onItemDragListener;
    }

    public void swapCursor(SquidCursor newCursor) {
        this.cursor = newCursor;
        if(this.cursor != null) refreshBaseMoney();
    }

    private void refreshBaseMoney() {
        cursor.moveToFirst(); // The base currency is first in the result set.
        baseMoney = currencyRepository.instantiateBaseMoney(cursor);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == ACTIONS_TYPE_POSITION) return ACTIONS_TYPE_POSITION;
        return OTHER_CURRENCY_TYPE;
    }

    @Override
    public AbstractCurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == ACTIONS_TYPE_POSITION) {
            View itemView = inflater.inflate(R.layout.list_item_currency_calculate_actions, parent, false);
            return new ActionsViewHolder(itemView, currencyRepository, metaRepository, analytics);
        }
        // OTHER_CURRENCY_TYPE
        View itemView = inflater.inflate(R.layout.list_item_currency_calculate_other, parent, false);
        return new CurrencyViewHolder(this, itemView, onItemDragListener, currencyRepository, metaRepository, analytics);
    }

    @Override
    public void onBindViewHolder(AbstractCurrencyViewHolder holder, int position) {
        Currency currency = getCurrency(position);
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

    public void notifyCurrencyInserted(int currencyPosition) {
        Log.v(LOG_TAG, "notifyCurrencyInserted " + currencyPosition + " translated to " + viewPosition(currencyPosition));
        notifyItemInserted(viewPosition(currencyPosition));
    }

    public void notifyCurrencyRemoved(int currencyPosition) {
        Log.v(LOG_TAG, "notifyCurrencyRemoved " + currencyPosition + " translated to " + viewPosition(currencyPosition));
        notifyItemRemoved(viewPosition(currencyPosition));
    }

    /**
     * notifyItemMoved is final, so use this method and NEVER use notifyItemMoved
     *
     * When moving an item from one side of the fixed position action button row, we need to move
     * the item that borders the buttons to the other side of the buttons. Otherwise, RecyclerView
     * will assume that all rows other than those explicitly moved will maintain their order.
     *
     * Or moving across multiple rows at once doesn't work and this happens to do it step by step.
     */
    public void notifyItemMovedWithFixedRow(int fromViewPosition, int toViewPosition) {
        Log.v(LOG_TAG, "notifyItemMoved position " + fromViewPosition + " " + toViewPosition);
        notifyItemMoved(fromViewPosition, toViewPosition);
        if(fromViewPosition > ACTIONS_TYPE_POSITION && toViewPosition < ACTIONS_TYPE_POSITION) {
            // An item below the buttons has moved above it. To make space, move the item above the
            // buttons to below it.
            Log.v(LOG_TAG, "notifyItemMoved position " + (ACTIONS_TYPE_POSITION+1) + " " + (ACTIONS_TYPE_POSITION));
            notifyItemMoved(ACTIONS_TYPE_POSITION+1, ACTIONS_TYPE_POSITION);
        } else if(fromViewPosition < ACTIONS_TYPE_POSITION && toViewPosition > ACTIONS_TYPE_POSITION) {
            // An item above the buttons has moved below it. To fill space, move the item below the
            // buttons to above it.
            Log.v(LOG_TAG, "notifyItemMoved position " + (ACTIONS_TYPE_POSITION-1) + " " + (ACTIONS_TYPE_POSITION));
            notifyItemChanged(ACTIONS_TYPE_POSITION-1, ACTIONS_TYPE_POSITION);
        }
    }

    public void notifyCalculated(String payload) {
        Log.v(LOG_TAG, "notifyCalculated notifyItemRangeChanged" + BASE_CURRENCY_TYPE_POSITION+1 + " to " + getItemCount());
        refreshBaseMoney();
        notifyItemRangeChanged(BASE_CURRENCY_TYPE_POSITION+1, getItemCount(), payload);
    }

    private Currency getCurrency(int viewPosition) {
        // offset by 1 to account for the actions row
        int dataPosition = viewPosition < ACTIONS_TYPE_POSITION ? viewPosition : viewPosition - 1;
        Currency currency = new Currency();
        cursor.moveToPosition(dataPosition);
        currency.readPropertiesFromCursor(cursor);
        return currency;
    }

    private int viewPosition(int dataPosition) {
        // data's position starts at 1, view starts at 0
        int offsetPosition = dataPosition - 1;
        // data's position does not have the row of buttons
        return offsetPosition < ACTIONS_TYPE_POSITION ? offsetPosition : offsetPosition + 1;
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

        public abstract void bindView(Currency currency, OptionalMoney baseMoney);

        public abstract Currency getCurrency();
    }

    public static class BaseCurrencyViewHolder extends AbstractCurrencyViewHolder {
        @Bind(R.id.base_currency) BaseCurrencyAmountEditorView baseCurrencyAmountEditorView;
        @Bind(R.id.currency_drag_handle) View dragHandleView;
        private OptionalMoney optionalMoney;

        public BaseCurrencyViewHolder(final RecyclerView.Adapter adapter,
                                      View itemView,
                                      final OnItemDragListener onItemDragListener,
                                      CurrencyRepository currencyRepository,
                                      CurrencyMetaRepository metaRepository,
                                      final Analytics analytics) {
            super(itemView, currencyRepository, metaRepository, analytics);
            ButterKnife.bind(this, itemView);
            baseCurrencyAmountEditorView.init(currencyRepository, metaRepository);
            baseCurrencyAmountEditorView.setCurrencyAmountChangeListener(new CurrencyAmountChangeListener() {
                @Override
                public void onCurrencyAmountChange() {
                    // BaseCurrencyAmountEditorView will have persisted the new base amount. Trigger
                    // a rebind so target and other rows will convert the new amount.
                    Log.v(LOG_TAG, "notifyDataSetChanged currencyAmountChangeListener");
                    adapter.notifyDataSetChanged();
                }
            });
            baseCurrencyAmountEditorView.getCurrencyAmount().setTextClearListener(new ClearableEditText.TextClearListener() {
                @Override
                public void onTextCleared() {
                    analytics.getMainActivityAnalytics().recordClearBaseAmount();
                }
            });
            if(onItemDragListener != null) {
                dragHandleView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                            onItemDragListener.onItemDrag(BaseCurrencyViewHolder.this);
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        public void bindView(Currency currency, OptionalMoney optionalMoney) {
            Log.v(LOG_TAG, "base bindView " + optionalMoney);
            this.optionalMoney = optionalMoney;
            baseCurrencyAmountEditorView.setMoney(optionalMoney);
        }

        @Override
        public Currency getCurrency() {
            return optionalMoney.getCurrency();
        }
    }

    /**
     * A single ViewHolder that can bind itself as a base, target, or other currency. RecyclerView
     * will turn moves into adds if the ViewHolder changes type so everything has to have a single
     * wrapper.
     */
    public static class CurrencyViewHolder extends AbstractCurrencyViewHolder implements View.OnClickListener {
        private static final String CHANGE_RECALCULATE = "recalculate";
        @Bind(R.id.currency_drag_handle) View dragHandleView;
        @Bind(R.id.currency_flag) ImageView flagImage;
        @Bind(R.id.currency_name) TextView nameText;
        @Bind(R.id.calculated_amount) TextView calculatedAmount;
        @Bind(R.id.edit_amount) ClearableEditText editAmount;

        private SelectedCurrencyAdapter adapter;
        private OptionalMoney optionalMoney;
        private CurrencyMeta meta;
        private int defaultTextColor;
        private ClearableEditText.TextChangeListener textChangeListener = new ClearableEditText.TextChangeListener() {
            @Override
            public void onTextChanged(String text) {
                Log.v(LOG_TAG, "CurrencyViewHolder#onTextChanged " + optionalMoney.getCurrency().getCode() + " " + text);
                optionalMoney.setAmount(text);
                currencyRepository.setBaseMoney(optionalMoney);
                // Trigger rebind so currency conversions will be recalculated. #setBaseMoney
                // will not write to the DB if only the amount has changed so we can notify the
                // adapter immediately.
                adapter.notifyCalculated(CHANGE_RECALCULATE);
            }
        };
        private ClearableEditText.TextClearListener textClearListener = new ClearableEditText.TextClearListener() {
            @Override
            public void onTextCleared() {
                Log.v(LOG_TAG, "CurrencyViewHolder onTextCleared");
                CurrencyViewHolder.this.analytics.getMainActivityAnalytics().recordClearBaseAmount();
            }
        };

        public CurrencyViewHolder(
                SelectedCurrencyAdapter adapter,
                View itemView,
                final OnItemDragListener onItemDragListener,
                CurrencyRepository currencyRepository,
                CurrencyMetaRepository metaRepository,
                Analytics analytics) {

            super(itemView, currencyRepository, metaRepository, analytics);
            this.adapter = adapter;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            // remember the system default so we can set it back
            defaultTextColor = nameText.getTextColors().getDefaultColor();
            dragHandleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        onItemDragListener.onItemDrag(CurrencyViewHolder.this);
                    }
                    return false;
                }
            });
        }

        public void bindView(Currency currency, OptionalMoney baseOptionalMoney) {
            meta = metaRepository.findByCode(currency.getCode());
            optionalMoney = baseOptionalMoney.convertTo(currency);
            nameText.setText(currency.getName());
            calculatedAmount.setText(optionalMoney.getAmountFormatted());

            updateTypeForPosition();
        }

        public void updateTypeForPosition() {
            if(isBase()) {
                Log.v(LOG_TAG, "updateTypeForPosition init editAmount " + optionalMoney.getCurrency().getCode());
                makeEditable(true);
                editAmount.removeTextChangedListener(textChangeListener);
                editAmount.setText(optionalMoney.getAmount());
                editAmount.addTextChangedListener(textChangeListener);
                editAmount.setTextClearListener(textClearListener);
            } else {
                makeEditable(false);
                // Remove listeners to prevent erronious callbacks.
                editAmount.removeTextChangedListener(textChangeListener);
                editAmount.setTextClearListener(null);
            }

            if(isBase() || isTarget()) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorWhite));
                largeDark(nameText);
                largeDark(calculatedAmount);
                styleFlagImage(CurrencyMeta.FlagSize.SQUARE, 50);
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
                smallGray(nameText);
                smallGray(calculatedAmount);
                styleFlagImage(CurrencyMeta.FlagSize.NORMAL, 40);
            }
        }

        private boolean isBase() {
            return getAdapterPosition() == BASE_CURRENCY_TYPE_POSITION;
        }

        private boolean isTarget() {
            return getAdapterPosition() == TARGET_CURRENCY_TYPE_POSITION;
        }

        private void styleFlagImage(CurrencyMeta.FlagSize flagSize, int maxWidthDp) {
            int resourceId = meta.getFlagResourceId(flagSize);
            Drawable drawable = itemView.getResources().getDrawable(resourceId);
            flagImage.setImageDrawable(drawable);
            float maxWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    maxWidthDp,
                    itemView.getResources().getDisplayMetrics());
            flagImage.setMaxWidth(Math.round(maxWidth));
        }

        private void largeDark(TextView tv) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            tv.setTextColor(Color.BLACK);
        }

        private void smallGray(TextView tv) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            tv.setTextColor(defaultTextColor);
        }

        private void makeEditable(boolean isEditable) {
            editAmount.setVisibility(isEditable ? View.VISIBLE : View.GONE);
            calculatedAmount.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        }

        public void onSwipe() {
            analytics.getMainActivityAnalytics().recordSwipeRemovedCurrency(optionalMoney.getCurrency());
            currencyRepository.updateSelection(optionalMoney.getCurrency().getId(), false);
        }

        @Override
        public void onClick(View v) {
            analytics.getMainActivityAnalytics().recordSelectCurrency(optionalMoney.getCurrency());
            Log.v(LOG_TAG, "Select a new base " + optionalMoney);
            // The calculated amount likely has more decimal places than we display. Set the base
            // amount to be what the user sees.
            // TODO how to run this when a currency is drug into the base positon?
            optionalMoney.roundToCurrency();
            currencyRepository.setBaseMoney(optionalMoney);
        }

        public CurrencyMeta.FlagSize flagSize() {
            return CurrencyMeta.FlagSize.NORMAL;
        }

        @Override
        public Currency getCurrency() {
            return optionalMoney.getCurrency();
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
        public void bindView(Currency currency, OptionalMoney baseMoney) {
            // nothing to bind
        }

        @Override
        public Currency getCurrency() {
            return null;
        }
    }
}
