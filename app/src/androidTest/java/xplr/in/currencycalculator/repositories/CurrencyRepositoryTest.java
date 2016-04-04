package xplr.in.currencycalculator.repositories;

import android.support.test.InstrumentationRegistry;
import android.test.AndroidTestCase;

import com.google.common.io.CharStreams;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.sources.CurrencySource;

/**
 * Created by cheriot on 4/3/16.
 */
public class CurrencyRepositoryTest extends AndroidTestCase {

    public CurrencyRepositoryTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        SugarContext.init(getContext());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        SugarContext.terminate();
    }

    @Test
    public void testEmptyDatabase() {
        assertEquals("Empty database for each test.", 0L, SugarRecord.count(Currency.class));
    }

    @Test
    public void testFetchAll() throws Exception {
        String json = resource("currencyResponse.json");
        CurrencyRepository currencyRepository = currencyRepository(json);
        currencyRepository.fetchAll();
        assertEquals(5, SugarRecord.count(Currency.class));
    }

    private String resource(String filename) {
        try {
            InputStream inStream = InstrumentationRegistry.getContext().getAssets().open(filename);
            return CharStreams.toString(new InputStreamReader(inStream));
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
