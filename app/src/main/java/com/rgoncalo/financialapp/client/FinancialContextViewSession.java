package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;

/**
 * Holds the financial context view currently selected by the client.
 */
public final class FinancialContextViewSession {

    private FinancialContextView currentView;

    public void load(FinancialContextView view) {
        this.currentView = view;
    }

    public void clear() {
        this.currentView = null;
    }

    public FinancialContextView requireCurrentView() {
        if (currentView == null) {
            throw new ClientRuntimeException(
                    "No financial context view is currently loaded"
            );
        }

        return currentView;
    }
}
