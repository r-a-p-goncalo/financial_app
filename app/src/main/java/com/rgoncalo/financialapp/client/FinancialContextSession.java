package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

public final class FinancialContextSession {

    private FinancialContextRecord currentContext;

    public void load(FinancialContextRecord context) {
        this.currentContext = context;
    }

    public void clear() {
        this.currentContext = null;
    }

    public FinancialContextRecord requireCurrentContext() {
        if (currentContext == null) {
            throw new ClientRuntimeException(
                    "No financial context is currently loaded"
            );
        }

        return currentContext;
    }
}