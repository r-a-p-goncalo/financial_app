package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record GrantFinancialContextPermissionRequest(
        FinancialContextId financialContextId,
        UserId userId,
        FinancialContextPermission permission,
        UserId grantedByUserId
) {
}
