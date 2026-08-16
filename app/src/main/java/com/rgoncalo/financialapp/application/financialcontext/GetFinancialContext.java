package com.rgoncalo.financialapp.application.financialcontext;

import java.util.Optional;
import java.util.UUID;

public class GetFinancialContext {

    private final FinancialContextRepository financialContextRepository;

    public GetFinancialContext(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }


    public Optional<FinancialContextRecord> execute(GetFinancialContextRequest request) {

        return financialContextRepository.findById(request.financialContextId());
    }

}
