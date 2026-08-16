package com.rgoncalo.financialapp.domain.account;

import com.rgoncalo.financialapp.domain.financialcontext.FinancialContext;
import com.rgoncalo.financialapp.domain.financialobject.FinancialObject;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;

public class Account extends FinancialObject {

    private final String name;
    private final MonetaryValue initialAmount;
    private final FinancialContext financialContext;

    public Account(
            String id,
            String name,
            MonetaryValue initialAmount,
            FinancialContext financialContext
    ) {
        super(id);
        this.name = name;
        this.initialAmount = initialAmount;
        this.financialContext = financialContext;
    }

    public String getName() {
        return name;
    }

    public MonetaryValue getInitialAmount() {
        return initialAmount;
    }

    public FinancialContext getFinancialContext(){
        return this.financialContext;
    }
}