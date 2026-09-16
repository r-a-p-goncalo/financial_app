package com.rgoncalo.financialapp.infrastructure.security;

import com.rgoncalo.financialapp.application.user.PasswordHashingStrategy;
import com.rgoncalo.financialapp.commondata.user.PasswordHash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PBKDF2-HMAC-SHA-256 password hashing with a unique random salt per user.
 */
public class Pbkdf2PasswordHashingStrategy
        implements PasswordHashingStrategy {

    private static final String ID = "pbkdf2-hmac-sha256-v1";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public PasswordHash hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);

        return new PasswordHash(
                id(),
                ITERATIONS + ":"
                        + Base64.getEncoder().encodeToString(salt)
                        + ":"
                        + Base64.getEncoder().encodeToString(hash)
        );
    }

    @Override
    public boolean matches(String password, PasswordHash passwordHash) {
        if (password == null || passwordHash == null
                || passwordHash.value() == null
                || !id().equals(passwordHash.strategyId())) {
            return false;
        }

        try {
            String[] parts = passwordHash.value().split(":", -1);

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = derive(password, salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] derive(String password, byte[] salt, int iterations) {
        char[] characters = password.toCharArray();

        try {
            PBEKeySpec specification = new PBEKeySpec(
                    characters,
                    salt,
                    iterations,
                    HASH_BITS
            );

            try {
                return SecretKeyFactory.getInstance(ALGORITHM)
                        .generateSecret(specification)
                        .getEncoded();
            } finally {
                specification.clearPassword();
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Could not hash password.",
                    exception
            );
        } finally {
            java.util.Arrays.fill(characters, '\0');
        }
    }
}
