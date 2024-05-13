package ch.zhaw.it.pm.vault_guard.util;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class HashingTest {

    @Test
    void testHashPassword() {
        String text = "password";
        String expectedHash = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
        try {
            String actualHash = Hashing.hash(text);
            assertEquals(expectedHash, actualHash);
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void testHashPasswordWithNull() {
        assertThrows(IllegalArgumentException.class, () -> Hashing.hash(null));
    }

    @Test
    void testHashPasswordWithDifferentInputs() {
        String text1 = "password1";
        String text2 = "password2";
        try {
            String hash1 = Hashing.hash(text1);
            String hash2 = Hashing.hash(text2);
            assertNotEquals(hash1, hash2);
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

    @Test
    void testHashPasswordWithSameInput() {
        String text = "password";
        try {
            String hash1 = Hashing.hash(text);
            String hash2 = Hashing.hash(text);
            assertEquals(hash1, hash2);
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown");
        }
    }

}