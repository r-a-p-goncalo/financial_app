package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.UUID;

public class CreateUser {

    private final UserRepository userRepository;

    public CreateUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRecord execute(CreateUserRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("User name is required.");
        }

        return userRepository.save(new UserRecord(
                new UserId(UUID.randomUUID().toString()),
                request.name().trim()
        ));
    }
}
