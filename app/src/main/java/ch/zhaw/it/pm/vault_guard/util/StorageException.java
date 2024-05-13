package ch.zhaw.it.pm.vault_guard.util;

/**
 * Exception class for storage errors.
 */
public class StorageException extends Exception {
    /**
     * Constructor for the StorageException class.
     *
     * @param message The error message.
     * @param cause   The cause of the error.
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for the StorageException class.
     *
     * @param message The error message.
     */
    public StorageException(String message) {
        super(message);
    }
}
