package xplr.in.currencycalculator.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import xplr.in.currencycalculator.BuildConfig;

import static org.junit.Assert.assertEquals;
/**
 * Created by cheriot on 7/3/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class OptionalMoneyTest {

    @Test
    public void testSetAmount() {
        OptionalMoney optionalMoney = new OptionalMoney(euro(), "100");
        optionalMoney.setAmount("1000");
        assertEquals("Sets amounts.", "1000", optionalMoney.getAmount());
        optionalMoney.setAmount(null);
        assertEquals("Null amount on app install.", "", optionalMoney.getAmount());
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
}
