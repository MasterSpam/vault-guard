package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class PasswordStrengthCalculatorTest {

    private PasswordStrengthCalculator passwordValidator;
    private static final Log log = LogFactory.getLog(PasswordStrengthCalculatorTest.class);

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordStrengthCalculator();
    }

    @Test
    void testCalculateStrength() {
        assertEquals(PasswordStrengthCategories.VERY_WEAK, passwordValidator.calculateStrength("123"));
        assertEquals(PasswordStrengthCategories.VERY_WEAK, passwordValidator.calculateStrength("password"));
        assertEquals(PasswordStrengthCategories.VERY_WEAK, passwordValidator.calculateStrength("Password1"));
        assertEquals(PasswordStrengthCategories.VERY_WEAK, passwordValidator.calculateStrength("Password1!"));
        assertEquals(PasswordStrengthCategories.VERY_WEAK, passwordValidator.calculateStrength("Password1!VeryLong"));
        assertEquals(PasswordStrengthCategories.MODERATE, passwordValidator.calculateStrength("$iVQiq7H"));
        assertEquals(PasswordStrengthCategories.STRONG, passwordValidator.calculateStrength("$iVQiq7H*"));
        assertEquals(PasswordStrengthCategories.STRONG, passwordValidator.calculateStrength("VFDL9px5MhS"));
        assertEquals(PasswordStrengthCategories.VERY_STRONG, passwordValidator.calculateStrength("YrDrw!@GiASp@Bh%9UWL!ivxywG@n8N8s4$YRGfgjKVgrXcFe$7HWLA^YqAfxES9CcG92#8iV3Ljdie&2vb5%C!8XdQZkVhD*mChdK@FGWFaoUksR8xqipBERho8#2jQ"));
    }


    @Test
    @SuppressWarnings("unchecked")
    void testReadPasswordReferenceFiles() {
        try {
            Method method = PasswordStrengthCalculator.class.getDeclaredMethod("readPasswordReferenceFiles", String.class);
            method.setAccessible(true);

            List<String> englishWords = (List<String>) method.invoke(passwordValidator, "ch/zhaw/it/pm/vault_guard/dictionaries/english.txt");
            assertFalse(englishWords.isEmpty());

            List<String> commonPasswords = (List<String>) method.invoke(passwordValidator, "ch/zhaw/it/pm/vault_guard/dictionaries/known_passwords.txt");
            assertFalse(commonPasswords.isEmpty());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Error while testing readPasswordReferenceFiles", e);
        }
    }
}