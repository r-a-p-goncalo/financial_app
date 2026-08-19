package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;

public interface RepositoryTestConfiguration
        extends AutoCloseable {

    String name();

    AccountRepository createAccountRepository();
    FinancialContextRepository createFinancialContextRepository();

    @Override
    default void close() throws Exception {
    }
}