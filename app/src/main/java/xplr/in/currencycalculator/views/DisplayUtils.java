package xplr.in.currencycalculator.views;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse and format for the phone's locale.
 */
public class DisplayUtils {

    public static Number parse(String strAmount) {
        Number amount;
        try {
            amount = new DecimalFormat().parse(strAmount);
        } catch (ParseException pe) {
            String msg = "Error parsing " + strAmount + ".";
            throw new RuntimeException(msg, pe);
        }
        return amount;
    }


    public static Number parseUserFormatted(String strAmount) {
        return parse(stripFormatting(strAmount));
    }

    /**
     * For parsing strings that were formatted and then modified by the user. ex 1,00.00
     * @param strAmount number string that my contain incorrect formatting symbols
     * @return a String that can be parsed by BigDecimal
     */
    public static String stripFormatting(String strAmount) {
        // Remove all formatting symbols except the decimal place.
        char c = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
        return strAmount.replaceAll("[^\\d^"+c+"]", "");
    }

    public static StringBuffer format(Number amount) {
        return formatter().format(amount, new StringBuffer(), new FieldPosition(0));
    }

    private static DecimalFormat formatter() {
        // Formatter for the default locale.
        DecimalFormat df = new DecimalFormat();
        // By default, the formatter will round values that are too precise. Since we're working
        // with BigDecimals, maintain the precision.
        df.setMaximumFractionDigits(Integer.MAX_VALUE);
        df.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return df;
    }

    public static final Pattern HEADLESS_NUMBER = Pattern.compile("^[^\\d^\\.]*0");
    public static final Pattern UNUSED_DECIMAL = Pattern.compile("(\\.0*$)");
    public static String formatWhileTyping(CharSequence chars) {
        String toFormat = chars.toString();

        boolean isNakedZero = false;
        if(chars.charAt(0) == '.') {
            isNakedZero = true;
            // Don't format a lone decimal.
            if(chars.length() == 1) return ".";
        }

        // Removing first non-zero number screws up formatting. Pretend the first zero is 1 and
        // then put the zero back at the end.
        // 10,000 -> user edit -> 0,000
        boolean isHeadlessNumber = false;
        Matcher hnMatcher = HEADLESS_NUMBER.matcher(chars);
        if(hnMatcher.find()) {
            isHeadlessNumber = true;
            // The first char may be the 0 or a grouping character so start immediately after the
            // match to replace it.
            toFormat = "1" + chars.subSequence(hnMatcher.end(), chars.length());
            // Change the first 0 to a one
            // Record that the switch has been made
        } else {
            toFormat = chars.toString();
        }

        Number number = parse(stripFormatting(toFormat));
        StringBuffer formatted = format(number);

        if(isHeadlessNumber) {
            // Formatted number should always start with the first numeric character.
            // This assumption will break for some money or percent  formats.
            formatted.replace(0,1,"0");
        }

        if(isNakedZero) {
            // Remove the 0 before the decimal place.
            formatted.deleteCharAt(0);
        }

        // Keep unused decimal and zeros.
        // 123 -> user edit -> 123. -> user edit 123.0
        Matcher udMatcher = UNUSED_DECIMAL.matcher(chars);
        if(udMatcher.find()) formatted.append(udMatcher.group());
        return formatted.toString();
    }
}
