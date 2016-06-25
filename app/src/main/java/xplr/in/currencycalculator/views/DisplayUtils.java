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
        return new DecimalFormat().format(amount, new StringBuffer(), new FieldPosition(0));
    }

    public static final Pattern UNUSED_DECIMAL = Pattern.compile("(\\.0*$)");
    public static String formatWhileTyping(CharSequence chars) {
        Number number = parse(stripFormatting(chars.toString()));
        StringBuffer formatted = format(number);
        Matcher matcher = UNUSED_DECIMAL.matcher(chars);
        if(matcher.find()) formatted.append(matcher.group());
        return formatted.toString();
    }
}
