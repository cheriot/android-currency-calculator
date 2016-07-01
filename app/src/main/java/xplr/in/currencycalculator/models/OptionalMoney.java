package xplr.in.currencycalculator.models;

import android.text.TextUtils;

/**
 * A kind of Option<Money> where an amount of "-" indicates there is a currency selected, but no
 * amount specified.
 * Created by cheriot on 6/29/16.
 */
public class OptionalMoney {

    private static final String LOG_TAG = OptionalMoney.class.getSimpleName();
    private static final String EMPTY_AMOUNT = "-";

    private Currency currency;
    private String amount;

    public OptionalMoney(Currency currency, String amount) {
        this.currency = currency;
        setAmount(amount);
    }

    public boolean isEmpty() {
        if(TextUtils.isEmpty(amount)) return true;
        if(".".equals(amount)) return true;
        return false;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public Money getMoney() {
        if(isEmpty()) throw new IllegalStateException("Attempt to get an unspecified Money instance.");
        return new Money(currency, amount);
    }

    public OptionalMoney convertTo(Currency currency) {
        if(isEmpty()) {
            return new OptionalMoney(currency, "");
        } else {
            return new OptionalMoney(currency, getMoney().convertTo(currency).getAmount().toString());
        }
    }

    public String getAmountFormatted() {
        // with BigDecimal 0 is not equal to 0.0 so use floatValue
        if(isEmpty() || getMoney().getAmount().floatValue() == 0) {
            // Don't change from - to 0 just because the user started typing. It's distracting.
            return EMPTY_AMOUNT;
        } else {
            return getMoney().getAmountFormatted();
        }
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setAmount(String amount) {
        // this.amount must be EMPTY_AMOUNT or a number parsable by BigDecimal
        this.amount = EMPTY_AMOUNT.equals(amount) ? "" : amount.trim();
    }

    public String getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "OptionalMoney{" +
                "currency=" + currency.getCode() +
                ", amount='" + amount + '\'' +
                '}';
    }
}
