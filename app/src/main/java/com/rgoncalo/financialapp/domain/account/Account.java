package com.rgoncalo.financialapp.domain.account;

import com.rgoncalo.financialapp.domain.financialobject.FinancialObject;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;

public class Account extends FinancialObject {

    private final String name;
    private final MonetaryValue initialAmount;

    public Account(
            String id,
            String name,
            MonetaryValue initialAmount
    ) {
        super(id);
        this.name = name;
        this.initialAmount = initialAmount;
    }

    public String getName() {
        return name;
    }

    public MonetaryValue getInitialAmount() {
        return initialAmount;
    }
}