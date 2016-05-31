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
public class BaseCurrencyView extends LinearLayout {
    private static final String LOG_TAG = BaseCurrencyView.class.getSimpleName();

    @Bind(R.id.base_currency_flag) ImageView baseCurrencyFlag;
    @Bind(R.id.base_currency_name) TextView baseCurrencyName;
    @Bind(R.id.base_currency_amount) ClearableEditText baseCurrencyAmount;

    private CurrencyRepository currencyRepository;
    private CurrencyMetaRepository metaRepository;
    private CurrencyAmountChangeListener changeListener;
    private SelectedCurrency baseCurrency;

    public BaseCurrencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_base_currency, this, true);
        ButterKnife.bind(this);

        // properties of <merge /> don't get merged. Set them here.
        float density = context.getResources().getDisplayMetrics().density;
        setMinimumHeight((int) (80*density));
        setFocusableInTouchMode(true);
        setGravity(Gravity.CENTER_VERTICAL);

        // Replace with butterknife's @OnTextChange?
        baseCurrencyAmount.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if(baseCurrency != null && !text.equals(baseCurrency.getAmount())) {
                    Log.v(LOG_TAG, "TEXT " + text);
                    currencyRepository.setBaseAmount(baseCurrency, text);
                    if(changeListener != null) changeListener.onCurrencyAmountChange();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void init(CurrencyRepository currencyRepository, CurrencyMetaRepository metaRepository) {
        this.currencyRepository = currencyRepository;
        this.metaRepository = metaRepository;
    }

    public void setCurrencyAmountChangeListener(CurrencyAmountChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setBaseCurrency(SelectedCurrency baseCurrency) {
        this.baseCurrency = baseCurrency;
        displayBaseCurrency(this.baseCurrency);
    }

    private void displayBaseCurrency(SelectedCurrency currency) {
        Log.v(LOG_TAG, "displayBaseCurrency " + currency.getCode());

        baseCurrencyName.setText(currency.getName());
        CurrencyMeta meta = metaRepository.findByCode(currency.getCode());
        if(meta != null) {
            Drawable drawable = getResources().getDrawable(meta.getFlagResourceId(CurrencyMeta.FlagSize.SQUARE));
            baseCurrencyFlag.setImageDrawable(drawable);
        }
        baseCurrencyAmount.getEditText().setText(currency.getAmount());
        // Move the cursor to the end as if the amount had just been typed.
        baseCurrencyAmount.moveCursorToEnd();
    }

    public interface CurrencyAmountChangeListener {
        void onCurrencyAmountChange();
    }
}
