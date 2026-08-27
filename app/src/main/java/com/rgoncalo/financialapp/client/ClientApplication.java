package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryRequest;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.ListAccountsSummaryRequest;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.*;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

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

    private final FinancialContextViewSession financialContextViewSession;

    private Collection<FinancialContextRecord> listedFinancialContexts;

    private final Map<AccountRecordId, AccountRecord> cachedAccountRecords;

    private static final Logger logger =
            LoggerFactory.getLogger(ClientApplication.class);

    public ClientApplication(Application serverApp){
        this.financialContextSession = new FinancialContextSession();
        this.financialContextViewSession = new FinancialContextViewSession();
        this.serverApp = serverApp;

        this.cachedAccountRecords = new HashMap<AccountRecordId, AccountRecord>();
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

        this.financialContextViewSession.clear();
        this.cachedAccountRecords.clear();
        this.financialContextSession.load(newFinancialContext.get());

    }

    public void unloadFinancialContext() throws ClientRuntimeException {
        this.financialContextViewSession.clear();
        this.cachedAccountRecords.clear();
        this.financialContextSession.clear();

    }

    public AccountRecord createAccount(String accountName, MonetaryValue initialAmount)  throws ClientRuntimeException {

        AccountRecord account = serverApp.createAccount().execute(
                new CreateAccountRequest(
                        accountName,
                        initialAmount,
                        this.financialContextSession
                                .requireCurrentContext()
                                .financialContextId()
                )
        );

        financialContextViewSession.clear();

        return account;
    }

    public Collection<AccountRecord> listAccountsSummary() throws ClientRuntimeException {

        Collection<AccountRecord> listedAccountRecords = serverApp.accountSummary().execute(new ListAccountsSummaryRequest(this.financialContextSession.requireCurrentContext().financialContextId()));

        this.cachedAccountRecords.clear();

       listedAccountRecords.forEach(v -> this.cachedAccountRecords.put(v.accountRecordId(), v));

        return listedAccountRecords;
    }

    public AccountRecord getCachedAccountRecord(AccountRecordId accountRecordId){

        if(accountRecordId == null)
            return null;

        return cachedAccountRecords.get(accountRecordId);
    }

    public String getCachedAccountRecordName(AccountRecordId accountRecordId, String toReturnIfFail){

        AccountRecord accountRecord = getCachedAccountRecord(accountRecordId);

        if(accountRecord == null) {
            logger.info("Received request to get account record name that failed, with account id: {}", accountRecordId);
            return toReturnIfFail;

        }else
            return accountRecord.name();
    }

    /**
     *
     * @param accountRecordIdString, either an id string of an account record or its name
     *
     * @return the account record id object correspondent to the given name or id string
     */
    public AccountRecordId getAccountRecordIdFrom(String accountRecordIdString){

        if (cachedAccountRecords.isEmpty())
            return new AccountRecordId(accountRecordIdString, financialContextSession.requireCurrentContext().financialContextId());

        Iterator<AccountRecord> accountRecordIterator = cachedAccountRecords.values().iterator();
        AccountRecord next;

        while (accountRecordIterator.hasNext()){

            next = accountRecordIterator.next();

            if (next.name().equals(accountRecordIdString))
                return next.accountRecordId();
        }

        return new AccountRecordId(accountRecordIdString, financialContextSession.requireCurrentContext().financialContextId());

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

        TransactionRecord transaction = serverApp
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

        financialContextViewSession.clear();

        return transaction;
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
     * Loads a dated, calculated view of the current financial context.
     */
    public FinancialContextView loadFinancialContextView(LocalDate date) {

        FinancialContextRecord financialContext = financialContextSession
                .requireCurrentContext();

        Collection<AccountRecord> accounts = serverApp
                .accountSummary()
                .execute(
                        new ListAccountsSummaryRequest(
                                financialContext.financialContextId()
                        )
                );

        Collection<TransactionRecord> transactions = serverApp
                .transactionsSummary()
                .execute(
                        new ListTransactionsSummaryRequest(
                                financialContext.financialContextId()
                        )
                );

        FinancialContextView view = new FinancialContextView(
                financialContext,
                date,
                accounts,
                transactions
        );

        financialContextViewSession.load(view);

        return view;
    }

    /**
     * Loads a view for the current calendar date.
     */
    public FinancialContextView loadFinancialContextView() {
        return loadFinancialContextView(LocalDate.now());
    }

    public FinancialContextView getCurrentFinancialContextView() {
        return financialContextViewSession.requireCurrentView();
    }

    public void unloadFinancialContextView() {
        financialContextViewSession.clear();
    }
}
