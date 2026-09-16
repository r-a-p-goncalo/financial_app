package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Collection;

public class ListUsersSummary {

    private final UserRepository userRepository;

    public ListUsersSummary(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Collection<UserRecord> execute(ListUsersSummaryRequest request) {
        return userRepository.listUsersSummary();
    }
}
