package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;

public interface RepositoryTestConfiguration
        extends AutoCloseable {

    String name();

    AccountRepository createAccountRepository();
    FinancialContextRepository createFinancialContextRepository();
    TransactionRepository createTransactionRepository();

    @Override
    default void close() throws Exception {
    }
}