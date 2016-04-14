package xplr.in.currencycalculator.sources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.models.CurrencyMeta;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by cheriot on 4/14/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyMetaSourceTest {

    private CurrencyMetaSource currencyMetaSource;

    @Before
    public void setUp() throws Exception {
        currencyMetaSource = new CurrencyMetaSource(RuntimeEnvironment.application);
    }

    @Test
    public void testGet() throws Exception {
        List<CurrencyMeta> metaList = currencyMetaSource.get();
        assertNotNull(metaList);
        assertEquals("Correct metaList count.", 157, metaList.size());

        CurrencyMeta usd = null;
        for(CurrencyMeta meta : metaList) {
            assertEquals("Three letter code", 3, meta.getCode().length());
            assertNotNull("Name is not null.", meta.getName());
            assertTrue("Minor currency unit is >= 0", meta.getMinorUnits() >= 0);
            assertTrue("Minor currency unit is <= 3", meta.getMinorUnits() <= 3);
            if(meta.getCode().equals("USD")) usd = meta;
        }
        assertNotNull("Found USD.", usd);
        assertEquals("USD name.", "US Dollar", usd.getName());
        assertEquals("USD minor units.", 2, usd.getMinorUnits());
    }
}
