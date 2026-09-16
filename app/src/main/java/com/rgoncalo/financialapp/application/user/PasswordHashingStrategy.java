package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.PasswordHash;

/**
 * Produces and verifies password hashes for one named algorithm.
 */
public interface PasswordHashingStrategy {

    String id();

    PasswordHash hash(String password);

    boolean matches(String password, PasswordHash passwordHash);
}
