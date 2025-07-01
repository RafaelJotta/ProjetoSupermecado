package br.com.supermercado.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário para criar e verificar hashes de senha usando BCrypt.
 */
public class PasswordUtil {
    private static final int LOG_ROUNDS = 10;

    public static String hashPassword(String password_plaintext) {
        String salt = BCrypt.gensalt(LOG_ROUNDS);
        return BCrypt.hashpw(password_plaintext, salt);
    }

    public static boolean checkPassword(String password_plaintext, String stored_hash) {
        if (password_plaintext == null || stored_hash == null || stored_hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password_plaintext, stored_hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}