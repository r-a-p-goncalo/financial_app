package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.client.data.account.ClientAccount;

/**
 * Holds the account currently selected by the account-level CLI.
 */
public final class AccountSession {

    private ClientAccount currentAccount;

    public void load(ClientAccount account) {
        this.currentAccount = account;
    }

    public void clear() {
        this.currentAccount = null;
    }

    public ClientAccount requireCurrentAccount() {
        if (currentAccount == null) {
            throw new ClientRuntimeException(
                    "No account is currently loaded"
            );
        }

        return currentAccount;
    }
}
