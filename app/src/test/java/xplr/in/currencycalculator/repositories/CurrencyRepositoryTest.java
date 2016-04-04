package xplr.in.currencycalculator.repositories;


import com.google.common.io.Resources;
import com.orm.SugarRecord;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;

import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.sources.CurrencySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by cheriot on 4/3/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyRepositoryTest {

    @Test
    public void testEmptyDatabase() {
        assertEquals("Empty database for each test.", 0L, SugarRecord.count(Currency.class));
    }

    @Test
    public void testFetchAllCount() {
        String json = resource("currencyResponse.json");
        currencyRepository(json).fetchAll();
        assertEquals(5, SugarRecord.count(Currency.class));
    }

    @Test
    public void testFetchAllSkipsInvalid() {
        // TODO
    }

    @Test
    public void testFetchAllCorrectCodeAndRate() {
        String json = resource("currencyResponse.json");
        currencyRepository(json).fetchAll();
        Currency btc = SugarRecord.find(Currency.class, "code = 'BTC'").get(0);
        assertNotNull("BTC code parsed correctly", btc);
        assertEquals("Rate parsed correctly", new BigDecimal("0.0024"), btc.getRate());
    }

    private String resource(String filename) {
        try {
            URL url = Resources.getResource(filename);
            return Resources.toString(url, Charset.defaultCharset());
        } catch(Exception e) {
            throw new RuntimeException("Error loading file "+filename, e);
        }
    }

    private CurrencyRepository currencyRepository(final String json) {
        class MockCurrencySource implements CurrencySource {
            @Override
            public String get() {
                return json;
            }
        }
        return new CurrencyRepository(new MockCurrencySource());
    }
}
