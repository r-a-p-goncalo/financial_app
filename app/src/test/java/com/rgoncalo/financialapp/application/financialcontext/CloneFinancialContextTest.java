package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
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
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import com.rgoncalo.financialapp.support.RecordingTransactionRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class
})
class CloneFinancialContextTest {

    @TestTemplate
    void clonesContextAccountsAndTransactionsIntoAnIndependentChild(
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
        RecordingAccountRepository accountRepository =
                new RecordingAccountRepository(rawAccountRepository);
        RecordingTransactionRepository transactionRepository =
                new RecordingTransactionRepository(rawTransactionRepository);

        FinancialContextRecord child = new CloneFinancialContext(
                contextRepository,
                accountRepository,
                transactionRepository,
                configuration.createFinancialContextPermissionRepository(),
                TestUsers.authorization(configuration)
        ).execute(new CloneFinancialContextRequest(
                parentId,
                "Holiday budget",
                userId
        ));

        assertEquals(1, contextRepository.saveCalls());
        assertEquals(2, accountRepository.saveCalls());
        assertEquals(2, transactionRepository.saveCalls());

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

        Map<AccountRecordId, AccountRecord> childAccountsByParentId =
                accountRepository.listAccountsSummary(child.financialContextId())
                        .stream()
                        .collect(Collectors.toMap(
                                AccountRecord::parentAccountRecordId,
                                Function.identity()
                        ));

        assertEquals(2, childAccountsByParentId.size());
        assertClonedAccount(checking, childAccountsByParentId.get(
                checking.accountRecordId()), child.financialContextId());
        assertClonedAccount(savings, childAccountsByParentId.get(
                savings.accountRecordId()), child.financialContextId());

        Map<TransactionRecordId, TransactionRecord> childTransactionsByParentId =
                transactionRepository.listTransactionsSummary(
                                child.financialContextId()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                TransactionRecord::parentTransactionRecordId,
                                Function.identity()
                        ));

        assertEquals(2, childTransactionsByParentId.size());
        assertClonedTransaction(
                transfer,
                childTransactionsByParentId.get(transfer.transactionRecordId()),
                child.financialContextId(),
                childAccountsByParentId
        );
        assertClonedTransaction(
                income,
                childTransactionsByParentId.get(income.transactionRecordId()),
                child.financialContextId(),
                childAccountsByParentId
        );
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
                configuration.createAccountRepository(),
                configuration.createTransactionRepository(),
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
        RecordingAccountRepository accountRepository =
                new RecordingAccountRepository(
                        configuration.createAccountRepository()
                );
        RecordingTransactionRepository transactionRepository =
                new RecordingTransactionRepository(
                        configuration.createTransactionRepository()
                );

        CloneFinancialContext useCase = new CloneFinancialContext(
                contextRepository,
                accountRepository,
                transactionRepository,
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
        assertEquals(0, accountRepository.saveCalls());
        assertEquals(0, transactionRepository.saveCalls());
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

    private void assertClonedAccount(
            AccountRecord parent,
            AccountRecord child,
            FinancialContextId childContextId
    ) {
        assertNotNull(child);
        assertNotEquals(parent.accountRecordId(), child.accountRecordId());
        assertEquals(childContextId, child.accountRecordId().financialContextId());
        assertEquals(parent.accountRecordId(), child.parentAccountRecordId());
        assertEquals(parent.name(), child.name());
        assertEquals(parent.initialAmount(), child.initialAmount());
        assertEquals(0, child.overriddenAttributes());
        assertTrue(child.inherits(AccountRecord.Attribute.NAME));
        assertTrue(child.inherits(AccountRecord.Attribute.INITIAL_AMOUNT));
    }

    private void assertClonedTransaction(
            TransactionRecord parent,
            TransactionRecord child,
            FinancialContextId childContextId,
            Map<AccountRecordId, AccountRecord> childAccountsByParentId
    ) {
        assertNotNull(child);
        assertNotEquals(parent.transactionRecordId(), child.transactionRecordId());
        assertEquals(childContextId,
                child.transactionRecordId().financialContextId());
        assertEquals(parent.transactionRecordId(),
                child.parentTransactionRecordId());
        assertEquals(childAccountId(parent.originAccountId(),
                        childAccountsByParentId),
                child.originAccountId());
        assertEquals(childAccountId(parent.targetAccountId(),
                        childAccountsByParentId),
                child.targetAccountId());
        assertEquals(parent.dateTime(), child.dateTime());
        assertEquals(parent.value(), child.value());
        assertEquals(0, child.overriddenAttributes());
        assertTrue(child.inherits(TransactionRecord.Attribute.ORIGIN_ACCOUNT));
        assertTrue(child.inherits(TransactionRecord.Attribute.TARGET_ACCOUNT));
        assertTrue(child.inherits(TransactionRecord.Attribute.DATE_TIME));
        assertTrue(child.inherits(TransactionRecord.Attribute.VALUE));
    }

    private AccountRecordId childAccountId(
            AccountRecordId parentAccountId,
            Map<AccountRecordId, AccountRecord> childAccountsByParentId
    ) {
        return parentAccountId == null
                ? null
                : childAccountsByParentId.get(parentAccountId).accountRecordId();
    }
}
