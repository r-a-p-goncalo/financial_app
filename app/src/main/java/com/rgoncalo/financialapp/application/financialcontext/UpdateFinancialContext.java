package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

/** Updates the name of one financial context. */
public class UpdateFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextAuthorization authorization;

    public UpdateFinancialContext(
            FinancialContextRepository financialContextRepository,
            FinancialContextAuthorization authorization
    ) {
        this.financialContextRepository = financialContextRepository;
        this.authorization = authorization;
    }

    public FinancialContextRecord execute(UpdateFinancialContextRequest request) {
        FinancialContextId id = request.financialContextId();
        authorization.requirePermission(request.userId(), id,
                FinancialContextPermission.WRITE);
        FinancialContextRecord context = financialContextRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Financial context does not exist."
                ));
        return financialContextRepository.save(new FinancialContextRecord(
                id, request.name(), context.parentFinancialContextId(),
                context.overriddenAttributes()
                        | FinancialContextRecord.Attribute.NAME.mask()
        ));
    }
}
