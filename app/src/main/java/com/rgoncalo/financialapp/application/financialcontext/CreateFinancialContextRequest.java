package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.user.UserId;

public record CreateFinancialContextRequest(String name, UserId userId) {
}
