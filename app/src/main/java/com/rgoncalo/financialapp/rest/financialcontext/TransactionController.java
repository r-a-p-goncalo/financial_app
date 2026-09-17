package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.application.transaction.CreateTransactionRequest;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.rest.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST bridge for transactions within an accessible financial context.
 */
@RestController
@RequestMapping("/api/v1/financial-contexts/{financialContextId}/transactions")
public class TransactionController {

    private final Application application;
    private final CurrentUser currentUser;

    public TransactionController(
            Application application,
            CurrentUser currentUser
    ) {
        this.application = application;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<TransactionResponse> list(
            @PathVariable String financialContextId,
            Authentication authentication
    ) {
        return effectiveContext(financialContextId, authentication).transactions()
                .stream().map(TransactionResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @PathVariable String financialContextId,
            @Valid @RequestBody CreateTransactionHttpRequest request,
            Authentication authentication
    ) {
        FinancialContextId contextId = new FinancialContextId(financialContextId);
        TransactionRecord transaction = application.createTransaction().execute(
                new CreateTransactionRequest(
                        contextId,
                        accountId(request.originAccountId(), contextId),
                        accountId(request.targetAccountId(), contextId),
                        request.dateTime(),
                        new MonetaryValue(request.value()),
                        currentUser.userId(authentication)
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transaction));
    }

    private AccountRecordId accountId(
            String accountId,
            FinancialContextId financialContextId
    ) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }

        return new AccountRecordId(accountId, financialContextId);
    }

    private EffectiveFinancialContext effectiveContext(
            String financialContextId,
            Authentication authentication
    ) {
        return application.getEffectiveFinancialContext().execute(
                new GetEffectiveFinancialContextRequest(
                        new FinancialContextId(financialContextId),
                        currentUser.userId(authentication)
                )
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Financial context does not exist."
        ));
    }
}
