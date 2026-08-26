package com.rgoncalo.financialapp.client.data.financialcontext;

import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
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

    public Optional<ClientAccount> findAccount(AccountRecordId accountId) {
        return Optional.ofNullable(accountsById.get(accountId));
    }

    /**
     * Resolves an account first by ID and then by its displayed name.
     */
    public Optional<ClientAccount> findAccount(String accountIdOrName) {
        return accounts.stream()
                .filter(account -> account.account().accountRecordId()
                        .accountRecordId()
                        .equals(accountIdOrName))
                .findFirst()
                .or(() -> accounts.stream()
                        .filter(account -> Objects.equals(
                                account.account().name(),
                                accountIdOrName
                        ))
                        .findFirst());
    }

    private static boolean affects(
            AccountRecordId accountId,
            TransactionRecord transaction
    ) {
        return accountId.equals(transaction.originAccountId())
                || accountId.equals(transaction.targetAccountId());
    }
}
