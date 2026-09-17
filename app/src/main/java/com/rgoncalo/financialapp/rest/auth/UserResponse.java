package com.rgoncalo.financialapp.rest.auth;

import com.rgoncalo.financialapp.commondata.user.UserRecord;

/**
 * User data that is safe to return to an HTTP client.
 */
public record UserResponse(String userId, String name) {

    public static UserResponse from(UserRecord user) {
        return new UserResponse(user.userId().userId(), user.name());
    }
}
