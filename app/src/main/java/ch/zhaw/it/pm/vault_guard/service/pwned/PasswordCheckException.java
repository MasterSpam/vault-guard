package ch.zhaw.it.pm.vault_guard.service.pwned;

/**
 * Exception class for password check errors.
 */
public class PasswordCheckException extends RuntimeException {
    /**
     * Constructor for the PasswordCheckException class.
     *
     * @param message The error message.
     * @param cause   The cause of the error.
     */
    public PasswordCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for the PasswordCheckException class.
     *
     * @param message The error message.
     */
    public PasswordCheckException(String message) {
        super(message);
    }
}
