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
        base.setId(1);
        base.setCode("AAA");
        base.setRate("2");
        Money m = new Money(base, "100");

        Currency target = new Currency();
        target.setId(2);
        target.setCode("BBB");
        target.setRate("4");
        assertEquals("Convert amount correctly.", new BigDecimal("200"), m.convertTo(target).getAmount());
        assertEquals("Assign currency correctly.", target, m.convertTo(target).getCurrency());
        assertEquals("Convert to the same currency performs no calculations.", m, m.convertTo(m.getCurrency()));
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

        assertEquals("Round half up.", bd("1.01"), round("1.005"));
        assertEquals("Round doesn't add zeros.", bd("1"), round("1"));
        assertEquals("Round doesn't add zeros.", bd("10"), round("10")); // will be represented as 1E+1
    }

    @Test
    public void testRoundFriendly() {
        assertEquals("Friendly usd amount.", "1", new Money(usd(), "1").roundToFriendly().getAmount().toString());
        assertEquals("Friendly euro amount.", "1", new Money(euro(), "0.88").roundToFriendly().getAmount().toString());
        assertEquals("Friendly renmenbi amount.", BigDecimal.valueOf(10), new Money(renminbi(), "6.46").roundToFriendly().getAmount());
        assertEquals("Friendly kenyan shilling amount.", BigDecimal.valueOf(1000), new Money(renminbi(), "101.13").roundToFriendly().getAmount());
    }

    private BigDecimal round(String initialAmount) {
        Money m = new Money(usd(), new BigDecimal(initialAmount));
        Money r = m.roundToCurrency();
        return r.getAmount();
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private Currency usd() {
        Currency usd = new Currency();
        usd.setId(1);
        usd.setName("US Dollar");
        usd.setRate("1");
        usd.setCode("USD");
        usd.setPosition(null);
        usd.setMinorUnits(2);
        return usd;
    }

    private Currency euro() {
        Currency euro = new Currency();
        euro.setId(2);
        euro.setName("Euro");
        euro.setRate("0.8763");
        euro.setCode("EUR");
        euro.setPosition(null);
        euro.setMinorUnits(2);
        return euro;
    }

    private Currency renminbi() {
        Currency ren = new Currency();
        ren.setId(3);
        ren.setName("Yuan Renminbi");
        ren.setRate("6.4590");
        ren.setCode("CNY");
        ren.setPosition(null);
        ren.setMinorUnits(2);
        return ren;
    }

    private Currency kenyanShilling() {
        Currency kes = new Currency();
        kes.setId(4);
        kes.setName("Kenyan Shilling");
        kes.setRate("101.1310");
        kes.setCode("EUR");
        kes.setPosition(null);
        kes.setMinorUnits(2);
        return kes;
    }
}
