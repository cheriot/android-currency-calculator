package xplr.in.currencycalculator.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collection;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;
import xplr.in.currencycalculator.sources.ResRawSource;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by cheriot on 4/14/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyMetaRepositoryTest {

    private CurrencyMetaRepository currencyMetaRepository;

    @Before
    public void setUp() throws Exception {
        App app = (App)RuntimeEnvironment.application;
        CurrencyMetaSource source = new CurrencyMetaSource(app);
        currencyMetaRepository = new CurrencyMetaRepository(source, new CurrencyMetaParser(), new ResRawSource(app));
    }

    @Test
    public void testFindAll() {
        Collection<CurrencyMeta> metaList = currencyMetaRepository.findAll();
        assertNotNull(metaList);
        assertEquals("Correct metaList count.", 157, metaList.size());


        for(CurrencyMeta meta : metaList) {
            assertEquals("Three letter code", 3, meta.getCode().length());
            assertNotNull("Name is not null.", meta.getName());
            assertTrue("Minor currency unit is >= 0", meta.getMinorUnits() >= 0);
            assertTrue("Minor currency unit is <= 3", meta.getMinorUnits() <= 3);
        }
    }

    @Test
    public void testUsd() {
        CurrencyMeta usd = currencyMetaRepository.findByCode("USD");
        assertNotNull("Found USD.", usd);
        assertEquals("USD code.", "USD", usd.getCode());
        assertEquals("USD name.", "US Dollar", usd.getName());
        assertEquals("USD minor units.", 2, usd.getMinorUnits());
    }

    @Test
    public void testFindByCountryCode() {
        CurrencyMeta usd = currencyMetaRepository.findByCountryCode("us");
        assertNotNull("Found meta from lowercase.", usd);
        assertEquals("Meta is USD.", "USD", usd.getCode());

        assertNull("Null is null.", currencyMetaRepository.findByCountryCode(null));
        assertNull("Empty string is null.", currencyMetaRepository.findByCountryCode(""));
        assertNull("Whitespace strings are null.", currencyMetaRepository.findByCountryCode(" "));

        CurrencyMeta eur = currencyMetaRepository.findByCountryCode("FR ");
        assertNotNull("Found meta from caps with whitespace.", eur);
        assertEquals("Meta is EUR.", "EUR", eur.getCode());
    }
}
