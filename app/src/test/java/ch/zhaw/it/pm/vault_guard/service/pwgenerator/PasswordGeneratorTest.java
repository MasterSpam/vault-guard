package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


class PasswordGeneratorTest {


    @RepeatedTest(10)
    void testGeneratePasswordLength() {
        String password = PasswordGenerator.generatePassword(8, true, true, true, "");
        assertEquals(8, password.length());
    }

    @Test
    void testGeneratePasswordLengthTooShort() {
        assertThrows(IllegalArgumentException.class, () -> PasswordGenerator.generatePassword(3, true, true, true, ""));
    }

    @Test
    void testVeryLongPassword() {
        assertDoesNotThrow(() -> {
            String password = PasswordGenerator.generatePassword(100, true, true, true, "");
            assertEquals(100, password.length());
        });
    }

    @RepeatedTest(100)
    void testIncludeNumerals() {
        String password = PasswordGenerator.generatePassword(10, true, false, false, "");
        assertTrue(password.matches(".*[0-9].*"));
    }

    @RepeatedTest(100)
    void testIncludeUppercase() {
        String password = PasswordGenerator.generatePassword(10, false, true, false, "");
        assertTrue(password.matches(".*[A-Z].*"));
    }

    @RepeatedTest(100)
    void testIncludeSpecialSymbols() {
        String password = PasswordGenerator.generatePassword(10, false, false, true, "");
        assertTrue(password.matches(".*[!@#$%&*()_+\\-=\\[\\]|/?><].*"));
    }

    @RepeatedTest(100)
    void testForbiddenCharactersExcluded() {
        String forbiddenChars = "V0rb1Dd3n_Ch4r@Cter5*!";
        String password = PasswordGenerator.generatePassword(10, true, true, true, forbiddenChars);
        assertFalse(password.matches(".*[" + forbiddenChars + "].*"), "Password contains forbidden characters: " + password);
    }

    /**
     * This test is the only one, that COULD potentially fail.
     * The numbers are chosen in a way, that the probability of a collision is very low.
     * But it is still possible.
     * The exact probability is 1 - (1 - 1/62^20)^1000 which is very close to 0.
     * To be precise: 0.0000000000000000000000000000000014
     * So the test fail every 10^30 times.
     */
    @Test
    void testRandomnessOverMultipleGenerations() {
        Set<String> passwords = new HashSet<>();
        int sampleSize = 1000;
        for (int i = 0; i < sampleSize; i++) {
            String password = PasswordGenerator.generatePassword(20, true, true, true, "");
            passwords.add(password);
        }
        assertEquals(sampleSize, passwords.size(), "Expected all generated passwords to be unique");
    }

    @Test
    void testEmptyCharacterSet() {
        assertEquals("", PasswordGenerator.generatePassword(10, false, false, false, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&*()_+-=[]|/?><"));
    }
}
