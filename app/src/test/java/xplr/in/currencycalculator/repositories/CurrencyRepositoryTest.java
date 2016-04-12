package xplr.in.currencycalculator.repositories;


import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.common.io.Resources;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.databases.CurrenciesDatabase;
import xplr.in.currencycalculator.databases.Currency;
import xplr.in.currencycalculator.sources.CurrencySource;
import xplr.in.currencycalculator.sources.ResRawCurrencySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by cheriot on 4/3/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyRepositoryTest {

    private CurrenciesDatabase database;

    @Before
    public void setUp() {
        App app = (App)RuntimeEnvironment.application;
        database = new CurrenciesDatabase(app, new ResRawCurrencySource(app));
    }

    @Test
    public void testEmptyDatabase() {
        assertEquals("Empty database for each test.", 0L, database.countAll(Currency.class));
    }

    @Test
    public void testFetchAllSkipsInvalid() {
        populate();
        assertEquals("Skips the 6th, N/A currency.", 5, database.countAll(Currency.class));
    }

    @Test
    public void testFetchAllCorrectCodeAndRate() {
        CurrencyRepository currencyRepository = populate();
        Currency btc = currencyRepository.findByCode("BTC");
        assertNotNull("BTC code parsed correctly.", btc);
        assertEquals("Rate parsed correctly.", "0.0024", btc.getRate());
    }

    @Test
    public void testInitialInsert() {
        CurrencyRepository currencyRepository = populate();

        Currency btc = currencyRepository.findByCode("BTC");
        currencyRepository.insertAtPosition(1, btc);
        assertInPosition("BTC is now first in the list.", 1, "BTC");

        Currency amd = currencyRepository.findByCode("AMD");
        currencyRepository.insertAtPosition(1, amd);

        assertInPosition("AMD is now first in the list.", 1, "AMD");
        assertInPosition("BTC has been shifted to second.", 2, "BTC");
        assertInPosition("ALL is still not in the list.", null, "ALL");
    }

    @Test
    public void testMoveUp() {
        populate();

        insertAt(1, "BTC");
        insertAt(1, "AED");
        insertAt(1, "AMD");
        insertAt(1, "ALL");
        insertAt(1, "AFN");

        // Now ordered AFN, ALL, AMD, AED, BTC.
        assertInPosition("AED starts with position 4.", 4, "AED");

        // Move up
        insertAt(2, "AED");

        assertInPosition("AFN is unchanged.",    1, "AFN");
        assertInPosition("AED moved to second.", 2, "AED");
        assertInPosition("ALL moved to third.",  3, "ALL");
        assertInPosition("AMD moved to fourth.", 4, "AMD");
        assertInPosition("BTC is unchanged.",    5, "BTC");
    }

    @Test
    public void testMoveDown() {
        populate();

        insertAt(1, "BTC");
        insertAt(1, "AED");
        insertAt(1, "AMD");
        insertAt(1, "ALL");
        insertAt(1, "AFN");

        // Now ordered AFN, ALL, AMD, AED, BTC.
        assertInPosition("ALL starts with position 2.", 2, "ALL");

        // Move down.
        insertAt(4, "ALL");

        assertInPosition("AFN is unchanged.",    1, "AFN");
        assertInPosition("AMD moved to second.", 2, "AMD");
        assertInPosition("AED moved to third.",  3, "AED");
        assertInPosition("ALL moved to fourth.", 4, "ALL");
        assertInPosition("BTC is unchanged.",    5, "BTC");
    }

    @Test
    public void testGetBaseCurrency() {
        CurrencyRepository currencyRepository = populate();

        insertAt(1, "BTC");
        insertAt(1, "AED");
        insertAt(1, "AMD");

        assertInPosition("AMD is first in the list.", 1, "AMD");

        Currency base = currencyRepository.getBaseCurrency();
        assertNotNull("Found base currency.", base);
        assertEquals("Base currency is AMD.", "AMD", base.getCode());
    }

    private void insertAt(int position, String code) {
        currencyRepository().insertAtPosition(position, currencyRepository().findByCode(code));
    }

    private void assertInPosition(String message, Integer position, String code) {
        assertEquals(message, position, currencyRepository().findByCode(code).getPosition());
    }

    private CurrencyRepository populate() {
        String json = resource("currencyResponse.json");
        CurrencyRepository currencyRepository = currencyRepository(json, null);
        currencyRepository.fetchAll();
        return currencyRepository;
    }

    private CurrencyRepository populate(String amount) {
        String json = resource("currencyResponse.json");
        CurrencyRepository currencyRepository = currencyRepository(json, amount);
        currencyRepository.fetchAll();
        return currencyRepository;
    }

    private CurrencyRepository currencyRepository() {
        return new CurrencyRepository(null, null, database, new EventBus());
    }

    private String resource(String filename) {
        try {
            // TODO replace with Android's Resources.get*(resId)
            // http://blog.nimbledroid.com/2016/04/06/slow-ClassLoader.getResourceAsStream.html
            URL url = Resources.getResource(filename);
            return Resources.toString(url, Charset.defaultCharset());
        } catch(Exception e) {
            throw new RuntimeException("Error loading file "+filename, e);
        }
    }

    private CurrencyRepository currencyRepository(final String json, final String amount) {
        class MockCurrencySource implements CurrencySource {
            @Override
            public String get() {
                return json;
            }
        }


        class MockSharedPrefs implements SharedPreferences {
            @Override
            public Map<String, ?> getAll() {
                return null;
            }
            @Nullable
            @Override
            public String getString(String key, String defValue) {
                return amount != null ? amount : defValue;
            }
            @Nullable
            @Override
            public Set<String> getStringSet(String key, Set<String> defValues) {
                return null;
            }
            @Override
            public int getInt(String key, int defValue) {
                return 0;
            }
            @Override
            public long getLong(String key, long defValue) {
                return 0;
            }
            @Override
            public float getFloat(String key, float defValue) {
                return 0;
            }
            @Override
            public boolean getBoolean(String key, boolean defValue) {
                return false;
            }
            @Override
            public boolean contains(String key) {
                return false;
            }
            @Override
            public Editor edit() {
                return null;
            }
            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }
            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }
        }
        return new CurrencyRepository(new MockSharedPrefs(), new MockCurrencySource(), database, new EventBus());
    }
}
