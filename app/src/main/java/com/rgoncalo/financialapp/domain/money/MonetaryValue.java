package com.rgoncalo.financialapp.domain.money;

import java.math.BigDecimal;

public class MonetaryValue {

    Unit unit;
    BigDecimal value;

    public MonetaryValue(BigDecimal newValue){
        value = newValue;
    }

    public MonetaryValue(Double newValue){
        value = BigDecimal.valueOf(newValue);
    }

    public BigDecimal getValue(){
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
