package com.rgoncalo.financialapp.commondata.financialcontext;

import com.rgoncalo.financialapp.commondata.user.UserId;

import java.time.Instant;

/**
 * A permission granted to a user for a financial context.
 */
public record FinancialContextPermissionRecord(
        FinancialContextId financialContextId,
        UserId userId,
        FinancialContextPermission permission,
        UserId grantedByUserId,
        Instant grantedAt
) {
}
