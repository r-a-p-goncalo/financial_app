package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.CreateAccount;

public class Application {

    private final ApplicationConfiguration appConfig;

    public Application(ApplicationConfiguration appConfig) {
        this.appConfig = appConfig;
    }

    public CreateAccount createAccount() {
        return new CreateAccount(this.appConfig.accountRepository());
    }
}