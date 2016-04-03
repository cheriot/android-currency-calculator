package xplr.in.currencycalculator.models;

import com.orm.dsl.NotNull;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.math.BigDecimal;

@Table
public class Currency {

    @NotNull @Unique
    private Long id;
    @NotNull @Unique
    private String code;
    @NotNull
    private BigDecimal rate;

    public Currency() {
    }

    public void update(String code, BigDecimal rate) {
        this.code = code;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", rate=" + rate +
                '}';
    }
}
