package com.rgoncalo.financialapp.configuration;


import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.database.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.database.memory.InMemoryFinancialContextRepository;

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
    public FinancialContextRepository createFinancialContextRepository() {
        return new InMemoryFinancialContextRepository();
    }
}