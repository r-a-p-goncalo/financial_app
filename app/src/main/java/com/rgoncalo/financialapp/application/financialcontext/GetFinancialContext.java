package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.Optional;

public class GetFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextAuthorization authorization;

    public GetFinancialContext(
            FinancialContextRepository financialContextRepository,
            FinancialContextAuthorization authorization
    ) {
        this.financialContextRepository = financialContextRepository;
        this.authorization = authorization;
    }


    public Optional<FinancialContextRecord> execute(GetFinancialContextRequest request) {
        Optional<FinancialContextRecord> financialContext =
                financialContextRepository.findById(request.financialContextId());

        if (financialContext.isEmpty()) {
            return Optional.empty();
        }

        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.READ
        );

        return financialContext;
    }

}
