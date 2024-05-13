package ch.zhaw.it.pm.vault_guard.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The Hashing class provides utility methods for hashing passwords.
 * It uses the SHA-1 algorithm to create a hashed version of a given password.
 * This class is stateless and all its methods are static.
 */
public class Hashing {

    /**
     * Prevent instantiation of the class.
     */
    private Hashing() {
        // Prevent instantiation
    }

    /**
     * Hashes the given text using the SHA-1 algorithm.
     *
     * @param text the text to be hashed
     * @return the hashed version of the text
     * @throws NoSuchAlgorithmException if the SHA-1 algorithm is not available in the environment
     */
    public static String hash(String text) throws NoSuchAlgorithmException {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] byteOfTextToHash = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] hashedByteArray = digest.digest(byteOfTextToHash);
        return bytesToHex(hashedByteArray);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes the byte array to be converted
     * @return the hexadecimal representation of the byte array
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
