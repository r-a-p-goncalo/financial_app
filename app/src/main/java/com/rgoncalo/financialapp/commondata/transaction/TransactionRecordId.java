package com.rgoncalo.financialapp.commondata.transaction;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

public record TransactionRecordId
        (String transactionId, FinancialContextId financialContextId){

}
