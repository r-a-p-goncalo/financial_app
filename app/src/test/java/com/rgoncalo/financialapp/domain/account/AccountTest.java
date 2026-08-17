package com.rgoncalo.financialapp.domain.account;

import com.rgoncalo.financialapp.domain.financialcontext.FinancialContext;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AccountTest {

    @Test
    void exposesValuesUsedToCreateAccount() {
        FinancialContext financialContext = new TestFinancialContext("context-1");
        MonetaryValue initialAmount = new MonetaryValue(new BigDecimal("250.00"));

        Account account = new Account(
                "account-1",
                "Savings",
                initialAmount,
                financialContext
        );

        assertEquals("account-1", account.getId());
        assertEquals("Savings", account.getName());
        assertEquals(0, new BigDecimal("250.00").compareTo(account.getInitialAmount().getValue()));
        assertSame(financialContext, account.getFinancialContext());
    }

    private static final class TestFinancialContext extends FinancialContext {
        private TestFinancialContext(String id) {
            super(id);
        }
    }
}
