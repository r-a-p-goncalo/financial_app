package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
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
 * REST bridge for accounts within an accessible financial context.
 */
@RestController
@RequestMapping("/api/v1/financial-contexts/{financialContextId}/accounts")
public class AccountController {

    private final Application application;
    private final CurrentUser currentUser;

    public AccountController(Application application, CurrentUser currentUser) {
        this.application = application;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AccountResponse> list(
            @PathVariable String financialContextId,
            Authentication authentication
    ) {
        return effectiveContext(financialContextId, authentication).accounts()
                .stream().map(AccountResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @PathVariable String financialContextId,
            @Valid @RequestBody CreateAccountHttpRequest request,
            Authentication authentication
    ) {
        AccountRecord account = application.createAccount().execute(
                new CreateAccountRequest(
                        request.name(),
                        new MonetaryValue(request.initialAmount()),
                        new FinancialContextId(financialContextId),
                        currentUser.userId(authentication)
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountResponse.from(account));
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
