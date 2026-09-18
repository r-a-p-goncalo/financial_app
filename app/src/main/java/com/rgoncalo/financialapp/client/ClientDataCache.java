package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores data received from the server during a client session.
 *
 * <p>Records belonging to a financial context are kept in that context's own
 * cache. Refreshing one context therefore replaces only that context's data,
 * including removing records that no longer exist on the server.</p>
 */
public final class ClientDataCache {

    private final Map<FinancialContextId, FinancialContextRecord>
            financialContextRecords = new LinkedHashMap<>();

    private final Map<FinancialContextId, FinancialContextCache>
            financialContextCaches = new LinkedHashMap<>();

    public void saveFinancialContext(FinancialContextRecord financialContext) {
        financialContextRecords.put(
                financialContext.financialContextId(),
                financialContext
        );
    }

    /**
     * Replaces the known financial-context list with a server response.
     */
    public void refreshFinancialContexts(
            Collection<FinancialContextRecord> financialContexts
    ) {
        Map<FinancialContextId, FinancialContextRecord> refreshedContexts =
                new LinkedHashMap<>();

        financialContexts.forEach(financialContext -> refreshedContexts.put(
                financialContext.financialContextId(),
                financialContext
        ));

        financialContextRecords.keySet().retainAll(refreshedContexts.keySet());
        financialContextCaches.keySet().retainAll(refreshedContexts.keySet());
        financialContextRecords.putAll(refreshedContexts);
    }

    public Optional<FinancialContextRecord> findFinancialContext(
            FinancialContextId financialContextId
    ) {
        return Optional.ofNullable(
                financialContextRecords.get(financialContextId)
        );
    }

    public Optional<FinancialContextRecord> findFinancialContextByName(
            String name
    ) {
        return financialContextRecords.values().stream()
                .filter(financialContext -> financialContext.name().equals(name))
                .findFirst();
    }

    public void saveAccountRecord(AccountRecord accountRecord) {
        FinancialContextId financialContextId = accountRecord.accountRecordId()
                .financialContextId();

        cacheFor(financialContextId).accountRecords.put(
                accountRecord.accountRecordId(),
                accountRecord
        );
    }

    /**
     * Replaces the cached account records for one financial context.
     */
    public void refreshAccountRecords(
            FinancialContextId financialContextId,
            Collection<AccountRecord> accountRecords
    ) {
        FinancialContextCache cache = cacheFor(financialContextId);
        cache.accountRecords.clear();
        accountRecords.forEach(account -> cache.accountRecords.put(
                account.accountRecordId(),
                account
        ));
    }

    public Optional<AccountRecord> findAccountRecord(
            AccountRecordId accountRecordId
    ) {
        if (accountRecordId == null) {
            return Optional.empty();
        }

        FinancialContextCache cache = financialContextCaches.get(
                accountRecordId.financialContextId()
        );

        if (cache == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(cache.accountRecords.get(accountRecordId));
    }

    /**
     * Returns the cached account name, or the supplied fallback when absent.
     */
    public String getCachedAccountRecordName(
            AccountRecordId accountRecordId,
            String toReturnIfFail
    ) {
        return findAccountRecord(accountRecordId)
                .map(AccountRecord::name)
                .orElse(toReturnIfFail);
    }

    public Optional<AccountRecord> findAccountRecordByName(
            FinancialContextId financialContextId,
            String name
    ) {
        FinancialContextCache cache = financialContextCaches.get(
                financialContextId
        );

        if (cache == null) {
            return Optional.empty();
        }

        return cache.accountRecords.values().stream()
                .filter(account -> account.name().equals(name))
                .findFirst();
    }

    public void saveTransactionRecord(TransactionRecord transactionRecord) {
        FinancialContextId financialContextId = transactionRecord
                .transactionRecordId()
                .financialContextId();

        cacheFor(financialContextId).transactionRecords.put(
                transactionRecord.transactionRecordId(),
                transactionRecord
        );
    }

    /**
     * Replaces the cached transactions for one financial context.
     */
    public void refreshTransactionRecords(
            FinancialContextId financialContextId,
            Collection<TransactionRecord> transactionRecords
    ) {
        FinancialContextCache cache = cacheFor(financialContextId);
        cache.transactionRecords.clear();
        transactionRecords.forEach(transaction -> cache.transactionRecords.put(
                transaction.transactionRecordId(),
                transaction
        ));
    }

    public Optional<TransactionRecord> findTransactionRecord(
            TransactionRecordId transactionRecordId
    ) {
        if (transactionRecordId == null) {
            return Optional.empty();
        }

        FinancialContextCache cache = financialContextCaches.get(
                transactionRecordId.financialContextId()
        );

        if (cache == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                cache.transactionRecords.get(transactionRecordId)
        );
    }

    private FinancialContextCache cacheFor(
            FinancialContextId financialContextId
    ) {
        return financialContextCaches.computeIfAbsent(
                financialContextId,
                ignored -> new FinancialContextCache()
        );
    }

    private static final class FinancialContextCache {

        private final Map<AccountRecordId, AccountRecord> accountRecords =
                new LinkedHashMap<>();

        private final Map<TransactionRecordId, TransactionRecord>
                transactionRecords = new LinkedHashMap<>();
    }
}
