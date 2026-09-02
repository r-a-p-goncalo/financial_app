package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Collection;

public class ListFinancialContextChildren {

    private final FinancialContextRepository financialContextRepository;

    public ListFinancialContextChildren(
            FinancialContextRepository financialContextRepository
    ) {
        this.financialContextRepository = financialContextRepository;
    }

    public Collection<FinancialContextRecord> execute(
            ListFinancialContextChildrenRequest request
    ) {
        return financialContextRepository.listChildren(
                request.parentFinancialContextId()
        );
    }
}
