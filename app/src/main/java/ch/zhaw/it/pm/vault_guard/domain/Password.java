package ch.zhaw.it.pm.vault_guard.domain;

import ch.zhaw.it.pm.vault_guard.util.Hashing;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * The Password class represents a password in the system.
 * It contains the original password and its hashed version.
 * The original password is the password as entered by the user.
 * The hashed password is a secure representation of the password, which is used for storage and comparison.
 * This class is immutable, meaning that once a Password object is created, it cannot be changed.
 */
public class Password {
    private final String originalPassword;
    private String hashedPassword;

    /**
     * Constructs a new Password object with the given original and hashed passwords.
     * The constructor checks that neither the original password nor the hashed password is null.
     * If either is null, a NullPointerException is thrown.
     *
     * @param originalPassword the original password as entered by the user
     * @throws NullPointerException if originalPassword or hashedPassword is null
     */
    public Password(String originalPassword) {
        this.originalPassword = Objects.requireNonNull(originalPassword, "originalPassword must not be null");
        initializeHashedPassword(originalPassword);
    }

    /**
     * Initializes the hashed password by computing the hash of the original password.
     * This method uses the Hashing utility class to compute the hash.
     * If the hashing algorithm is not available, a RuntimeException is thrown.
     *
     * @param originalPassword the original password
     */
    private void initializeHashedPassword(String originalPassword) {
        try {
            this.hashedPassword = Hashing.hash(originalPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the original password as entered by the user.
     * This method does not perform any transformations or modifications on the password.
     *
     * @return the original password
     */
    public String getOriginalPassword() {
        return originalPassword;
    }

    /**
     * Returns the hashed version of the password.
     * This is a secure representation of the password, which is used for storage and comparison.
     *
     * @return the hashed password
     */
    public String getHashedPassword() {
        return hashedPassword;
    }
}