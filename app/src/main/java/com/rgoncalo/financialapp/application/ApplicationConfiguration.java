package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;

public record ApplicationConfiguration(
        AccountRepository accountRepository,
        FinancialContextRepository financialContextRepository,
        TransactionRepository transactionRepository
) {
}