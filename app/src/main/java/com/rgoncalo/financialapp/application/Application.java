package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.application.account.GetAccount;
import com.rgoncalo.financialapp.application.account.ListAccountsSummary;
import com.rgoncalo.financialapp.application.financialcontext.CreateFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.CloneFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.application.financialcontext.GrantFinancialContextPermission;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextChildren;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummary;
import com.rgoncalo.financialapp.application.transaction.CreateTransaction;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummary;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryForAccount;
import com.rgoncalo.financialapp.application.user.CreateUser;
import com.rgoncalo.financialapp.application.user.GetUser;
import com.rgoncalo.financialapp.application.user.ListUsersSummary;

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
                appConfig.accountRepository(),
                authorization()
        );
    }

    public ListAccountsSummary accountSummary() {
        return new ListAccountsSummary(
                appConfig.accountRepository(),
                authorization()
        );
    }

    public GetAccount getAccountById() {
        return new GetAccount(
                appConfig.accountRepository(),
                authorization()
        );
    }

    public CreateFinancialContext createFinancialContext() {
        return new CreateFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository(),
                appConfig.userRepository()
        );
    }

    public GetFinancialContext getFinancialContextById() {
        return new GetFinancialContext(
                appConfig.financialContextRepository(),
                authorization()
        );
    }

    public CloneFinancialContext cloneFinancialContext() {
        return new CloneFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.accountRepository(),
                appConfig.transactionRepository(),
                appConfig.financialContextPermissionRepository(),
                authorization()
        );
    }

    public GetEffectiveFinancialContext getEffectiveFinancialContext() {
        return new GetEffectiveFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.accountRepository(),
                appConfig.transactionRepository(),
                authorization()
        );
    }

    public ListFinancialContextSummary listFinancialContextSummary() {
        return new ListFinancialContextSummary(
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository()
        );
    }

    public ListFinancialContextChildren listFinancialContextChildren() {
        return new ListFinancialContextChildren(
                appConfig.financialContextRepository(),
                authorization()
        );
    }

    public CreateTransaction createTransaction() {
        return new CreateTransaction(
                appConfig.transactionRepository(),
                appConfig.accountRepository(),
                authorization()
        );
    }

    public ListTransactionsSummary transactionsSummary() {
        return new ListTransactionsSummary(
                appConfig.transactionRepository(),
                authorization()
        );
    }

    public ListTransactionsSummaryForAccount transactionsSummaryForAccount() {
        return new ListTransactionsSummaryForAccount(
                appConfig.transactionRepository(),
                authorization()
        );
    }

    public CreateUser createUser() {
        return new CreateUser(appConfig.userRepository());
    }

    public GetUser getUserById() {
        return new GetUser(appConfig.userRepository());
    }

    public ListUsersSummary listUsersSummary() {
        return new ListUsersSummary(appConfig.userRepository());
    }

    public GrantFinancialContextPermission
    grantFinancialContextPermission() {
        return new GrantFinancialContextPermission(
                authorization(),
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository(),
                appConfig.userRepository()
        );
    }

    private FinancialContextAuthorization authorization() {
        return new FinancialContextAuthorization(
                appConfig.financialContextPermissionRepository()
        );
    }
}
