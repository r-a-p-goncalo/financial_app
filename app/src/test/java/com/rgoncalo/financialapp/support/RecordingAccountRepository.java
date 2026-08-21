package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

import java.util.Collection;
import java.util.Optional;

/**
 * A wrapper for account repositories that stores extra metadata for testing
 */
public final class RecordingAccountRepository implements AccountRepository {

    private int saveCalls;
    private int summaryCalls;
    private AccountRepository accountRepository;

    public RecordingAccountRepository(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountRecord save(AccountRecord account) {
        saveCalls++;
        return accountRepository.save(account);
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(FinancialContextId financialContextId) {
        summaryCalls++;
        return accountRepository.listAccountsSummary(financialContextId);
    }

    @Override
    public Optional<AccountRecord> findById(AccountRecordId id) {
        return accountRepository.findById(id);
    }


    public int saveCalls() {
        return saveCalls;
    }

    public int summaryCalls() {
        return summaryCalls;
    }
}
