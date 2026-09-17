package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

/**
 * A financial context representation for HTTP clients.
 */
public record FinancialContextResponse(
        String financialContextId,
        String name,
        String parentFinancialContextId
) {

    public static FinancialContextResponse from(
            FinancialContextRecord financialContext
    ) {
        return new FinancialContextResponse(
                financialContext.financialContextId().financialContextId(),
                financialContext.name(),
                financialContext.parentFinancialContextId() == null
                        ? null
                        : financialContext.parentFinancialContextId()
                                .financialContextId()
        );
    }
}
