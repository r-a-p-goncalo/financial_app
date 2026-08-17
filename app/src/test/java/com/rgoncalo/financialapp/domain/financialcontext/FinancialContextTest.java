package com.rgoncalo.financialapp.domain.financialcontext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialContextTest {

    @Test
    void exposesItsIdentity() {
        FinancialContext financialContext = new FinancialContext("context-1");

        assertEquals("context-1", financialContext.getId());
    }
}
