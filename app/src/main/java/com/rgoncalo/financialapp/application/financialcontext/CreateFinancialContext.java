package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.UUID;

public class CreateFinancialContext {

    private final FinancialContextRepository financialContextRepository;

    public CreateFinancialContext(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }


    public FinancialContextRecord execute(CreateFinancialContextRequest request) {

        String financialContextId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        FinancialContextRecord financialContextRecord = new FinancialContextRecord(
                new FinancialContextId(financialContextId),
                request.name()
        );

        return financialContextRepository.save(financialContextRecord);
    }

}
