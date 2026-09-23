package com.babyshop.util;

import org.mindrot.jbcrypt.BCrypt;

/** Thin wrapper around jBCrypt for hashing and verifying passwords. */
public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false; // malformed hash
        }
    }
}
