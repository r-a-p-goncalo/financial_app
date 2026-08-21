package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryTransactionRepository;

public class InMemoryRepositoryTestConfiguration
        implements RepositoryTestConfiguration {

    @Override
    public String name() {
        return "InMemory";
    }

    @Override
    public AccountRepository createAccountRepository() {
        return new InMemoryAccountRepository();
    }

    @Override
    public FinancialContextRepository
    createFinancialContextRepository() {

        return new InMemoryFinancialContextRepository();
    }

    @Override
    public TransactionRepository
    createTransactionRepository() {

        return new InMemoryTransactionRepository();
    }
}