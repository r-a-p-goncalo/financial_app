package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.CloneAccount;
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
import com.rgoncalo.financialapp.application.user.AuthenticateUser;
import com.rgoncalo.financialapp.application.user.GetUser;
import com.rgoncalo.financialapp.application.user.GetUserByName;
import com.rgoncalo.financialapp.application.user.ListUsersSummary;

/**
 * Server side application.
 */
public class Application {

    private final ApplicationConfiguration appConfig;

    public Application(ApplicationConfiguration appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Server side backend entry point to create an account in a financial context
     *
     * @return {@link CreateAccount}
     */
    public CreateAccount createAccount() {
        return new CreateAccount(
                appConfig.accountRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to explicitly clone an inherited account
     * into a financial context.
     *
     * @return {@link CloneAccount}
     */
    public CloneAccount cloneAccount() {
        return new CloneAccount(
                appConfig.accountRepository(),
                appConfig.financialContextRepository(),
                appConfig.transactionRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to get a list of accounts of a financial context
     *
     * @return {@link ListAccountsSummary}
     */
    public ListAccountsSummary accountSummary() {
        return new ListAccountsSummary(
                appConfig.accountRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to get an Account
     *
     * @return {@link GetAccount}
     */
    public GetAccount getAccountById() {
        return new GetAccount(
                appConfig.accountRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to create a financial context
     *
     * @return {@link CreateFinancialContext}
     */
    public CreateFinancialContext createFinancialContext() {
        return new CreateFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository(),
                appConfig.userRepository()
        );
    }

    /**
     * Server side backend entry point to get a financial context
     *
     * @return {@link GetFinancialContext}
     */
    public GetFinancialContext getFinancialContextById() {
        return new GetFinancialContext(
                appConfig.financialContextRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to clone a financial context
     *
     * @return {@link CloneFinancialContext}
     */
    public CloneFinancialContext cloneFinancialContext() {
        return new CloneFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to get an effective financial context
     *
     * @return {@link GetEffectiveFinancialContext}
     */
    public GetEffectiveFinancialContext getEffectiveFinancialContext() {
        return new GetEffectiveFinancialContext(
                appConfig.financialContextRepository(),
                appConfig.accountRepository(),
                appConfig.transactionRepository(),
                authorization()
        );
    }


    /**
     * Server side backend entry point to get a list of financial contexts of a user
     *
     * @return {@link ListFinancialContextSummary}
     */
    public ListFinancialContextSummary listFinancialContextSummary() {
        return new ListFinancialContextSummary(
                appConfig.financialContextRepository(),
                appConfig.financialContextPermissionRepository()
        );
    }

    /**
     * Server side backend entry point to get a list of financial contexts of a user
     *
     * @return {@link ListFinancialContextSummary}
     */
    public ListFinancialContextChildren listFinancialContextChildren() {
        return new ListFinancialContextChildren(
                appConfig.financialContextRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to create a transaction in a financial context
     *
     * @return {@link CreateTransaction}
     */
    public CreateTransaction createTransaction() {
        return new CreateTransaction(
                appConfig.transactionRepository(),
                appConfig.accountRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point get a list of transactions for a financial context
     *
     * @return {@link ListTransactionsSummary}
     */
    public ListTransactionsSummary transactionsSummary() {
        return new ListTransactionsSummary(
                appConfig.transactionRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to get a list of transactions for an account
     *
     * @return {@link ListTransactionsSummaryForAccount}
     */
    public ListTransactionsSummaryForAccount transactionsSummaryForAccount() {
        return new ListTransactionsSummaryForAccount(
                appConfig.transactionRepository(),
                authorization()
        );
    }

    /**
     * Server side backend entry point to create a user
     *
     * @return {@link CreateUser}
     */
    public CreateUser createUser() {
        return new CreateUser(
                appConfig.userRepository(),
                appConfig.passwordHashingStrategies()
        );
    }

    /**
     * Server side backend entry point to authenticate a user
     *
     * @return {@link AuthenticateUser}
     */
    public AuthenticateUser authenticateUser() {
        return new AuthenticateUser(
                appConfig.userRepository(),
                appConfig.passwordHashingStrategies()
        );
    }

    /**
     * Server side backend entry point get a user
     *
     * @return {@link GetUser}
     */
    public GetUser getUserById() {
        return new GetUser(appConfig.userRepository());
    }

    /**
     * Finds a user by its unique name.
     *
     * @return {@link GetUserByName}
     */
    public GetUserByName getUserByName() {
        return new GetUserByName(appConfig.userRepository());
    }

    /**
     * Server side backend entry point list users
     *
     * @return {@link ListUsersSummary}
     */
    public ListUsersSummary listUsersSummary() {
        return new ListUsersSummary(appConfig.userRepository());
    }

    /**
     * Server side backend entry point for a user to grant another user a financial context permission
     *
     * @return {@link GrantFinancialContextPermission}
     */
    public GrantFinancialContextPermission grantFinancialContextPermission() {
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
