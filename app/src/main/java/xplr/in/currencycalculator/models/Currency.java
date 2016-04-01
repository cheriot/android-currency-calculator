package xplr.in.currencycalculator.models;

import java.math.BigDecimal;

/**
 * Created by cheriot on 4/1/16.
 */
public class Currency {

    private String code;
    private BigDecimal rate;

    public Currency(String code, BigDecimal rate) {
        this.code = code;
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }
}
