package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Optional;

/**
 * Finds a user by its unique name without exposing persistence to callers.
 */
public class GetUserByName {

    private final UserRepository userRepository;

    public GetUserByName(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserRecord> execute(GetUserByNameRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("User name is required.");
        }

        return userRepository.findByName(request.name().trim());
    }
}
