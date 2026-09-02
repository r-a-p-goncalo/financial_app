package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

/**
 * Data required to create a lazy copy of a financial context.
 *
 * @param parentFinancialContextId the context being copied
 * @param name the child name, or {@code null} to inherit the parent name
 */
public record CloneFinancialContextRequest(
        FinancialContextId parentFinancialContextId,
        String name
) {
}
