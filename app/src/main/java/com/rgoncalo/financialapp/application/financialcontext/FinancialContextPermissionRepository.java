package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;

import java.util.Collection;
import java.util.Optional;

/**
 * Persistence boundary for permissions granted on financial contexts.
 */
public interface FinancialContextPermissionRepository {

    FinancialContextPermissionRecord save(
            FinancialContextPermissionRecord permission
    );

    Optional<FinancialContextPermissionRecord> findByUserAndContext(
            UserId userId,
            FinancialContextId financialContextId
    );

    Collection<FinancialContextPermissionRecord> listByUserId(
            UserId userId
    );

    Collection<FinancialContextPermissionRecord> listByFinancialContextId(
            FinancialContextId financialContextId
    );
}
