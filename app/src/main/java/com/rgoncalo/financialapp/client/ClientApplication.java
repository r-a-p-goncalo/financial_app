package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryForAccountRequest;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryRequest;
import com.rgoncalo.financialapp.application.account.GetAccountRequest;
import com.rgoncalo.financialapp.client.data.account.ClientAccount;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

//TODO: This maybe could be changed into a global state and then applications for each window

/**
 * Describes the app that a client interacts with, doing internal processing before requests for a server are made
 * <p>
 * For now, this lives only as a logic, and no real separation exists
 * </p>
 */
public class ClientApplication {

    private final Application serverApp;
    private final FinancialContextSession financialContextSession;
    private final AccountSession accountSession;
    private Collection<FinancialContextRecord> listedFinancialContexts;
    private Collection<AccountRecord> listedAccountRecords;

    public ClientApplication(Application serverApp){
        this.financialContextSession = new FinancialContextSession();
        this.accountSession = new AccountSession();
        this.serverApp = serverApp;
    }

    public FinancialContextRecord createFinancialRecord(String name){
        return serverApp.createFinancialContext().execute(new CreateFinancialContextRequest(name));
    }

    public FinancialContextRecord getCurrentFinancialContext(){
        return this.financialContextSession.requireCurrentContext();
    }

    public Collection<FinancialContextRecord> getFinancialContexts(){
        listedFinancialContexts = this.serverApp.listFinancialContextSummary().execute(new ListFinancialContextSummaryRequest());

        return listedFinancialContexts;
    }

    public FinancialContextId getFinancialContextIdFrom(String financialContextIdString){

        if (listedFinancialContexts == null)
            return new FinancialContextId(financialContextIdString);

        Iterator<FinancialContextRecord> financialContextRecordIterator = listedFinancialContexts.iterator();
        FinancialContextRecord next;

        while (financialContextRecordIterator.hasNext()){

            next = financialContextRecordIterator.next();

            if (next.name().equals(financialContextIdString))
                financialContextIdString = next.financialContextId().financialContextId();
        }

        return new FinancialContextId(financialContextIdString);

    }

    public void loadIntoFinancialContext(FinancialContextId financialContextId) throws  ClientRuntimeException{

        Optional<FinancialContextRecord> newFinancialContext = serverApp.getFinancialContextById().execute(new GetFinancialContextRequest(financialContextId));

        if (newFinancialContext.isEmpty())
            throw new ClientRuntimeException("Error getting financial context with id: " + financialContextId);

        this.accountSession.clear();
        this.listedAccountRecords = null;
        this.financialContextSession.load(newFinancialContext.get());

    }

    public void unloadFinancialContext() throws ClientRuntimeException {
        this.accountSession.clear();
        this.listedAccountRecords = null;
        this.financialContextSession.clear();

    }

    public AccountRecord createAccount(String accountName, MonetaryValue initialAmount)  throws ClientRuntimeException {

        return serverApp.createAccount().execute(new CreateAccountRequest(accountName, initialAmount, this.financialContextSession.requireCurrentContext().financialContextId()));
    }

    public Collection<AccountRecord> listAccountsSummary() throws ClientRuntimeException {

        listedAccountRecords = serverApp.accountSummary().execute(new ListAccountsSummaryRequest(this.financialContextSession.requireCurrentContext().financialContextId()));

        return listedAccountRecords;
    }

    public AccountRecordId getAccountRecordIdIdFrom(String accountRecordIdString){

        if (listedAccountRecords == null)
            return new AccountRecordId(accountRecordIdString, financialContextSession.requireCurrentContext().financialContextId());

        Iterator<AccountRecord> accountRecordIterator = listedAccountRecords.iterator();
        AccountRecord next;

        while (accountRecordIterator.hasNext()){

            next = accountRecordIterator.next();

            if (next.name().equals(accountRecordIdString))
                return next.accountRecordId();
        }

        return new AccountRecordId(accountRecordIdString, financialContextSession.requireCurrentContext().financialContextId());

    }

    public void loadAccount(AccountRecordId accountRecordId) {

        AccountRecordId accountIdInCurrentContext = new AccountRecordId(
                accountRecordId.accountRecordId(),
                financialContextSession
                        .requireCurrentContext()
                        .financialContextId()
        );

        Optional<AccountRecord> account = serverApp
                .getAccountById()
                .execute(
                        new GetAccountRequest(
                                accountIdInCurrentContext
                        )
                );

        if (account.isEmpty()) {
            throw new ClientRuntimeException(
                    "Error getting account with id: " + accountRecordId
            );
        }

        accountSession.load(
                new ClientAccount(account.get())
        );
    }

    public ClientAccount getCurrentAccount() {
        return accountSession.requireCurrentAccount();
    }

    public void unloadAccount() {
        accountSession.clear();
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

    /**
     * Retrieves the loaded account's transactions and turns them into a
     * chronological running-balance history.
     */
    public List<AccountTransactionSummary>
    listTransactionsSummaryForCurrentAccount() {

        ClientAccount account = accountSession.requireCurrentAccount();

        Collection<TransactionRecord> transactions = serverApp
                .transactionsSummaryForAccount()
                .execute(
                        new ListTransactionsSummaryForAccountRequest(
                                account.record().accountRecordId()
                        )
                );

        return account.summarizeTransactions(transactions);
    }
}
