package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.CloneFinancialContextRequest;
import com.rgoncalo.financialapp.application.financialcontext.CreateFinancialContextRequest;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextChildrenRequest;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummaryRequest;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
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
 * REST bridge for the financial-context use cases.
 */
@RestController
@RequestMapping("/api/v1/financial-contexts")
public class FinancialContextController {

    private final Application application;
    private final CurrentUser currentUser;

    public FinancialContextController(
            Application application,
            CurrentUser currentUser
    ) {
        this.application = application;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<FinancialContextResponse> list(Authentication authentication) {
        UserId userId = currentUser.userId(authentication);

        return application.listFinancialContextSummary().execute(
                new ListFinancialContextSummaryRequest(userId)
        ).stream().map(context -> FinancialContextResponse.from(
                effectiveContext(context.financialContextId(), userId)
                        .financialContext()
        )).toList();
    }

    @PostMapping
    public ResponseEntity<FinancialContextResponse> create(
            @Valid @RequestBody CreateFinancialContextHttpRequest request,
            Authentication authentication
    ) {
        FinancialContextRecord context = application.createFinancialContext()
                .execute(new CreateFinancialContextRequest(
                        request.name(),
                        currentUser.userId(authentication)
                ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinancialContextResponse.from(context));
    }

    @GetMapping("/{financialContextId}")
    public FinancialContextDetailsResponse load(
            @PathVariable String financialContextId,
            Authentication authentication
    ) {
        return FinancialContextDetailsResponse.from(effectiveContext(
                new FinancialContextId(financialContextId),
                currentUser.userId(authentication)
        ));
    }

    @GetMapping("/{financialContextId}/children")
    public List<FinancialContextResponse> children(
            @PathVariable String financialContextId,
            Authentication authentication
    ) {
        UserId userId = currentUser.userId(authentication);
        FinancialContextId parentId = new FinancialContextId(financialContextId);

        return application.listFinancialContextChildren().execute(
                new ListFinancialContextChildrenRequest(parentId, userId)
        ).stream().map(context -> FinancialContextResponse.from(
                effectiveContext(context.financialContextId(), userId)
                        .financialContext()
        )).toList();
    }

    @PostMapping("/{financialContextId}/clones")
    public ResponseEntity<FinancialContextResponse> clone(
            @PathVariable String financialContextId,
            @RequestBody(required = false) CloneFinancialContextHttpRequest request,
            Authentication authentication
    ) {
        FinancialContextRecord context = application.cloneFinancialContext()
                .execute(new CloneFinancialContextRequest(
                        new FinancialContextId(financialContextId),
                        request == null ? null : request.name(),
                        currentUser.userId(authentication)
                ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinancialContextResponse.from(context));
    }

    private EffectiveFinancialContext effectiveContext(
            FinancialContextId financialContextId,
            UserId userId
    ) {
        return application.getEffectiveFinancialContext().execute(
                new GetEffectiveFinancialContextRequest(financialContextId, userId)
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Financial context does not exist."
        ));
    }
}
