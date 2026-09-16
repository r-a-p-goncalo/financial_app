package com.rgoncalo.financialapp.infrastructure.persistence.memory;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryFinancialContextPermissionRepository
        implements FinancialContextPermissionRepository {

    private final Map<PermissionKey, FinancialContextPermissionRecord>
            permissions = new LinkedHashMap<>();

    @Override
    public FinancialContextPermissionRecord save(
            FinancialContextPermissionRecord permission
    ) {
        permissions.put(PermissionKey.from(permission), permission);
        return permission;
    }

    @Override
    public Optional<FinancialContextPermissionRecord> findByUserAndContext(
            UserId userId,
            FinancialContextId financialContextId
    ) {
        return Optional.ofNullable(permissions.get(
                new PermissionKey(userId, financialContextId)
        ));
    }

    @Override
    public Collection<FinancialContextPermissionRecord> listByUserId(
            UserId userId
    ) {
        return permissions.values().stream()
                .filter(permission -> permission.userId().equals(userId))
                .toList();
    }

    @Override
    public Collection<FinancialContextPermissionRecord>
    listByFinancialContextId(FinancialContextId financialContextId) {
        return permissions.values().stream()
                .filter(permission -> permission.financialContextId().equals(
                        financialContextId
                ))
                .toList();
    }

    private record PermissionKey(
            UserId userId,
            FinancialContextId financialContextId
    ) {
        private static PermissionKey from(
                FinancialContextPermissionRecord permission
        ) {
            return new PermissionKey(
                    permission.userId(),
                    permission.financialContextId()
            );
        }
    }
}
