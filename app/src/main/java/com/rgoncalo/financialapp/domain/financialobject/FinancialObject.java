package com.rgoncalo.financialapp.domain.financialobject;

public class FinancialObject {

    private final String id;

    protected FinancialObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
