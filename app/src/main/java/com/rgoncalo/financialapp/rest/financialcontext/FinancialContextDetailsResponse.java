package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;

import java.util.List;

/**
 * Resolved context data used when a client loads a financial context.
 */
public record FinancialContextDetailsResponse(
        FinancialContextResponse financialContext,
        List<AccountResponse> accounts,
        List<TransactionResponse> transactions
) {

    public static FinancialContextDetailsResponse from(
            EffectiveFinancialContext financialContext
    ) {
        return new FinancialContextDetailsResponse(
                FinancialContextResponse.from(
                        financialContext.financialContext()
                ),
                financialContext.accounts().stream()
                        .map(AccountResponse::from)
                        .toList(),
                financialContext.transactions().stream()
                        .map(TransactionResponse::from)
                        .toList()
        );
    }
}
