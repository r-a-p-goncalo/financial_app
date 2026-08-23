package com.rgoncalo.financialapp.commondata.account;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

public record AccountRecordId (String accountRecordId,
                               FinancialContextId financialContextId){
}
