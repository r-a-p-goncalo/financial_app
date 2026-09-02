package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.application.account.GetAccount;
import com.rgoncalo.financialapp.application.account.ListAccountsSummary;
import com.rgoncalo.financialapp.application.financialcontext.CreateFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.CloneFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextChildren;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummary;
import com.rgoncalo.financialapp.application.transaction.CreateTransaction;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummary;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryForAccount;

/**
 * Server side application.
 */
public class Application {

    private final ApplicationConfiguration appConfig;

    public Application(ApplicationConfiguration appConfig) {
        this.appConfig = appConfig;
    }

    public CreateAccount createAccount() {
        return new CreateAccount(
                appConfig.accountRepository()
        );
    }

    public ListAccountsSummary accountSummary() {
        return new ListAccountsSummary(
                appConfig.accountRepository()
        );
    }

    public GetAccount getAccountById() {
        return new GetAccount(
                appConfig.accountRepository()
        );
    }

    public CreateFinancialContext createFinancialContext() {
        return new CreateFinancialContext(
                appConfig.financialContextRepository()
        );
    }

    public GetFinancialContext getFinancialContextById() {
        return new GetFinancialContext(
                appConfig.financialContextRepository()
        );
    }

    public CloneFinancialContext cloneFinancialContext() {
        return new CloneFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.accountRepository(),
                appConfig.transactionRepository()
        );
    }

    public GetEffectiveFinancialContext getEffectiveFinancialContext() {
        return new GetEffectiveFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.accountRepository(),
                appConfig.transactionRepository()
        );
    }

    public ListFinancialContextSummary listFinancialContextSummary() {
        return new ListFinancialContextSummary(
                appConfig.financialContextRepository()
        );
    }

    public ListFinancialContextChildren listFinancialContextChildren() {
        return new ListFinancialContextChildren(
                appConfig.financialContextRepository()
        );
    }

    public CreateTransaction createTransaction() {
        return new CreateTransaction(
                appConfig.transactionRepository(),
                appConfig.accountRepository()
        );
    }

    public ListTransactionsSummary transactionsSummary() {
        return new ListTransactionsSummary(
                appConfig.transactionRepository()
        );
    }

    public ListTransactionsSummaryForAccount transactionsSummaryForAccount() {
        return new ListTransactionsSummaryForAccount(
                appConfig.transactionRepository()
        );
    }
}
