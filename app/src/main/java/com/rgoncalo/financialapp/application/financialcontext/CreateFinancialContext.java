package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;

import java.util.UUID;

public class CreateFinancialContext {

    private final FinancialContextRepository financialContextRepository;

    public CreateFinancialContext(FinancialContextRepository financialContextRepository) {
        this.financialContextRepository = financialContextRepository;
    }


    public FinancialContextRecord execute(CreateFinancialContextRequest request) {

        String financialContextId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        FinancialContextRecord financialContextRecord = new FinancialContextRecord(
                financialContextId,
                request.name()
        );

        return financialContextRepository.save(financialContextRecord);
    }

}
