package ch.zhaw.it.pm.vault_guard.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordTest {
    @Test
    void testPasswordConstructorPositive() {
        String originalPassword = "TestPassword";
        String expectedHashedPassword = "6250625b226df62870ae23af8d3fac0760d71588";

        Password password = new Password(originalPassword);

        assertEquals(originalPassword, password.getOriginalPassword());
        assertEquals(expectedHashedPassword, password.getHashedPassword());
    }

    @Test
    void testPasswordConstructorNegativeNull() {
        assertThrows(NullPointerException.class, () -> new Password(null));
    }

    @Test
    void testPasswordConstructorNegativeEmpty() {
        String originalPassword = "";
        String expectedHashedPassword = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        Password password = new Password(originalPassword);

        assertTrue(password.getOriginalPassword().isEmpty());
        assertEquals(expectedHashedPassword, password.getHashedPassword());
    }

}