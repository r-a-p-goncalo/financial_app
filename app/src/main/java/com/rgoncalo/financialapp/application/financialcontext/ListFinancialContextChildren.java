package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.Collection;

public class ListFinancialContextChildren {

    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextAuthorization authorization;

    public ListFinancialContextChildren(
            FinancialContextRepository financialContextRepository,
            FinancialContextAuthorization authorization
    ) {
        this.financialContextRepository = financialContextRepository;
        this.authorization = authorization;
    }

    public Collection<FinancialContextRecord> execute(
            ListFinancialContextChildrenRequest request
    ) {
        authorization.requirePermission(
                request.userId(),
                request.parentFinancialContextId(),
                FinancialContextPermission.READ
        );
        return financialContextRepository.listChildren(
                request.parentFinancialContextId()
        ).stream().filter(child -> authorization.isPermitted(
                request.userId(),
                child.financialContextId(),
                FinancialContextPermission.READ
        )).toList();
    }
}
