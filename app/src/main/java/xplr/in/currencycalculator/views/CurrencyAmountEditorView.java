package xplr.in.currencycalculator.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

    private TextWatcher textWatcher;

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

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                Log.v(LOG_TAG, "TEXT " + text);
                setAmount(selectedCurrency, text);
                if(changeListener != null) changeListener.onCurrencyAmountChange();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    public void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
    }

    public void setCurrencyAmountChangeListener(CurrencyAmountChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setSelectedCurrency(SelectedCurrency selectedCurrency) {
        currencyAmount.getEditText().removeTextChangedListener(textWatcher);
        this.selectedCurrency = selectedCurrency;
        displayCurrency(this.selectedCurrency);
        currencyAmount.getEditText().addTextChangedListener(textWatcher);
    }

    private void displayCurrency(SelectedCurrency currency) {
        Log.v(LOG_TAG, "displayCurrency " + currency.getCode());

        currencyName.setText(currency.getName());
        CurrencyMeta meta = metaRepository.findByCode(currency.getCode());
        if(meta != null) {
            Drawable drawable = getResources().getDrawable(meta.getFlagResourceId(CurrencyMeta.FlagSize.SQUARE));
            currencyFlag.setImageDrawable(drawable);
        }
        currencyAmount.getEditText().setText(currency.getAmount());
        // Move the cursor to the end as if the amount had just been typed.
        currencyAmount.moveCursorToEnd();
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
}
