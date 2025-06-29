package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Hashes a plain-text password using the BCrypt algorithm.
     * The salt is automatically generated and included in the resulting hash.
     *
     * @param plainTextPassword The password to hash.
     * @return A String containing the BCrypt hash of the password.
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        // The gensalt() method automatically handles creating a secure, random salt.
        // The work factor (log rounds) is 10 by default, which is a good starting point.
        // You can increase it for more security, e.g., BCrypt.gensalt(12).
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plain-text password matches a stored BCrypt hash.
     * This method automatically extracts the salt from the hashed password to perform the comparison.
     *
     * @param plainTextPassword The password entered by the user.
     * @param hashedPassword    The hashed password retrieved from the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null || plainTextPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // This can happen if the hashedPassword string is not a valid BCrypt hash.
            // Log the error and return false for security.
            System.err.println("Error checking password. The stored hash may not be a valid BCrypt format. " + e.getMessage());
            return false;
        }
    }
}