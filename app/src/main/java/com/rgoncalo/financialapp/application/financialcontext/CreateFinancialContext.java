package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.user.UserRepository;

import java.time.Instant;
import java.util.UUID;

public class CreateFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public CreateFinancialContext(
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository,
            UserRepository userRepository
    ) {
        this.financialContextRepository = financialContextRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }


    public FinancialContextRecord execute(CreateFinancialContextRequest request) {
        userRepository.findById(request.userId()).orElseThrow(
                () -> new IllegalArgumentException("User does not exist.")
        );

        String financialContextId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        FinancialContextRecord financialContextRecord = new FinancialContextRecord(
                new FinancialContextId(financialContextId),
                request.name()
        );

        FinancialContextRecord saved = financialContextRepository.save(
                financialContextRecord
        );
        permissionRepository.save(new FinancialContextPermissionRecord(
                saved.financialContextId(),
                request.userId(),
                FinancialContextPermission.OWNER,
                request.userId(),
                Instant.now()
        ));

        return saved;
    }

}
