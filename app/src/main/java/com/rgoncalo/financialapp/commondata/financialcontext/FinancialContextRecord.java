package com.rgoncalo.financialapp.commondata.financialcontext;


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
