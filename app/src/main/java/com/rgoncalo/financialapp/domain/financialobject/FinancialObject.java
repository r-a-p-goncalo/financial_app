package com.rgoncalo.financialapp.domain.financialobject;

public class FinancialObject {

    private final String id; // TODO: for now, this shares its identity with the database ID, but that should not be necessarily the case. It is possible that this id is irrelevant and should be removed.

    protected FinancialObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
