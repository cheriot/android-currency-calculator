package xplr.in.currencycalculator.models;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by cheriot on 5/31/16.
 */
public class Money {

    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private final Currency currency;
    private final BigDecimal amount;

    public Money(Currency currency, String amountStr) {
        this(currency, new BigDecimal(amountStr));
    }

    public Money(Currency currency, int amount) {
        this(currency, BigDecimal.valueOf(amount));
    }

    public Money(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
        if(!isValid()) throw new RuntimeException("Invalid Money with <"+amount+"> <"+currency+">.");
    }

    private boolean isValid() {
        return this.currency != null && this.amount != null && !this.currency.getRate().equals(BigDecimal.ZERO);
    }

    public Money multiply(BigDecimal rhs) {
        return new Money(currency, amount.multiply(rhs));
    }

    public Money multiply(int rhs) {
        return multiply(BigDecimal.valueOf(rhs));
    }

    public BigDecimal divide(Money divisor) {
        checkUnits(divisor);
        return amount.divide(divisor.getAmount(), MATH_CONTEXT);
    }

    public BigDecimal divide(int divisor) {
        return amount.divide(BigDecimal.valueOf(divisor), MATH_CONTEXT);
    }

    public Money subtract(Money rhs) {
        checkUnits(rhs);
        return new Money(currency, amount.subtract(rhs.getAmount()));
    }

    public Money convertTo(Currency targetCurrency) {
        BigDecimal dollarsAmount = convertToUSD();
        BigDecimal targetUSDRate = new BigDecimal(targetCurrency.getRate());
        BigDecimal targetAmount = dollarsAmount.multiply(targetUSDRate);
        return new Money(targetCurrency, targetAmount);
    }

    public Money convertTo(BigDecimal rate, Currency targetCurrency) {
        return new Money(targetCurrency, amount.multiply(rate));
    }

    public BigDecimal rateTo(Currency targetCurrency) {
        Money one = new Money(currency, BigDecimal.ONE);
        return one.convertTo(targetCurrency).getAmount();
    }

    private BigDecimal convertToUSD() {
        BigDecimal baseUSDRate = new BigDecimal(currency.getRate());
        return amount.divide(baseUSDRate, MATH_CONTEXT);
    }

    /**
     * Round amount to a friendly number for the amount a new install will default to.
     * @return Money of the came currency with a friendly amount.
     */
    public Money roundToFriendly() {
        int compare = amount.compareTo(BigDecimal.ONE);
        if(amount.compareTo(BigDecimal.ONE) == -1 || amount.compareTo(BigDecimal.ONE) == 0) {
            // USD, Euro, Bitcoin, etc
            return new Money(currency, BigDecimal.ONE);
        } else {
            // Any kind of Shilling
            double digitCount = Math.floor(Math.log10(amount.doubleValue())) + 1;
            // The power of 10 ABOVE the amount.
            BigDecimal friendly = BigDecimal.valueOf((long)Math.pow(10, digitCount));
            // Don't show unneeded decimals.
            friendly.setScale(0, BigDecimal.ROUND_DOWN);
            return new Money(currency, friendly);
            // Alternative way, round to a single significant digit, add a zero, change the first
            // digit to a one.
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getAmountFormatted() {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(currency.getMinorUnits());
        return format.format(amount);
    }

    private void checkUnits(Money other) {
        if(!currency.equals(other.getCurrency())) {
            throw new IllegalStateException("Money math must have matching currencies. <" + currency + "> <" + other.getCurrency() + ">.");
        }
    }

    public Money roundToCurrency() {
        BigDecimal rounded = amount.setScale(currency.getMinorUnits(), RoundingMode.HALF_UP).stripTrailingZeros();
        // .stripTrailingZeros will not just drop decimal zeros, it will also turn 10 into 1E+1
        // .toPlainString will get us back to 10. This isn't for correctness, just readability in logs.
        BigDecimal simple = new BigDecimal(rounded.toPlainString());
        return new Money(currency, simple);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;

        return currency.equals(money.currency) && amount.equals(money.amount);
    }

    @Override
    public int hashCode() {
        int result = currency.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Money{" +
                "amount=" + amount +
                ", currency.code=" + currency.getCode() +
                '}';
    }
}
