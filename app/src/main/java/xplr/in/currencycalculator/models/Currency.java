package xplr.in.currencycalculator.models;

import com.orm.dsl.Table;

import java.math.BigDecimal;

@Table
public class Currency {

    private Long id;
    private String code;
    private BigDecimal rate;

    public Currency() {}

    public Currency(String code, BigDecimal rate) {
        this.code = code;
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }
}
