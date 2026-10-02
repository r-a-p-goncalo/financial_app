package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

/**
 * Internal maintenance operation. HTTP callers receive only the reversible
 * soft-delete operation; this removes a context and its local history.
 */
public class PermanentlyDeleteFinancialContext {

    private final SoftDeleteFinancialContext softDeleteFinancialContext;
    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextPermissionRepository permissionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public PermanentlyDeleteFinancialContext(
            SoftDeleteFinancialContext softDeleteFinancialContext,
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.softDeleteFinancialContext = softDeleteFinancialContext;
        this.financialContextRepository = financialContextRepository;
        this.permissionRepository = permissionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void execute(SoftDeleteFinancialContextRequest request) {
        softDeleteFinancialContext.execute(request);
        for (TransactionRecord transaction : transactionRepository
                .listStoredTransactions(request.financialContextId())) {
            transactionRepository.deletePermanently(transaction.transactionRecordId());
        }
        for (AccountRecord account : accountRepository.listStoredAccounts(
                request.financialContextId())) {
            accountRepository.deletePermanently(account.accountRecordId());
        }
        permissionRepository.deleteByFinancialContextId(request.financialContextId());
        financialContextRepository.deletePermanently(request.financialContextId());
    }
}
