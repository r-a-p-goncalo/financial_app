package com.rgoncalo.financialapp.infrastructure.database.memory;

import com.rgoncalo.financialapp.application.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.domain.account.Account;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new LinkedHashMap<>();

    @Override
    public Account save(Account account) {
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary() {
        return accounts.values()
                .stream()
                .map(account -> new AccountRecord(
                        account.getId(),
                        account.getName(),
                        null
                ))
                .toList();
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public int size() {
        return accounts.size();
    }

    public Collection<Account> accounts() {
        return List.copyOf(accounts.values());
    }
}