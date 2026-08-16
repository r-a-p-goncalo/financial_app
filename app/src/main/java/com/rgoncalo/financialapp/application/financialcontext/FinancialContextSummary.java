package com.rgoncalo.financialapp.application.financialcontext;

import java.util.Collection;

public class FinancialContextSummary {


    private final FinancialContextRepository financialContextRepository;

    public FinancialContextSummary(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }

    public Collection<FinancialContextRecord> execute(FinancialContextSummaryRequest request) {

        return financialContextRepository.listFinancialContextsSummary();
    }

}
