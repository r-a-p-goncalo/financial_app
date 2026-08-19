package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Collection;

public class ListFinancialContextSummary {


    private final FinancialContextRepository financialContextRepository;

    public ListFinancialContextSummary(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }

    public Collection<FinancialContextRecord> execute(ListFinancialContextSummaryRequest request) {

        return financialContextRepository.listFinancialContextsSummary();
    }

}
