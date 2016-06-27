package xplr.in.currencycalculator.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.models.SelectedCurrency;
import xplr.in.currencycalculator.repositories.CurrencyMetaRepository;
import xplr.in.currencycalculator.repositories.CurrencyRepository;

/**
 * Created by cheriot on 5/31/16.
 */
public class CurrencyAmountEditorView extends LinearLayout {
    private static final String LOG_TAG = CurrencyAmountEditorView.class.getSimpleName();

    @Bind(R.id.currency_flag) ImageView currencyFlag;
    @Bind(R.id.currency_name) TextView currencyName;
    @Bind(R.id.currency_amount) ClearableEditText currencyAmount;

    private ClearableEditText.TextChangeListener textChangeListener;

    private CurrencyRepository currencyRepository;
    private CurrencyMetaRepository metaRepository;
    private CurrencyAmountChangeListener changeListener;
    private SelectedCurrency selectedCurrency;

    public CurrencyAmountEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_currency_amount_editor, this, true);
        ButterKnife.bind(this);

        // properties of <merge /> don't get merged. Set them here.
        float density = context.getResources().getDisplayMetrics().density;
        setMinimumHeight((int) (80*density));
        setFocusableInTouchMode(true);
        setGravity(Gravity.CENTER_VERTICAL);

        textChangeListener = new ClearableEditText.TextChangeListener() {
            @Override
            public void onTextChanged(String text) {
                Log.v(LOG_TAG, "TEXT " + text);
                setAmount(selectedCurrency, text);
                if(changeListener != null) changeListener.onCurrencyAmountChange();
            }
        };
        currencyAmount.addTextChangedListener(textChangeListener);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), currencyAmount.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.v(LOG_TAG, "CAEV#onRestoreInstanceState " + getId() + "  " + state);
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currencyAmount.onRestoreInstanceState(savedState.getChild());
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // As we save our own instance state, ensure our children don't save and restore their state as well.
        // (it can overwrite the state restored here)
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        /** See comment in {@link #dispatchSaveInstanceState(android.util.SparseArray)} */
        super.dispatchThawSelfOnly(container);
    }

    public void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
    }

    public void setCurrencyAmountChangeListener(CurrencyAmountChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setSelectedCurrency(SelectedCurrency selectedCurrency) {
        currencyAmount.removeTextChangedListener(textChangeListener);
        this.selectedCurrency = selectedCurrency;
        displayCurrency(this.selectedCurrency);
        if(!selectedCurrency.equals(this.selectedCurrency)) {
            // Move the cursor to the end as if the amount had just been typed.
            currencyAmount.moveCursorToEnd();
        } else {
            // The user may be typing in the middle of the word so leave the cursor alone.
            Log.v(LOG_TAG, "Duplicate call to setSelectedCurrency. Ignoring");
        }
        currencyAmount.addTextChangedListener(textChangeListener);
    }

    private void displayCurrency(SelectedCurrency currency) {
        Log.v(LOG_TAG, "displayCurrency " + currency.getCode());

        currencyName.setText(currency.getName());
        CurrencyMeta meta = metaRepository.findByCode(currency.getCode());
        if(meta != null) {
            Drawable drawable = getResources().getDrawable(meta.getFlagResourceId(CurrencyMeta.FlagSize.SQUARE));
            currencyFlag.setImageDrawable(drawable);
        }
        currencyAmount.setText(currency.getAmount());
    }

    protected void setAmount(SelectedCurrency currency, String amount) {
        currency.setAmount(amount);
    }

    public CurrencyRepository getCurrencyRepository() {
        return currencyRepository;
    }

    public SelectedCurrency getSelectedCurrency() {
        return selectedCurrency;
    }

    public interface CurrencyAmountChangeListener {
        void onCurrencyAmountChange();
    }

    public ClearableEditText getCurrencyAmount() {
        return currencyAmount;
    }

    public static class SavedState extends View.BaseSavedState {

        private final Parcelable child;

        public SavedState(Parcelable superState, Parcelable child) {
            super(superState);
            this.child = child;
        }

        private SavedState(Parcel in) {
            super(in);
            this.child = in.readParcelable(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(child, flags);
        }

        public Parcelable getChild() {
            return child;
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
