package ch.zhaw.it.pm.vault_guard.service.pwhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CryptographyHandlerTest {
    private CryptographyHandler cryptographyHandler;
    private String testString;
    private String password;
    private String encryptedString;

    @BeforeEach
    void setUp() throws Exception {
        cryptographyHandler = new CryptographyHandler();
        testString = "Username\n Test$String \n Hello World!";
        password = "V€ry$tr0ngP@$$w0rd!";
        encryptedString = cryptographyHandler.encrypt(testString, password);
    }

    // Positive Test Cases
    @Test
    void testEncryptWithSameInputAndPassword() throws Exception {
        String result = cryptographyHandler.encrypt(testString, password);
        assertEquals(encryptedString, result);
    }

    @Test
    void testDecryptWithSameInputAndPassword() throws Exception {
        if (cryptographyHandler.decrypt(encryptedString, password).isPresent()) {
            String result = cryptographyHandler.decrypt(encryptedString, password).get();
            assertEquals(testString, result);
        }
    }

    // Negative Test Cases
    @Test
    void testDecryptWithInvalidString() {
        assertThrows(Exception.class, () -> cryptographyHandler.decrypt("InvalidString", password));
    }

    @Test
    void testEncryptDecryptLeadsToSameOutput() throws Exception {
        String encrypted = cryptographyHandler.encrypt(testString, password);
        if (cryptographyHandler.decrypt(encrypted, password).isPresent()) {
            String decrypted = cryptographyHandler.decrypt(encrypted, password).get();
            assertEquals(testString, decrypted);
        }
    }
}