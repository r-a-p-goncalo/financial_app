package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;

import java.time.Instant;

public final class TestUsers {

    private TestUsers() {
    }

    public static UserId create(RepositoryTestConfiguration configuration,
                                String id) {
        UserId userId = new UserId(id);
        configuration.createUserRepository().save(
                new UserRecord(userId, id)
        );
        return userId;
    }

    public static void grant(
            RepositoryTestConfiguration configuration,
            UserId userId,
            FinancialContextId financialContextId,
            FinancialContextPermission permission
    ) {
        configuration.createFinancialContextPermissionRepository().save(
                new FinancialContextPermissionRecord(
                        financialContextId,
                        userId,
                        permission,
                        userId,
                        Instant.parse("2026-01-01T00:00:00Z")
                )
        );
    }

    public static FinancialContextAuthorization authorization(
            RepositoryTestConfiguration configuration
    ) {
        return new FinancialContextAuthorization(
                configuration.createFinancialContextPermissionRepository()
        );
    }
}
