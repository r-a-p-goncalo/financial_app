package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.account.CloneAccount;
import com.rgoncalo.financialapp.application.account.CloneAccountRequest;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class
})
class CloneFinancialContextTest {

    @TestTemplate
    void projectsParentTransactionsIntoTheChildAndKeepsChildTransactionsLocal(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextRepository rawContextRepository =
                configuration.createFinancialContextRepository();
        AccountRepository rawAccountRepository =
                configuration.createAccountRepository();
        TransactionRepository rawTransactionRepository =
                configuration.createTransactionRepository();

        FinancialContextId parentId = new FinancialContextId("parent");
        rawContextRepository.save(new FinancialContextRecord(parentId, "Personal"));
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, parentId,
                FinancialContextPermission.WRITE);

        AccountRecord checking = account(
                "checking", parentId, "Checking", "1000.00"
        );
        AccountRecord savings = account(
                "savings", parentId, "Savings", "500.00"
        );
        rawAccountRepository.save(checking);
        rawAccountRepository.save(savings);

        TransactionRecord transfer = transaction(
                "transfer", parentId, checking.accountRecordId(),
                savings.accountRecordId(), "2026-08-21T10:00:00Z", "125.50"
        );
        TransactionRecord income = transaction(
                "income", parentId, null, checking.accountRecordId(),
                "2026-08-22T10:00:00Z", "800.00"
        );
        rawTransactionRepository.save(transfer);
        rawTransactionRepository.save(income);

        RecordingFinancialContextRepository contextRepository =
                new RecordingFinancialContextRepository(rawContextRepository);
        FinancialContextRecord child = new CloneFinancialContext(
                contextRepository,
                configuration.createFinancialContextPermissionRepository(),
                TestUsers.authorization(configuration)
        ).execute(new CloneFinancialContextRequest(
                parentId,
                "Holiday budget",
                userId
        ));

        assertEquals(1, contextRepository.saveCalls());

        assertNotEquals(parentId, child.financialContextId());
        assertEquals(parentId, child.parentFinancialContextId());
        assertEquals("Holiday budget", child.name());
        assertTrue(child.overrides(FinancialContextRecord.Attribute.NAME));
        assertEquals(child, contextRepository.findById(child.financialContextId())
                .orElseThrow());
        assertEquals(
                FinancialContextPermission.OWNER,
                configuration.createFinancialContextPermissionRepository()
                        .findByUserAndContext(userId, child.financialContextId())
                        .orElseThrow()
                        .permission()
        );
        assertEquals(
                java.util.List.of(child),
                contextRepository.listChildren(parentId)
        );

        assertTrue(rawAccountRepository.listAccountsSummary(
                child.financialContextId()
        ).isEmpty());

        assertTrue(rawTransactionRepository.listTransactionsSummary(
                child.financialContextId()
        ).isEmpty());

        TransactionRecord laterIncome = transaction(
                "later-income", parentId, null, checking.accountRecordId(),
                "2026-08-23T10:00:00Z", "100.00"
        );
        rawTransactionRepository.save(laterIncome);
        TransactionRecord overriddenIncome = new TransactionRecord(
                new TransactionRecordId(
                        "adjusted-income", child.financialContextId()
                ),
                null,
                null,
                income.dateTime(),
                new MonetaryValue(new BigDecimal("850.00")),
                income.transactionRecordId(),
                TransactionRecord.Attribute.VALUE.mask()
        );
        rawTransactionRepository.save(overriddenIncome);
        AccountRecord childSavings = new CloneAccount(
                rawAccountRepository,
                rawContextRepository,
                rawTransactionRepository,
                TestUsers.authorization(configuration)
        ).execute(new CloneAccountRequest(
                savings.accountRecordId(),
                child.financialContextId(),
                userId
        ));
        TransactionRecord childExpense = transaction(
                "child-expense", child.financialContextId(),
                childSavings.accountRecordId(),
                null,
                "2026-08-24T10:00:00Z", "25.00"
        );
        rawTransactionRepository.save(childExpense);

        EffectiveFinancialContext effectiveChild =
                new GetEffectiveFinancialContext(
                        rawContextRepository,
                        rawAccountRepository,
                        rawTransactionRepository,
                        TestUsers.authorization(configuration)
                ).execute(new GetEffectiveFinancialContextRequest(
                        child.financialContextId(), userId
                )).orElseThrow();

        assertEquals(4, effectiveChild.transactions().size());
        assertEquals(childSavings.accountRecordId(),
                effectiveChild.accounts().get(0).accountRecordId());
        assertEquals(checking.accountRecordId(),
                effectiveChild.accounts().get(1).accountRecordId());
        assertEquals(overriddenIncome.transactionRecordId(),
                effectiveChild.transactions().get(0).transactionRecordId());
        assertEquals(childExpense.transactionRecordId(),
                effectiveChild.transactions().get(1).transactionRecordId());
        assertFalse(effectiveChild.transactions().stream().anyMatch(
                transaction -> transaction.transactionRecordId().equals(
                        income.transactionRecordId()
                )
        ));
        assertProjectedTransaction(
                transfer,
                effectiveTransaction(effectiveChild, transfer.transactionRecordId()),
                checking.accountRecordId(),
                childSavings.accountRecordId()
        );
        assertEquals(overriddenIncome.transactionRecordId(),
                effectiveTransaction(
                        effectiveChild,
                        overriddenIncome.transactionRecordId()
                ).transactionRecordId());
        assertEquals(checking.accountRecordId(),
                effectiveTransaction(
                        effectiveChild,
                        overriddenIncome.transactionRecordId()
                ).targetAccountId());
        assertEquals(new MonetaryValue(new BigDecimal("850.00")),
                effectiveTransaction(
                        effectiveChild,
                        overriddenIncome.transactionRecordId()
                ).value());
        assertProjectedTransaction(
                laterIncome,
                effectiveTransaction(effectiveChild, laterIncome.transactionRecordId()),
                null,
                checking.accountRecordId()
        );
        assertEquals(childExpense, effectiveTransaction(
                effectiveChild, childExpense.transactionRecordId()
        ));
    }

    @TestTemplate
    void inheritsTheParentNameWhenNoChildNameIsProvided(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextRepository contextRepository =
                configuration.createFinancialContextRepository();
        FinancialContextId parentId = new FinancialContextId("parent");
        contextRepository.save(new FinancialContextRecord(parentId, "Personal"));
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, parentId,
                FinancialContextPermission.WRITE);

        FinancialContextRecord child = new CloneFinancialContext(
                contextRepository,
                configuration.createFinancialContextPermissionRepository(),
                TestUsers.authorization(configuration)
        ).execute(new CloneFinancialContextRequest(parentId, null, userId));

        assertEquals("Personal", child.name());
        assertEquals(parentId, child.parentFinancialContextId());
        assertFalse(child.overrides(FinancialContextRecord.Attribute.NAME));
        assertTrue(child.inherits(FinancialContextRecord.Attribute.NAME));
    }

    @TestTemplate
    void rejectsAnUnknownParentWithoutPersistingAChild(
            RepositoryTestConfiguration configuration
    ) {
        RecordingFinancialContextRepository contextRepository =
                new RecordingFinancialContextRepository(
                        configuration.createFinancialContextRepository()
                );
        CloneFinancialContext useCase = new CloneFinancialContext(
                contextRepository,
                configuration.createFinancialContextPermissionRepository(),
                TestUsers.authorization(configuration)
        );
        UserId userId = TestUsers.create(configuration, "alice");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(new CloneFinancialContextRequest(
                        new FinancialContextId("missing"), "Copy", userId
                ))
        );

        assertEquals("Parent financial context does not exist.",
                exception.getMessage());
        assertEquals(0, contextRepository.saveCalls());
    }

    private AccountRecord account(
            String id,
            FinancialContextId contextId,
            String name,
            String initialAmount
    ) {
        return new AccountRecord(
                new AccountRecordId(id, contextId),
                name,
                new MonetaryValue(new BigDecimal(initialAmount))
        );
    }

    private TransactionRecord transaction(
            String id,
            FinancialContextId contextId,
            AccountRecordId originAccountId,
            AccountRecordId targetAccountId,
            String dateTime,
            String value
    ) {
        return new TransactionRecord(
                new TransactionRecordId(id, contextId),
                originAccountId,
                targetAccountId,
                Instant.parse(dateTime),
                new MonetaryValue(new BigDecimal(value))
        );
    }

    private TransactionRecord effectiveTransaction(
            EffectiveFinancialContext context,
            TransactionRecordId transactionId
    ) {
        return context.transactions().stream()
                .filter(transaction -> transaction.transactionRecordId()
                        .equals(transactionId))
                .findFirst()
                .orElseThrow();
    }

    private void assertProjectedTransaction(
            TransactionRecord parent,
            TransactionRecord child,
            AccountRecordId expectedOriginAccountId,
            AccountRecordId expectedTargetAccountId
    ) {
        assertNotNull(child);
        assertEquals(parent.transactionRecordId(),
                child.transactionRecordId());
        assertEquals(expectedOriginAccountId, child.originAccountId());
        assertEquals(expectedTargetAccountId, child.targetAccountId());
        assertEquals(parent.dateTime(), child.dateTime());
        assertEquals(parent.value(), child.value());
    }

}
