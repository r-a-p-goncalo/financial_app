package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;

public record ApplicationConfiguration(AccountRepository accountRepository,
                                       FinancialContextRepository financialContextRepository) {

}
