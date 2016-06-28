package xplr.in.currencycalculator.views;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by cheriot on 6/24/16.
 */
public class DisplayUtilsTest {

    @Test
    public void testStripFormatting() {
        assertEquals("Parse valid integer.", "1000", DisplayUtils.stripFormatting("1000"));
        assertEquals("Parse valid double.", "10.00", DisplayUtils.stripFormatting("10.00"));
        assertEquals("Parse valid formatted double.", "1000.50", DisplayUtils.stripFormatting("1,000.50"));
        assertEquals("Parse invalid formatted double.", "100.50", DisplayUtils.stripFormatting("1,00.50"));
    }

    @Test
    public void testFormatWhileTyping() {
        assertEquals("Does not integer.", "10", DisplayUtils.formatWhileTyping("10"));
        assertEquals("Formats integer.", "1,000", DisplayUtils.formatWhileTyping("1000"));
        assertEquals("Unused decimal.", "1,000.", DisplayUtils.formatWhileTyping("1,000."));
        assertEquals("Unused decimal zero.", "1,000.0", DisplayUtils.formatWhileTyping("1,000.0"));
        assertEquals("Headless number should not become a single zero.", "000.0", DisplayUtils.formatWhileTyping("000.0"));
        assertEquals("Headless number should not become a single zero.", "0,000.0", DisplayUtils.formatWhileTyping("0,000.0"));
    }
}
