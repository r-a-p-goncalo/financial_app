package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.account.SoftDeleteAccount;
import com.rgoncalo.financialapp.application.account.SoftDeleteAccountRequest;
import com.rgoncalo.financialapp.application.transaction.SoftDeleteTransaction;
import com.rgoncalo.financialapp.application.transaction.SoftDeleteTransactionRequest;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.TestUsers;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({RepositoryTestExtension.class, TestLoggingExtension.class})
class SoftDeleteDerivedRecordsTest {

    @TestTemplate
    void detachesDerivedAccountAndTransactionBeforeHidingSources(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextRepository contexts =
                configuration.createFinancialContextRepository();
        AccountRepository accounts = configuration.createAccountRepository();
        TransactionRepository transactions =
                configuration.createTransactionRepository();
        UserId user = TestUsers.create(configuration, "alice");
        FinancialContextId parentId = new FinancialContextId("parent");
        FinancialContextId childId = new FinancialContextId("child");
        AccountRecordId parentAccountId = new AccountRecordId("cash", parentId);
        AccountRecordId childAccountId = new AccountRecordId("cash-copy", childId);
        TransactionRecordId parentTransactionId = new TransactionRecordId(
                "income", parentId
        );
        TransactionRecordId childTransactionId = new TransactionRecordId(
                "income-copy", childId
        );

        contexts.save(new FinancialContextRecord(parentId, "Parent"));
        contexts.save(new FinancialContextRecord(childId, "Child", parentId, 0));
        TestUsers.grant(configuration, user, parentId,
                FinancialContextPermission.OWNER);
        TestUsers.grant(configuration, user, childId,
                FinancialContextPermission.OWNER);
        accounts.save(new AccountRecord(parentAccountId, "Cash", money("40")));
        accounts.save(new AccountRecord(childAccountId, "old", money("0"),
                parentAccountId, 0));
        transactions.save(new TransactionRecord(parentTransactionId, null,
                parentAccountId, Instant.parse("2026-01-01T00:00:00Z"),
                money("12")));
        transactions.save(new TransactionRecord(childTransactionId, null,
                childAccountId, Instant.EPOCH, money("0"), parentTransactionId,
                TransactionRecord.Attribute.TARGET_ACCOUNT.mask()));

        FinancialContextAuthorization authorization =
                TestUsers.authorization(configuration);
        new SoftDeleteAccount(accounts, contexts, transactions, authorization)
                .execute(new SoftDeleteAccountRequest(parentId, parentAccountId, user));
        new SoftDeleteTransaction(transactions, accounts, contexts, authorization)
                .execute(new SoftDeleteTransactionRequest(parentId,
                        parentTransactionId, user));

        assertTrue(accounts.findById(parentAccountId).isEmpty());
        assertTrue(transactions.findById(parentTransactionId).isEmpty());
        AccountRecord storedChildAccount = accounts.findById(childAccountId)
                .orElseThrow();
        assertEquals(null, storedChildAccount.parentAccountRecordId());
        assertEquals("Cash", storedChildAccount.name());
        assertEquals(money("40"), storedChildAccount.initialAmount());
        TransactionRecord storedChildTransaction = transactions.findById(
                childTransactionId
        ).orElseThrow();
        assertEquals(null, storedChildTransaction.parentTransactionRecordId());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"),
                storedChildTransaction.dateTime());
        assertEquals(money("12"), storedChildTransaction.value());
        assertEquals(childAccountId, storedChildTransaction.targetAccountId());
    }

    private MonetaryValue money(String value) {
        return new MonetaryValue(new BigDecimal(value));
    }
}
