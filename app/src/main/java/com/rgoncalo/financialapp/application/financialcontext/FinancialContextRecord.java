package com.rgoncalo.financialapp.application.financialcontext;

import java.math.BigDecimal;
import java.util.Collection;


/**
 * Represents the possibly partial data of an account.
 * Is used to support queries with partial information or hidden information mechanisms.
 *
 * @param name
 */
public record FinancialContextRecord(
        String id,
        String name
) {}
