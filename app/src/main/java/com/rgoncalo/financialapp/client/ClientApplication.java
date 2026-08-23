package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryRequest;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.ListAccountsSummaryRequest;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.*;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.time.Instant;
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
    private final FinancialContextSession financialContextSession;

    public ClientApplication(Application serverApp){
        this.financialContextSession = new FinancialContextSession();
        this.serverApp = serverApp;
    }

    public FinancialContextRecord createFinancialRecord(String name){
        return serverApp.createFinancialContext().execute(new CreateFinancialContextRequest(name));
    }

    public FinancialContextRecord getCurrentFinancialContext(){
        return this.financialContextSession.requireCurrentContext();
    }

    public Collection<FinancialContextRecord> getFinancialContexts(){
        return this.serverApp.listFinancialContextSummary().execute(new ListFinancialContextSummaryRequest());
    }

    public void loadIntoFinancialContext(String financialContextId) throws  ClientRuntimeException{
        Optional<FinancialContextRecord> newFinancialContext = serverApp.getFinancialContextById().execute(new GetFinancialContextRequest(new FinancialContextId(financialContextId)));

        if (newFinancialContext.isEmpty())
            throw new ClientRuntimeException("Error getting financial context with id: " + financialContextId);

        this.financialContextSession.load(newFinancialContext.get());

    }

    public void unloadFinancialContext() throws ClientRuntimeException {
        this.financialContextSession.clear();

    }

    public AccountRecord createAccount(String accountName, MonetaryValue initialAmount)  throws ClientRuntimeException {

        return serverApp.createAccount().execute(new CreateAccountRequest(accountName, initialAmount, this.financialContextSession.requireCurrentContext().financialContextId()));
    }

    public Collection<AccountRecord> listAccountsSummary() throws ClientRuntimeException {

        return serverApp.accountSummary().execute(new ListAccountsSummaryRequest(this.financialContextSession.requireCurrentContext().financialContextId()));
    }

    /**
     *
     * Creates a transaction. The IDs can use null as a placeholder for financial context, as this function will use the financial context in the current session
     *
     * @param originAccountId
     * @param targetAccountId
     * @param dateTime
     * @param value
     *
     * @return
     *
     * @throws ClientRuntimeException
     */
    public TransactionRecord createTransaction(
            AccountRecordId originAccountId,
            AccountRecordId targetAccountId,
            Instant dateTime,
            MonetaryValue value
    ) throws ClientRuntimeException {

        FinancialContextId financialContextId =
                financialContextSession
                        .requireCurrentContext()
                        .financialContextId();

        if(originAccountId != null)
            originAccountId = new AccountRecordId(originAccountId.accountRecordId(), financialContextSession.requireCurrentContext().financialContextId());

        if(targetAccountId != null)
            targetAccountId = new AccountRecordId(targetAccountId.accountRecordId(), financialContextSession.requireCurrentContext().financialContextId());

        return serverApp
                .createTransaction()
                .execute(
                        new CreateTransactionRequest(
                                financialContextId,
                                originAccountId,
                                targetAccountId,
                                dateTime,
                                value
                        )
                );
    }

    public Collection<TransactionRecord> listTransactionsSummary()
            throws ClientRuntimeException {

        FinancialContextId financialContextId =
                financialContextSession
                        .requireCurrentContext()
                        .financialContextId();

        return serverApp
                .transactionsSummary()
                .execute(
                        new ListTransactionsSummaryRequest(
                                financialContextId
                        )
                );
    }
}
