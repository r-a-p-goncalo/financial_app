package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.ListAccountsSummary;
import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.application.financialcontext.*;

/**
 * Server side app
 */
public class Application {

    private final ApplicationConfiguration appConfig;

    public Application(ApplicationConfiguration appConfig) {
        this.appConfig = appConfig;
    }

    public CreateAccount createAccount() {
        return new CreateAccount(this.appConfig.accountRepository());
    }

    public ListAccountsSummary accountSummary(){
        return new ListAccountsSummary(this.appConfig.accountRepository());
    }

    public CreateFinancialContext createFinancialContext(){
        return new CreateFinancialContext(this.appConfig.financialContextRepository());
    }

    public GetFinancialContext getFinancialContextById(){
        return new GetFinancialContext(this.appConfig.financialContextRepository());
    }

    public FinancialContextSummary listFinancialContextSummary(){
        return new FinancialContextSummary(this.appConfig.financialContextRepository());
    }
}