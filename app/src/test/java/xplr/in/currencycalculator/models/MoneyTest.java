package xplr.in.currencycalculator.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import xplr.in.currencycalculator.BuildConfig;

import static org.junit.Assert.assertEquals;

/**
 * Created by cheriot on 6/1/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MoneyTest {

    @Test
    public void testConvertTo() {
        Currency base = new Currency();
        base.setRate("2");
        Money m = new Money(base, "100");

        Currency target = new Currency();
        target.setRate("4");
        assertEquals("Convert currency correctly.", new BigDecimal("200"), m.convertTo(target).getAmount());
        assertEquals("Convert currency correctly.", target, m.convertTo(target).getCurrency());
    }

    @Test
    public void testConvertToRealistic() {
        Money usd100 = new Money(usd(), "100");

        assertEquals(
                "USD to Euros",
                new Money(euro(), "87.6300"),
                usd100.convertTo(euro()));
        assertEquals(
                "USD to Kenyan Shillings",
                new Money(kenyanShilling(), "10113.1000"),
                usd100.convertTo(kenyanShilling()));

        Money euro100 = new Money(euro(), "100");
        assertEquals(
                "Euro to Kenyan Shillings",
                new Money(kenyanShilling(), "11540.68241469816272965879265091863179380"),
                euro100.convertTo(kenyanShilling()));
    }

    @Test
    public void testRoundToCurrency() {
        Money m = new Money(usd(), new BigDecimal("1.005"));
        Money r = m.roundToCurrency();
        assertEquals("Rounding does not change the currency.", r.getCurrency(), m.getCurrency());

        assertEquals("Round half up.", "1.01", round("1.005"));
        assertEquals("Round doesn't add zeros.", "1", round("1"));
    }

    private String round(String initialAmount) {
        Money m = new Money(usd(), new BigDecimal(initialAmount));
        Money r = m.roundToCurrency();
        return r.getAmount().toString();
    }

    private Currency usd() {
        Currency usd = new Currency();
        usd.setName("US Dollar");
        usd.setRate("1");
        usd.setCode("USD");
        usd.setPosition(null);
        usd.setMinorUnits(2);
        return usd;
    }

    private Currency euro() {
        Currency euro = new Currency();
        euro.setName("Euro");
        euro.setRate("0.8763");
        euro.setCode("EUR");
        euro.setPosition(null);
        euro.setMinorUnits(2);
        return euro;
    }

    private Currency kenyanShilling() {
        Currency kes = new Currency();
        kes.setName("Kenyan Shilling");
        kes.setRate("101.1310");
        kes.setCode("EUR");
        kes.setPosition(null);
        kes.setMinorUnits(2);
        return kes;
    }
}
