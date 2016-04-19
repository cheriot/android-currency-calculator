package xplr.in.currencycalculator.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import xplr.in.currencycalculator.BuildConfig;

import static org.junit.Assert.assertEquals;

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
        assertEquals("Convert currency correctly.", new BigDecimal("200"), calculated.convertFrom(base));

        SelectedCurrency usd = usd();
        usd.setAmount("100");

        assertEquals(
                "USD to Euros",
                new BigDecimal("87.6300"),
                euro().convertFrom(usd));
        assertEquals(
                "USD to Kenyan Shillings",
                new BigDecimal("10113.1000"),
                kenyanShilling().convertFrom(usd));
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
        assertEquals("Convert currency correctly.", new BigDecimal("133.33333333333333333333333333333332"), calculated.convertFrom(base));
    }

    @Test
    public void testRoundNumberCloseTo() {
        SelectedCurrency usd = usd();
        usd.setAmount("1");
        assertEquals("1 Euro", "1", euro().roundNumberCloseTo(usd));
        assertEquals("100 KES", "100.0", kenyanShilling().roundNumberCloseTo(usd));
    }

    private SelectedCurrency usd() {
        SelectedCurrency usd = new SelectedCurrency();
        usd.setRate("1");
        usd.setCode("USD");
        return usd;
    }

    private SelectedCurrency euro() {
        SelectedCurrency euro = new SelectedCurrency();
        euro.setRate("0.8763");
        euro.setCode("EUR");
        return euro;
    }

    private SelectedCurrency kenyanShilling() {
        SelectedCurrency kes = new SelectedCurrency();
        kes.setRate("101.1310");
        kes.setCode("EUR");
        return kes;
    }
}