package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryTransactionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryUserRepository;

public class InMemoryRepositoryTestConfiguration
        implements RepositoryTestConfiguration {

    private final AccountRepository accountRepository =
            new InMemoryAccountRepository();
    private final FinancialContextRepository financialContextRepository =
            new InMemoryFinancialContextRepository();
    private final TransactionRepository transactionRepository =
            new InMemoryTransactionRepository();
    private final UserRepository userRepository = new InMemoryUserRepository();
    private final FinancialContextPermissionRepository permissionRepository =
            new InMemoryFinancialContextPermissionRepository();

    @Override
    public String name() {
        return "InMemory";
    }

    @Override
    public AccountRepository createAccountRepository() {
        return accountRepository;
    }

    @Override
    public FinancialContextRepository
    createFinancialContextRepository() {

        return financialContextRepository;
    }

    @Override
    public TransactionRepository
    createTransactionRepository() {

        return transactionRepository;
    }

    @Override
    public UserRepository createUserRepository() {
        return userRepository;
    }

    @Override
    public FinancialContextPermissionRepository
    createFinancialContextPermissionRepository() {
        return permissionRepository;
    }
}
