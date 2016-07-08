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

import dagger.Lazy;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;
import xplr.in.currencycalculator.sources.ResRawRateSource;
import xplr.in.currencycalculator.sources.ResRawSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by cheriot on 4/3/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyRepositoryTest {

    private App app;
    private CurrenciesDatabase database;
    private CurrencyBulkRepository currencyBulkRepository;

    @Before
    public void setUp() {
        app = (App)RuntimeEnvironment.application;
        // Don't actually populate the database when it's created. Let tests load smaller datasets.
        Lazy<CurrencyBulkRepository> lazy = mock(Lazy.class);
        when(lazy.get()).thenReturn(mock(CurrencyBulkRepository.class));
        database = new CurrenciesDatabase(app, lazy);
        CurrencyMetaRepository metaRepository = new CurrencyMetaRepository(
                new CurrencyMetaSource(app),
                new CurrencyMetaParser(),
                new ResRawSource(app));
        CurrencyRepository currencyRepository = new CurrencyRepository(null, database, new EventBus());
        currencyBulkRepository = new CurrencyBulkRepository(
                database,
                currencyRepository,
                metaRepository,
                new ResRawRateSource(app),
                null,
                null,
                new EventBus());
    }

    @Test
    public void testPopulatedDatabase() {
        // We initialize data in onTablesCreated.
        assertEquals("Empty database for each test.", 157L, database.countAll(Currency.class));
    }

    @Test
    public void testFetchAllCorrectCodeAndRate() {
        CurrencyRepository currencyRepository = populate();
        Currency BOB = currencyRepository.findByCode("BOB");
        assertNotNull("BOB code parsed correctly.", BOB);
        assertEquals("Rate parsed correctly.", "6.9125", BOB.getRate());
    }

    @Test
    public void testInitialInsert() {
        CurrencyRepository currencyRepository = populate();

        Currency BOB = currencyRepository.findByCode("BOB");
        currencyRepository.insertAtPosition(1, BOB);
        assertInPosition("BOB is now first in the list.", 1, "BOB");

        Currency amd = currencyRepository.findByCode("AMD");
        currencyRepository.insertAtPosition(1, amd);

        assertInPosition("AMD is now first in the list.", 1, "AMD");
        assertInPosition("BOB has been shifted to second.", 2, "BOB");
        assertInPosition("ALL is still not in the list.", null, "ALL");
    }

    @Test
    public void testMoveUp() {
        populate();

        insertAt(1, "BOB");
        insertAt(1, "AED");
        insertAt(1, "AMD");
        insertAt(1, "ALL");
        insertAt(1, "AFN");

        // Now ordered AFN, ALL, AMD, AED, BOB.
        assertInPosition("AED starts with position 4.", 4, "AED");

        // Move up
        insertAt(2, "AED");

        assertInPosition("AFN is unchanged.",    1, "AFN");
        assertInPosition("AED moved to second.", 2, "AED");
        assertInPosition("ALL moved to third.",  3, "ALL");
        assertInPosition("AMD moved to fourth.", 4, "AMD");
        assertInPosition("BOB is unchanged.",    5, "BOB");
    }

    @Test
    public void testMoveDown() {
        populate();

        insertAt(1, "BOB");
        insertAt(1, "AED");
        insertAt(1, "AMD");
        insertAt(1, "ALL");
        insertAt(1, "AFN");

        // Now ordered AFN, ALL, AMD, AED, BOB.
        assertInPosition("ALL starts with position 2.", 2, "ALL");

        // Move down.
        insertAt(4, "ALL");

        assertInPosition("AFN is unchanged.",    1, "AFN");
        assertInPosition("AMD moved to second.", 2, "AMD");
        assertInPosition("AED moved to third.",  3, "AED");
        assertInPosition("ALL moved to fourth.", 4, "ALL");
        assertInPosition("BOB is unchanged.",    5, "BOB");
    }

    @Test
    public void testGetBaseCurrency() {
        CurrencyRepository currencyRepository = populate();

        insertAt(1, "BOB");
        insertAt(1, "AED");
        insertAt(1, "AMD");

        assertInPosition("AMD is first in the list.", 1, "AMD");

        Currency base = currencyRepository.findBaseCurrency();
        assertNotNull("Found baseMoney currency.", base);
        assertEquals("Base currency is AMD.", "AMD", base.getCode());
    }

    @Test
    public void testSetBaseCurrency() {
        populate();

        SharedPreferences mockSharedPrefs = mock(SharedPreferences.class);
        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPrefs.edit()).thenReturn(mockEditor);

        CurrencyRepository currencyRepository = new CurrencyRepository(
                mockSharedPrefs, database, new EventBus());

        insertAt(1, "BOB");
        insertAt(1, "AED");
        insertAt(1, "AMD");
        assertInPosition("AMD is the baseMoney currency.", 1, "AMD");

        Currency baseCurrency = currencyRepository.findByCode(Currency.class, "BOB");
        currencyRepository.setBaseMoney(new Money(baseCurrency, "100"));
        assertInPosition("BOB is now selected.", 1, "BOB");

        verify(mockEditor).putString("base_currency_amount", "100");
        verify(mockEditor).apply();
    }

    @Test
    public void testDeselect() {
        // TODO verify that remaining currencies have the correct positions
    }

    private void insertAt(int position, String code) {
        currencyRepository().insertAtPosition(position, currencyRepository().findByCode(code));
    }

    private void assertInPosition(String message, Integer position, String code) {
        assertEquals(message, position, currencyRepository().findByCode(code).getPosition());
    }

    private CurrencyRepository populate() {
        CurrencyRepository currencyRepository = currencyRepository(null);
        currencyBulkRepository.updateOrInitMeta();
        return currencyRepository;
    }

    private CurrencyRepository populate(String amount) {
        String json = resource("currencyResponse.json");
        CurrencyRepository currencyRepository = currencyRepository(amount);
        currencyBulkRepository.updateOrInitMeta();
        return currencyRepository;
    }

    private CurrencyRepository currencyRepository() {
        return new CurrencyRepository(null, database, new EventBus());
    }

    private String resource(String filename) {
        try {
            // Do not use Resources.getResource in instrumentation tests or production code!
            // http://blog.nimbledroid.com/2016/04/06/slow-ClassLoader.getResourceAsStream.html
            URL url = Resources.getResource(filename);
            return Resources.toString(url, Charset.defaultCharset());
        } catch(Exception e) {
            throw new RuntimeException("Error loading file "+filename, e);
        }
    }

    private CurrencyRepository currencyRepository(final String amount) {

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
        return new CurrencyRepository(new MockSharedPrefs(), database, new EventBus());
    }
}
