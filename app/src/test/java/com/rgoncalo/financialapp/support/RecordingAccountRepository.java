package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class RecordingAccountRepository implements AccountRepository {

    private AccountRecord savedAccount;
    private int saveCalls;
    private String requestedSummaryFinancialContextId;
    private int summaryCalls;
    private Collection<AccountRecord> summaryResult = List.of();
    private Optional<AccountRecord> findResult = Optional.empty();

    @Override
    public AccountRecord save(AccountRecord account) {
        savedAccount = account;
        saveCalls++;
        return account;
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(String financialContextId) {
        requestedSummaryFinancialContextId = financialContextId;
        summaryCalls++;
        return summaryResult;
    }

    @Override
    public Optional<AccountRecord> findById(String id) {
        return findResult;
    }

    public AccountRecord savedAccount() {
        return savedAccount;
    }

    public int saveCalls() {
        return saveCalls;
    }

    public String requestedSummaryFinancialContextId() {
        return requestedSummaryFinancialContextId;
    }

    public int summaryCalls() {
        return summaryCalls;
    }

    public void setSummaryResult(Collection<AccountRecord> summaryResult) {
        this.summaryResult = summaryResult;
    }
}
