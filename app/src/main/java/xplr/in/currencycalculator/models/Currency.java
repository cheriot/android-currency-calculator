package xplr.in.currencycalculator.models;

import com.orm.dsl.NotNull;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.math.BigDecimal;

@Table
public class Currency {

    private Long id;
    @NotNull @Unique
    private String code;
    @NotNull
    private BigDecimal rate;
    @NotNull
    private boolean selected;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", rate=" + rate +
                ", selected=" + selected +
                '}';
    }
}
