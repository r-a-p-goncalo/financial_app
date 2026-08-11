package com.rgoncalo.financialapp.domain.financialcontext;

import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.financialobject.FinancialObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FinancialContext extends FinancialObject {

    private Map<String, Account> accounts;

    public FinancialContext(String name){
        accounts = new HashMap<String, Account>();
    }

    public void addAccount(Account newAccount){
        accounts.put(newAccount.getName(), newAccount);
    }

    public Account getAccount(String accountName){
        return accounts.get(accountName);
    }

    public Collection<String> getAccountNames(){
        return accounts.keySet();
    }

}
