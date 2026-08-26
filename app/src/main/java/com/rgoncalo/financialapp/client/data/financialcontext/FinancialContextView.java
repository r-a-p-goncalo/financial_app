package com.rgoncalo.financialapp.client.data.financialcontext;

import com.rgoncalo.financialapp.client.data.account.AccountTransactionSummary;
import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a financial context view, which is a financial context with the client side computations done
 *
 * For example, the value for an account at each point in time, the computed generated transactions and so on.
 */
public final class FinancialContextView {

    private final FinancialContextRecord financialContext;
    private final LocalDate date;
    private final List<ClientAccount> accounts;
    private final Map<AccountRecordId, ClientAccount> accountsById;
    private final List<FinancialContextTransactionSummary>
            transactionSummaries;

    /**
     * Builds the view as it stood at the end of {@code date}. Transactions
     * after that date are not part of any account's history or current total.
     */
    public FinancialContextView(
            FinancialContextRecord financialContext,
            LocalDate date,
            Collection<AccountRecord> accounts,
            Collection<TransactionRecord> transactions
    ) {

        this.financialContext = Objects.requireNonNull(financialContext);
        this.date = Objects.requireNonNull(date);
        Objects.requireNonNull(accounts);
        Objects.requireNonNull(transactions);

        List<TransactionRecord> transactionsInView = transactions
                .stream()
                .peek(Objects::requireNonNull)
                .filter(transaction -> !transaction.dateTime()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                        .isAfter(date))
                .toList();

        Map<AccountRecordId, ClientAccount> accountsById =
                new LinkedHashMap<>();

        for (AccountRecord account : accounts) {
            Objects.requireNonNull(account);

            ClientAccount clientAccount = new ClientAccount(
                    account,
                    transactionsInView.stream()
                            .filter(transaction -> affects(
                                    account.accountRecordId(),
                                    transaction
                            ))
                            .toList()
            );

            if (accountsById.put(
                    account.accountRecordId(),
                    clientAccount
            ) != null) {
                throw new IllegalArgumentException(
                        "Financial context view contains duplicate account: "
                                + account.accountRecordId()
                );
            }
        }

        this.accounts = List.copyOf(accountsById.values());
        this.accountsById = Map.copyOf(accountsById);
        this.transactionSummaries = transactionsInView
                .stream()
                .sorted(transactionComparator())
                .map(transaction -> new FinancialContextTransactionSummary(
                        transaction,
                        accountBalanceAfter(transaction.originAccountId(),
                                transaction),
                        accountBalanceAfter(transaction.targetAccountId(),
                                transaction)
                ))
                .toList();
    }

    public FinancialContextRecord financialContext() {
        return financialContext;
    }

    public LocalDate date() {
        return date;
    }

    public List<ClientAccount> accounts() {
        return accounts;
    }

    /**
     * Returns all transactions in the view in chronological order, including
     * the resulting value of each affected account.
     */
    public List<FinancialContextTransactionSummary>
    transactionSummaries() {
        return transactionSummaries;
    }

    public Optional<ClientAccount> findAccount(AccountRecordId accountId) {
        return Optional.ofNullable(accountsById.get(accountId));
    }

    private static boolean affects(
            AccountRecordId accountId,
            TransactionRecord transaction
    ) {
        return accountId.equals(transaction.originAccountId())
                || accountId.equals(transaction.targetAccountId());
    }

    private FinancialContextTransactionSummary.AccountBalance
    accountBalanceAfter(
            AccountRecordId accountId,
            TransactionRecord transaction
    ) {

        if (accountId == null) {
            return null;
        }

        return findAccount(accountId)
                .flatMap(account -> account.transactionSummaries()
                        .stream()
                        .filter(summary -> summary.transaction()
                                .transactionRecordId()
                                .equals(transaction.transactionRecordId()))
                        .findFirst()
                        .map(summary -> accountBalance(account, summary)))
                .orElse(null);
    }

    private static FinancialContextTransactionSummary.AccountBalance
    accountBalance(
            ClientAccount account,
            AccountTransactionSummary summary
    ) {
        return new FinancialContextTransactionSummary.AccountBalance(
                account,
                summary.totalAfterTransaction()
        );
    }

    private static Comparator<TransactionRecord> transactionComparator() {
        return Comparator.comparing(
                        TransactionRecord::dateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparing(
                        transaction -> transaction.transactionRecordId()
                                .transactionRecordId(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
    }
}
