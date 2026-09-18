package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.UUID;
import java.time.Instant;

/**
 * Creates a live child context of a financial context.
 *
 * <p>No accounts or transactions are copied at clone time. Effective-context
 * resolution overlays explicitly stored child objects on the parent objects.
 * Clients explicitly request a real child account before writing a transaction
 * that uses an inherited account.</p>
 */
public class CloneFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextPermissionRepository permissionRepository;
    private final FinancialContextAuthorization authorization;

    public CloneFinancialContext(
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository,
            FinancialContextAuthorization authorization
    ) {
        this.financialContextRepository = financialContextRepository;
        this.permissionRepository = permissionRepository;
        this.authorization = authorization;
    }

    public FinancialContextRecord execute(CloneFinancialContextRequest request) {

        FinancialContextRecord parent = financialContextRepository.findById(
                request.parentFinancialContextId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Parent financial context does not exist."
        ));

        authorization.requirePermission(
                request.userId(),
                parent.financialContextId(),
                FinancialContextPermission.READ //if a user can read the parent financial context, then it should be able to copy it
        );

        FinancialContextId childId = new FinancialContextId(
                UUID.randomUUID().toString()
        );

        boolean overridesName = request.name() != null;

        FinancialContextRecord child = new FinancialContextRecord(
                childId,
                overridesName ? request.name() : parent.name(),
                parent.financialContextId(),
                overridesName
                        ? FinancialContextRecord.Attribute.NAME.mask()
                        : 0
        );

        financialContextRepository.save(child);
        permissionRepository.save(new FinancialContextPermissionRecord(
                child.financialContextId(),
                request.userId(),
                FinancialContextPermission.OWNER,
                request.userId(),
                Instant.now()
        ));

        return child;
    }

}
