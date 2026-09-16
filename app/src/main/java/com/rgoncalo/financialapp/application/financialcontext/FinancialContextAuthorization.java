package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.security.AccessDeniedException;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.user.UserId;

import java.util.Objects;

/**
 * Centralizes authorization decisions for context-owned financial data.
 */
public class FinancialContextAuthorization {

    private final FinancialContextPermissionRepository permissionRepository;

    public FinancialContextAuthorization(
            FinancialContextPermissionRepository permissionRepository
    ) {
        this.permissionRepository = Objects.requireNonNull(permissionRepository);
    }

    public boolean isPermitted(
            UserId userId,
            FinancialContextId financialContextId,
            FinancialContextPermission requiredPermission
    ) {
        return permissionRepository.findByUserAndContext(
                        Objects.requireNonNull(userId),
                        Objects.requireNonNull(financialContextId)
                )
                .map(permission -> permission.permission().allows(
                        Objects.requireNonNull(requiredPermission)
                ))
                .orElse(false);
    }

    public void requirePermission(
            UserId userId,
            FinancialContextId financialContextId,
            FinancialContextPermission requiredPermission
    ) {
        if (!isPermitted(userId, financialContextId, requiredPermission)) {
            throw new AccessDeniedException(
                    "User does not have " + requiredPermission
                            + " access to financial context "
                            + financialContextId.financialContextId() + "."
            );
        }
    }
}
