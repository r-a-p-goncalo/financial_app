package com.rgoncalo.financialapp.domain.financialcontext;

import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.financialobject.FinancialObject;

import java.util.Collection;

public class FinancialContext extends FinancialObject {

    Collection<Account> accountCollection;

    protected FinancialContext(String id) {
        super(id);
    }


}
