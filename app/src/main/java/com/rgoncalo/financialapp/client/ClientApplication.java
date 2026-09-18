package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.account.CloneAccountRequest;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.*;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
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
    private final UserId userId;

    private final FinancialContextSession financialContextSession;

    private final FinancialContextViewSession financialContextViewSession;

    private final ClientDataCache dataCache;

    public ClientApplication(
            Application serverApp,
            UserId userId
    ) {
        this(serverApp, userId, new ClientDataCache());
    }

    public ClientApplication(
            Application serverApp,
            UserId userId,
            ClientDataCache dataCache
    ) {
        this.financialContextSession = new FinancialContextSession();
        this.financialContextViewSession = new FinancialContextViewSession();
        this.serverApp = serverApp;
        this.userId = Objects.requireNonNull(userId);
        this.dataCache = Objects.requireNonNull(dataCache);
    }

    public FinancialContextRecord createFinancialRecord(String name){
        FinancialContextRecord financialContext = serverApp
                .createFinancialContext()
                .execute(new CreateFinancialContextRequest(name, userId));

        dataCache.saveFinancialContext(financialContext);

        return financialContext;
    }

    /**
     * Creates a lazy child copy of the currently loaded context.
     * A {@code null} name continues inheriting the parent name.
     */
    public FinancialContextRecord cloneCurrentFinancialContext(String name) {
        FinancialContextRecord parent = financialContextSession
                .requireCurrentContext();
        FinancialContextRecord clone = serverApp.cloneFinancialContext().execute(
                new CloneFinancialContextRequest(
                        parent.financialContextId(),
                        name,
                        userId
                )
        );
        FinancialContextRecord effectiveClone = effectiveContext(
                clone.financialContextId()
        ).financialContext();

        dataCache.saveFinancialContext(effectiveClone);

        return effectiveClone;
    }

    public FinancialContextRecord getCurrentFinancialContext(){
        return this.financialContextSession.requireCurrentContext();
    }

    public Collection<FinancialContextRecord> getFinancialContexts(){
        Collection<FinancialContextRecord> financialContexts = this.serverApp
                .listFinancialContextSummary()
                .execute(new ListFinancialContextSummaryRequest(userId));

        financialContexts = financialContexts.stream()
                .map(financialContext -> effectiveContext(
                        financialContext.financialContextId()
                ).financialContext())
                .toList();

        dataCache.refreshFinancialContexts(financialContexts);

        return financialContexts;
    }

    public Collection<FinancialContextRecord> getCurrentFinancialContextChildren() {
        FinancialContextId parentId = financialContextSession
                .requireCurrentContext()
                .financialContextId();
        Collection<FinancialContextRecord> children = serverApp
                .listFinancialContextChildren()
                .execute(new ListFinancialContextChildrenRequest(parentId, userId))
                .stream()
                .map(child -> effectiveContext(
                        child.financialContextId()
                ).financialContext())
                .toList();

        children.forEach(dataCache::saveFinancialContext);

        return children;
    }

    public FinancialContextId getFinancialContextIdFrom(String financialContextIdString){
        return dataCache.findFinancialContextByName(financialContextIdString)
                .map(FinancialContextRecord::financialContextId)
                .orElseGet(() -> new FinancialContextId(financialContextIdString));

    }

    public void loadIntoFinancialContext(FinancialContextId financialContextId) throws  ClientRuntimeException{
        EffectiveFinancialContext effectiveContext = effectiveContext(
                financialContextId
        );

        this.financialContextViewSession.clear();
        this.financialContextSession.load(
                effectiveContext.financialContext()
        );
        dataCache.saveFinancialContext(effectiveContext.financialContext());

    }

    /**
     * Loads one of the current context's direct children.
     */
    public void loadCurrentFinancialContextChild(
            FinancialContextId childFinancialContextId
    ) {
        FinancialContextId parentId = financialContextSession
                .requireCurrentContext()
                .financialContextId();
        FinancialContextRecord child = serverApp.getFinancialContextById()
                .execute(new GetFinancialContextRequest(
                        childFinancialContextId,
                        userId
                ))
                .orElseThrow(() -> new ClientRuntimeException(
                        "Error getting financial context with id: "
                                + childFinancialContextId
                ));

        if (!parentId.equals(child.parentFinancialContextId())) {
            throw new ClientRuntimeException(
                    "The selected financial context is not a child of the "
                            + "current context."
            );
        }

        loadIntoFinancialContext(childFinancialContextId);
    }

    public void unloadFinancialContext() throws ClientRuntimeException {
        this.financialContextViewSession.clear();
        this.financialContextSession.clear();

    }

    public AccountRecord createAccount(String accountName, MonetaryValue initialAmount)  throws ClientRuntimeException {

        AccountRecord account = serverApp.createAccount().execute(
                new CreateAccountRequest(
                        accountName,
                        initialAmount,
                        this.financialContextSession
                                .requireCurrentContext()
                                .financialContextId(),
                        userId
                )
        );

        financialContextViewSession.clear();
        dataCache.saveAccountRecord(account);

        return account;
    }

    public Collection<AccountRecord> listAccountsSummary() throws ClientRuntimeException {
        FinancialContextId financialContextId = financialContextSession
                .requireCurrentContext()
                .financialContextId();
        Collection<AccountRecord> listedAccountRecords = effectiveContext(
                financialContextId
        ).accounts();

        dataCache.refreshAccountRecords(
                financialContextId,
                listedAccountRecords
        );

        return listedAccountRecords;
    }

    public ClientDataCache dataCache() {
        return dataCache;
    }

    /**
     * Returns an account record previously received from the server, if any.
     */
    public AccountRecord getCachedAccountRecord(AccountRecordId accountRecordId) {
        return dataCache.findAccountRecord(accountRecordId).orElse(null);
    }

    /**
     *
     * @param accountRecordIdString, either an id string of an account record or its name
     *
     * @return the account record id object correspondent to the given name or id string
     */
    public AccountRecordId getAccountRecordIdFrom(String accountRecordIdString){

        FinancialContextId financialContextId = financialContextSession
                .requireCurrentContext()
                .financialContextId();

        return dataCache
                .findAccountRecordByName(financialContextId, accountRecordIdString)
                .map(AccountRecord::accountRecordId)
                .orElseGet(() -> new AccountRecordId(
                        accountRecordIdString,
                        financialContextId
                ));

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

        originAccountId = realAccountId(originAccountId, financialContextId);
        targetAccountId = realAccountId(targetAccountId, financialContextId);

        TransactionRecord transaction = serverApp
                .createTransaction()
                .execute(
                        new CreateTransactionRequest(
                                financialContextId,
                                originAccountId,
                                targetAccountId,
                                dateTime,
                                value,
                                userId
                        )
                );

        financialContextViewSession.clear();
        dataCache.saveTransactionRecord(transaction);

        return transaction;
    }

    private AccountRecordId realAccountId(
            AccountRecordId accountRecordId,
            FinancialContextId financialContextId
    ) {
        if (accountRecordId == null
                || financialContextId.equals(
                accountRecordId.financialContextId()
        )) {
            return accountRecordId;
        }

        AccountRecord account = serverApp.cloneAccount().execute(
                new CloneAccountRequest(
                        accountRecordId,
                        financialContextId,
                        userId
                )
        );
        dataCache.saveAccountRecord(account);

        return account.accountRecordId();
    }

    public Collection<TransactionRecord> listTransactionsSummary()
            throws ClientRuntimeException {

        FinancialContextId financialContextId =
                financialContextSession
                        .requireCurrentContext()
                        .financialContextId();

        Collection<TransactionRecord> transactions = effectiveContext(
                financialContextId
        ).transactions();

        dataCache.refreshTransactionRecords(financialContextId, transactions);

        return transactions;
    }

    /**
     * Loads a dated, calculated view of the current financial context.
     */
    public FinancialContextView loadFinancialContextView(LocalDate date) {

        FinancialContextRecord financialContext = financialContextSession
                .requireCurrentContext();

        EffectiveFinancialContext effectiveContext = effectiveContext(
                financialContext.financialContextId()
        );
        financialContext = effectiveContext.financialContext();
        financialContextSession.load(financialContext);
        dataCache.saveFinancialContext(financialContext);
        Collection<AccountRecord> accounts = effectiveContext.accounts();
        Collection<TransactionRecord> transactions = effectiveContext
                .transactions();

        dataCache.refreshAccountRecords(
                financialContext.financialContextId(),
                accounts
        );
        dataCache.refreshTransactionRecords(
                financialContext.financialContextId(),
                transactions
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

    private EffectiveFinancialContext effectiveContext(
            FinancialContextId financialContextId
    ) {
        return serverApp.getEffectiveFinancialContext().execute(
                new GetEffectiveFinancialContextRequest(financialContextId, userId)
        ).orElseThrow(() -> new ClientRuntimeException(
                "Error getting financial context with id: " + financialContextId
        ));
    }
}
