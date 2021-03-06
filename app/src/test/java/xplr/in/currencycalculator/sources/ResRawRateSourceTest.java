package xplr.in.currencycalculator.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by cheriot on 4/12/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ResRawRateSourceTest {

    private ResRawRateSource resRawRateSource;

    @Before
    public void setUp() throws Exception {
        resRawRateSource = new ResRawRateSource((App)RuntimeEnvironment.application);

    }

    @Test
    public void testGet() {
        String content = resRawRateSource.get();
        assertTrue(isJSONValid(content));
        assertTrue("Contents contains random code: USDBAM", content.contains("USDBAM"));
        assertTrue("Contents contains the last code: USDZWL", content.contains("USDZWL"));
        assertEquals("Content read.", 37027, content.length());
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}
