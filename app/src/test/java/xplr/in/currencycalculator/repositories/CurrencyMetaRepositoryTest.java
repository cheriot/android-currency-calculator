package xplr.in.currencycalculator.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collection;

import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.models.CurrencyMeta;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;

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
        CurrencyMetaSource source = new CurrencyMetaSource(RuntimeEnvironment.application);
        currencyMetaRepository = new CurrencyMetaRepository(source, new CurrencyMetaParser());
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
}
