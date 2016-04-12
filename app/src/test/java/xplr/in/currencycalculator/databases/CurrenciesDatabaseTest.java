package xplr.in.currencycalculator.databases;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;

/**
 * Created by cheriot on 4/12/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrenciesDatabaseTest {

    private CurrenciesDatabase database;

    @Before
    public void setUp() {
        App app = (App)RuntimeEnvironment.application;
    }

    @Test
    public void testTODO() {

    }
}
