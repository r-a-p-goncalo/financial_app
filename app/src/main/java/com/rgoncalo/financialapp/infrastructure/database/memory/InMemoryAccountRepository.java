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

    private final Map<String, AccountRecord> accounts = new LinkedHashMap<>();

    @Override
    public AccountRecord save(AccountRecord account) {
        accounts.put(account.id(), account);
        return account;
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(String financialContextId) {
        return accounts.values()
                .stream()
                .filter(account ->
                        java.util.Objects.equals(
                                account.financialContextId(),
                                financialContextId
                        ))
                .map(account -> new AccountRecord(
                        account.id(),
                        account.name(),
                        null,
                        account.financialContextId()
                ))
                .toList();
    }



    @Override
    public Optional<AccountRecord> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public int size() {
        return accounts.size();
    }

    public Collection<AccountRecord> accounts() {
        return List.copyOf(accounts.values());
    }
}