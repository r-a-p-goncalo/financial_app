package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({RepositoryTestExtension.class, TestLoggingExtension.class})
class SoftDeleteFinancialContextTest {

    @TestTemplate
    void materializesDirectChildDataBeforeHidingItsParent(
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

        contexts.save(new FinancialContextRecord(parentId, "Personal"));
        contexts.save(new FinancialContextRecord(childId, "Personal", parentId, 0));
        TestUsers.grant(configuration, user, parentId,
                FinancialContextPermission.OWNER);
        TestUsers.grant(configuration, user, childId,
                FinancialContextPermission.OWNER);
        accounts.save(new AccountRecord(parentAccountId, "Cash", money("40")));
        transactions.save(new TransactionRecord(
                new TransactionRecordId("income", parentId), null, parentAccountId,
                Instant.parse("2026-01-01T00:00:00Z"), money("12")
        ));

        FinancialContextAuthorization authorization =
                TestUsers.authorization(configuration);
        new SoftDeleteFinancialContext(contexts, accounts, transactions,
                authorization).execute(new SoftDeleteFinancialContextRequest(
                parentId, user
        ));

        assertTrue(contexts.findById(parentId).isEmpty());
        FinancialContextRecord storedChild = contexts.findById(childId).orElseThrow();
        assertEquals(null, storedChild.parentFinancialContextId());
        assertEquals("Personal", storedChild.name());

        EffectiveFinancialContext child = new GetEffectiveFinancialContext(
                contexts, accounts, transactions, authorization
        ).execute(new GetEffectiveFinancialContextRequest(childId, user)).orElseThrow();
        assertEquals(1, child.accounts().size());
        assertEquals("Cash", child.accounts().iterator().next().name());
        assertEquals(money("40"), child.accounts().iterator().next().initialAmount());
        assertEquals(1, child.transactions().size());
        assertEquals(money("12"), child.transactions().iterator().next().value());
        assertTrue(child.transactions().iterator().next().targetAccountId()
                .financialContextId().equals(childId));
        assertFalse(child.transactions().iterator().next().targetAccountId()
                .equals(parentAccountId));
    }

    private MonetaryValue money(String value) {
        return new MonetaryValue(new BigDecimal(value));
    }
}
