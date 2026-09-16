package com.rgoncalo.financialapp.bootstrap;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.CreateFinancialContextRequest;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummaryRequest;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Executes bootstrap commands through application use cases before the CLI is
 * started. Configuration references are local aliases for generated IDs.
 */
public class BootstrapRunner {

    private final Application application;
    private final UserId userId;

    private static final Logger logger =
            LoggerFactory.getLogger(BootstrapRunner.class);

    public BootstrapRunner(Application application, UserId userId) {
        this.application = Objects.requireNonNull(application);
        this.userId = Objects.requireNonNull(userId);
    }

    public void run(BootstrapPlan plan) {

        Objects.requireNonNull(plan);

        if (!shouldRun(plan.mode())) {
            return;
        }

        Map<String, FinancialContextRecord> contexts = new HashMap<>();
        Map<AccountReference, AccountRecord> accounts = new HashMap<>();

        for (BootstrapCommand command : plan.commands()) {
            logger.info("Running command: {}", command.getClass().getSimpleName());
            execute(command, contexts, accounts);
        }
    }

    private boolean shouldRun(BootstrapMode mode) {

        return switch (mode) {
            case NEVER -> false;
            case ALWAYS -> true;
            case IF_EMPTY -> application
                    .listFinancialContextSummary()
                    .execute(new ListFinancialContextSummaryRequest(userId))
                    .isEmpty();
        };
    }

    private void execute(
            BootstrapCommand command,
            Map<String, FinancialContextRecord> contexts,
            Map<AccountReference, AccountRecord> accounts
    ) {

        if (command instanceof CreateFinancialContextBootstrapCommand create) {
            createFinancialContext(create, contexts);
        } else if (command instanceof CreateAccountBootstrapCommand create) {
            createAccount(create, contexts, accounts);
        } else if (command instanceof CreateTransactionBootstrapCommand create) {
            createTransaction(create, contexts, accounts);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported bootstrap command: " + command
            );
        }
    }

    private void createFinancialContext(
            CreateFinancialContextBootstrapCommand command,
            Map<String, FinancialContextRecord> contexts
    ) {

        String reference = requireValue(command.ref(), "financial context ref");

        if (contexts.containsKey(reference)) {
            throw new IllegalArgumentException(
                    "Duplicate financial context ref: " + reference
            );
        }

        FinancialContextRecord context = application
                .createFinancialContext()
                .execute(
                        new CreateFinancialContextRequest(
                                requireValue(
                                        command.name(),
                                        "financial context name"
                                ),
                                userId
                        )
                );

        contexts.put(reference, context);
    }

    private void createAccount(
            CreateAccountBootstrapCommand command,
            Map<String, FinancialContextRecord> contexts,
            Map<AccountReference, AccountRecord> accounts
    ) {

        String contextReference = requireValue(
                command.context(),
                "account context"
        );
        String accountReference = requireValue(command.ref(), "account ref");
        FinancialContextRecord context = requireContext(
                contexts,
                contextReference
        );
        AccountReference reference = new AccountReference(
                contextReference,
                accountReference
        );

        if (accounts.containsKey(reference)) {
            throw new IllegalArgumentException(
                    "Duplicate account ref in context "
                            + contextReference
                            + ": "
                            + accountReference
            );
        }

        AccountRecord account = application
                .createAccount()
                .execute(
                        new CreateAccountRequest(
                                requireValue(command.name(), "account name"),
                                new MonetaryValue(
                                        new BigDecimal(
                                                requireValue(
                                                        command.initialAmount(),
                                                        "initial amount"
                                                )
                                        )
                                ),
                                context.financialContextId(),
                                userId
                        )
                );

        accounts.put(reference, account);
    }

    private void createTransaction(
            CreateTransactionBootstrapCommand command,
            Map<String, FinancialContextRecord> contexts,
            Map<AccountReference, AccountRecord> accounts
    ) {

        String contextReference = requireValue(
                command.context(),
                "transaction context"
        );
        FinancialContextRecord context = requireContext(
                contexts,
                contextReference
        );

        AccountRecord origin = findAccount(
                accounts,
                contextReference,
                command.origin()
        );
        AccountRecord target = findAccount(
                accounts,
                contextReference,
                command.target()
        );

        application.createTransaction().execute(
                new CreateTransactionRequest(
                        context.financialContextId(),
                        origin == null ? null : origin.accountRecordId(),
                        target == null ? null : target.accountRecordId(),
                        Instant.parse(
                                requireValue(
                                        command.dateTime(),
                                        "transaction dateTime"
                                )
                        ),
                        new MonetaryValue(
                                new BigDecimal(
                                        requireValue(
                                                command.value(),
                                                "transaction value"
                                        )
                                )
                        ),
                        userId
                )
        );
    }

    private FinancialContextRecord requireContext(
            Map<String, FinancialContextRecord> contexts,
            String reference
    ) {

        FinancialContextRecord context = contexts.get(reference);

        if (context == null) {
            throw new IllegalArgumentException(
                    "Unknown financial context ref: " + reference
            );
        }

        return context;
    }

    private AccountRecord findAccount(
            Map<AccountReference, AccountRecord> accounts,
            String contextReference,
            String accountReference
    ) {

        if (accountReference == null || accountReference.isBlank()) {
            return null;
        }

        AccountRecord account = accounts.get(
                new AccountReference(contextReference, accountReference)
        );

        if (account == null) {
            throw new IllegalArgumentException(
                    "Unknown account ref in context "
                            + contextReference
                            + ": "
                            + accountReference
            );
        }

        return account;
    }

    private String requireValue(String value, String description) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Bootstrap command requires " + description
            );
        }

        return value;
    }

    private record AccountReference(
            String contextReference,
            String accountReference
    ) {
    }
}
