package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.ListAccountsSummaryRequest;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.*;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.util.Collection;
import java.util.Optional;

/**
 * Describes the app that a client interacts with, doing internal processing before requests for a server are made
 * <p>
 * For now, this lives only as a logic, and no real separation exists
 * </p>
 */
public class ClientApplication {

    private final Application serverApp;
    private FinancialContextRecord currentFinancialContext;

    public ClientApplication(Application serverApp){
        this.currentFinancialContext = null;
        this.serverApp = serverApp;
    }

    public FinancialContextRecord createFinancialRecord(String name){
        return serverApp.createFinancialContext().execute(new CreateFinancialContextRequest(name));
    }

    public FinancialContextRecord getCurrentFinancialContext(){
        return this.currentFinancialContext;
    }

    public Collection<FinancialContextRecord> getFinancialContexts(){
        return this.serverApp.listFinancialContextSummary().execute(new FinancialContextSummaryRequest());
    }

    public void loadIntoFinancialContext(String financialContextId) throws  ClientRuntimeException{
        Optional<FinancialContextRecord> newFinancialContext = serverApp.getFinancialContextById().execute(new GetFinancialContextRequest(financialContextId));

        if (newFinancialContext.isEmpty())
            throw new ClientRuntimeException("Error getting financial context with id: " + financialContextId);

        this.currentFinancialContext = newFinancialContext.get();

    }

    public void unloadFinancialContext() throws ClientRuntimeException {
        if(this.currentFinancialContext == null)
            throw new ClientRuntimeException("There was no financial context loaded to unload");

        this.currentFinancialContext = null;
    }

    public AccountRecord createAccount(String accountName, MonetaryValue initialAmount)  throws ClientRuntimeException {

        if(this.currentFinancialContext == null)
            throw new ClientRuntimeException("There is financial context loaded to create an account in");

        return serverApp.createAccount().execute(new CreateAccountRequest(accountName, initialAmount, this.currentFinancialContext.id()));
    }

    public Collection<AccountRecord> listAccountsSummary() throws ClientRuntimeException {

        if(this.currentFinancialContext == null)
            throw new ClientRuntimeException("There is financial context loaded to create an account in");

        return serverApp.accountSummary().execute(new ListAccountsSummaryRequest(this.currentFinancialContext.id()));
    }
}
