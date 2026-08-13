package com.rgoncalo.financialapp.domain.money;

import java.math.BigDecimal;

/**
 * Represents a monetary amount expressed in a specific monetary unit.
 *
 * <p>A monetary value is static: it represents a value at a particular
 * point in the financial model rather than a value that changes over time.</p>
 */
public class MonetaryValue {

    Unit unit;
    BigDecimal value;

    public MonetaryValue(BigDecimal newValue){
        value = newValue;
    }

    public MonetaryValue(Double newValue){
        value = BigDecimal.valueOf(newValue);
    }

    /**
     * Returns the numeric amount represented by this monetary value.
     *
     * @return the amount in this value's {@link Unit}
     */
    public BigDecimal getValue(){
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
