package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Optional;

public class GetFinancialContext {

    private final FinancialContextRepository financialContextRepository;

    public GetFinancialContext(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }


    public Optional<FinancialContextRecord> execute(GetFinancialContextRequest request) {

        return financialContextRepository.findById(request.financialContextId());
    }

}
