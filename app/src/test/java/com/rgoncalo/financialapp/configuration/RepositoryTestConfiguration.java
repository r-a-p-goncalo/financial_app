package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;

public interface RepositoryTestConfiguration
        extends AutoCloseable {

    String name();

    AccountRepository createAccountRepository();
    FinancialContextRepository createFinancialContextRepository();
    TransactionRepository createTransactionRepository();
    UserRepository createUserRepository();
    FinancialContextPermissionRepository
    createFinancialContextPermissionRepository();

    @Override
    default void close() throws Exception {
    }
}
