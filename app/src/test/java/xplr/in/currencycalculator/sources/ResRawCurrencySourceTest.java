package xplr.in.currencycalculator.sources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import xplr.in.currencycalculator.BuildConfig;

import static junit.framework.TestCase.*;

/**
 * Created by cheriot on 4/12/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ResRawCurrencySourceTest {

    private ResRawCurrencySource resRawCurrencySource;

    @Before
    public void setUp() throws Exception {
        resRawCurrencySource = new ResRawCurrencySource(RuntimeEnvironment.application);

    }

    @Test
    public void testGet() {
        String content = resRawCurrencySource.get();
        assertTrue("Contents contains random code: USDBAM", content.contains("USDBAM"));
        assertTrue("Contents contains the last code: USDZWL", content.contains("USDZWL"));
        assertEquals("Content read.", 37026, content.length());
    }
}
