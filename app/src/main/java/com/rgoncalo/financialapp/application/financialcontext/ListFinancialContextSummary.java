package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.Collection;

public class ListFinancialContextSummary {


    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextPermissionRepository permissionRepository;

    public ListFinancialContextSummary(
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository
    ) {
        this.financialContextRepository = financialContextRepository;
        this.permissionRepository = permissionRepository;
    }

    public Collection<FinancialContextRecord> execute(ListFinancialContextSummaryRequest request) {

        return permissionRepository.listByUserId(request.userId()).stream()
                .filter(permission -> permission.permission().allows(
                        FinancialContextPermission.READ
                ))
                .map(permission -> financialContextRepository.findById(
                        permission.financialContextId()
                ))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

}
