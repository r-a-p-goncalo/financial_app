package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Optional;

public class GetUser {

    private final UserRepository userRepository;

    public GetUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserRecord> execute(GetUserRequest request) {
        return userRepository.findById(request.userId());
    }
}
