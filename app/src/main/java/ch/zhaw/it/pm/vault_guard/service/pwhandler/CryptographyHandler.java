package ch.zhaw.it.pm.vault_guard.service.pwhandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * The CryptographyHandler class provides methods for encrypting and decrypting strings.
 * It uses a String as a keyword to encrypt and decrypt the strings.
 * This class is responsible for encrypting and decrypting strings.
 */
public class CryptographyHandler {

    private static final String ALGORITHM = "AES";
    String decryptedContent;

    /**
     * Creates a key for encryption and decryption.
     * The key is created using the SHA-256 algorithm.
     *
     * @param password The password used for encryption and decryption
     * @return The secret key used for encryption and decryption
     * @throws NoSuchAlgorithmException If the algorithm is not available
     */
    private static SecretKeySpec createKey(String password) throws NoSuchAlgorithmException {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, ALGORITHM);
    }


    /**
     * Decrypts the encrypted input using the password as a key.
     * Checks if String starts with the Username.
     *
     * @param encryptedInput The encrypted input
     * @param password       The password used for decryption
     * @return The decrypted content or an empty optional if the username is not correct
     * @throws NoSuchAlgorithmException If the algorithm is not available
     * @throws NoSuchPaddingException   If the padding is not available
     * @throws InvalidKeyException      If the key is invalid
     */
    public Optional<String> decrypt(String encryptedInput, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        try {
            SecretKeySpec secretKey = createKey(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedInput);
            byte[] decryptedData = cipher.doFinal(decodedData);
            decryptedContent = new String(decryptedData);

            return Optional.of(decryptedContent);


        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
    }

    /**
     * Encrypts the decrypted input using the password as a key.
     *
     * @param decryptedInput The decrypted input
     * @param password       The password used for encryption
     * @return The encrypted content
     * @throws NoSuchAlgorithmException  If the algorithm is not available
     * @throws NoSuchPaddingException    If the padding is not available
     * @throws IllegalBlockSizeException If the block size is invalid
     * @throws BadPaddingException       If the padding is invalid
     * @throws InvalidKeyException       If the key is invalid
     */
    public String encrypt(String decryptedInput, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecretKeySpec secretKey = createKey(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(decryptedInput.getBytes(StandardCharsets.UTF_8)));
    }
}
