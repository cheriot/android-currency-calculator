package xplr.in.currencycalculator.databases;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import xplr.in.currencycalculator.BuildConfig;

import static org.junit.Assert.*;

/**
 * Created by cheriot on 4/10/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SelectedCurrencyTest {

    @Test
    public void testConvertFrom() {
        SelectedCurrency base = new SelectedCurrency();
        base.setRate("2");
        base.setAmount("100");
        SelectedCurrency calculated = new SelectedCurrency();
        calculated.setRate("4");
        assertEquals("Convert currency correctly.", "200", calculated.convertFrom(base));
    }

    @Test
    public void testConvertFromNonterminating() {
        // Infinitely repeating decimals cannot be represented by BigDecimal. It will throw
        // java.lang.ArithmeticException: Non-terminating decimal expansion; no exact representable decimal result
        // Verify that we have a rounding rule in place.
        SelectedCurrency base = new SelectedCurrency();
        base.setRate("3");
        base.setAmount("100");
        SelectedCurrency calculated = new SelectedCurrency();
        calculated.setRate("4");
        assertEquals("Convert currency correctly.", "133.33333333333333333333333333333332", calculated.convertFrom(base));
    }
}