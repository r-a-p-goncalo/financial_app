package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Optional;
import java.util.UUID;

public class CreateUser {

    private final UserRepository userRepository;
    private final PasswordHashingStrategyResolver passwordHashingStrategies;

    public CreateUser(
            UserRepository userRepository,
            PasswordHashingStrategyResolver passwordHashingStrategies
    ) {
        this.userRepository = userRepository;
        this.passwordHashingStrategies = passwordHashingStrategies;
    }

    public UserRecord execute(CreateUserRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("User name is required.");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String name = request.name().trim();

        Optional<UserRecord> existingUser = userRepository.findByName(name);

        if (existingUser.isPresent() && existingUser.get().passwordHash() != null) {
            throw new IllegalArgumentException("User name is already in use.");
        }

        return userRepository.save(new UserRecord(
                existingUser.map(UserRecord::userId).orElseGet(
                        () -> new UserId(UUID.randomUUID().toString())
                ),
                name,
                passwordHashingStrategies.current().hash(request.password())
        ));
    }
}
