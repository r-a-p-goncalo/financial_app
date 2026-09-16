package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * Grants or changes a user's permission for a financial context.
 */
public class GrantFinancialContextPermission {

    private final FinancialContextAuthorization authorization;
    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public GrantFinancialContextPermission(
            FinancialContextAuthorization authorization,
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository,
            UserRepository userRepository
    ) {
        this.authorization = Objects.requireNonNull(authorization);
        this.financialContextRepository = Objects.requireNonNull(
                financialContextRepository
        );
        this.permissionRepository = Objects.requireNonNull(
                permissionRepository
        );
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public FinancialContextPermissionRecord execute(
            GrantFinancialContextPermissionRequest request
    ) {
        authorization.requirePermission(
                request.grantedByUserId(),
                request.financialContextId(),
                FinancialContextPermission.OWNER
        );
        financialContextRepository.findById(request.financialContextId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Financial context does not exist."
                ));
        userRepository.findById(request.userId()).orElseThrow(
                () -> new IllegalArgumentException("User does not exist.")
        );
        ensureOwnerRemains(request);

        return permissionRepository.save(
                new FinancialContextPermissionRecord(
                        request.financialContextId(),
                        request.userId(),
                        request.permission(),
                        request.grantedByUserId(),
                        Instant.now()
                )
        );
    }

    private void ensureOwnerRemains(
            GrantFinancialContextPermissionRequest request
    ) {
        boolean removesAnOwner = permissionRepository.findByUserAndContext(
                        request.userId(),
                        request.financialContextId()
                )
                .map(permission -> permission.permission()
                        == FinancialContextPermission.OWNER)
                .orElse(false)
                && request.permission() != FinancialContextPermission.OWNER;

        if (!removesAnOwner) {
            return;
        }

        long ownerCount = permissionRepository.listByFinancialContextId(
                        request.financialContextId()
                )
                .stream()
                .filter(permission -> permission.permission()
                        == FinancialContextPermission.OWNER)
                .count();

        if (ownerCount == 1) {
            throw new IllegalArgumentException(
                    "A financial context must retain an owner."
            );
        }
    }
}
