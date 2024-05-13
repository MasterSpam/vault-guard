package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The PasswordStrength class is responsible for calculating the strength of a password.
 * It uses various criteria to determine the strength, including length, presence of numbers,
 * uppercase letters, lowercase letters, special characters, and entropy.
 * It also checks if the password is a common password or an English word.
 * The strength of the password is categorized into five categories: VERY_WEAK, WEAK, MODERATE, STRONG, VERY_STRONG.
 * The class uses the FuzzySearch library to perform a fuzzy search on the password against a list of English words and known passwords.
 * The class reads the English words and known passwords from files in the resources/dictionaries directory.
 */
public class PasswordStrengthCalculator {

    private static final Log log = LogFactory.getLog(PasswordStrengthCalculator.class);

    /**
     * Calculates the strength of a password.
     *
     * @param password The password to calculate the strength of.
     * @return The category of strength the password falls into.
     */
    public PasswordStrengthCategories calculateStrength(String password) {
        int points = getPointsForLength(password);
        points += getPointsForHavingNumbers(password);
        points += getPointsForUpperCaseLetters(password);
        points += getPointsForLowerCaseLetters(password);
        points += getPointsForLowerAndUpperCaseLettersCombined(password);
        points += getPointsForSpecialCharacters(password);
        points += getPointsForEntropy(password);
        points += applySimilarityChecks(password);
        return PasswordStrengthCategories.getStrengthByPoints(points);
    }

    /**
     * Gets points for the length of the password.
     *
     * @param password The password to check the length of.
     * @return The points awarded for the length of the password.
     */
    private int getPointsForLength(String password) {
        return password.length() * 8;
    }

    /**
     * Gets points for the presence of numbers in the password.
     *
     * @param password The password to check for numbers.
     * @return The points awarded for the presence of numbers in the password.
     */
    private int getPointsForHavingNumbers(String password) {
        return password.matches(".*\\d.*") ? 8 : 0;
    }

    /**
     * Gets points for the presence of uppercase letters in the password.
     *
     * @param password The password to check for uppercase letters.
     * @return The points awarded for the presence of uppercase letters in the password.
     */
    private int getPointsForUpperCaseLetters(String password) {
        return password.matches(".*[A-Z].*") ? 4 : 0;
    }

    /**
     * Gets points for the presence of lowercase letters in the password.
     *
     * @param password The password to check for lowercase letters.
     * @return The points awarded for the presence of lowercase letters in the password.
     */
    private int getPointsForLowerCaseLetters(String password) {
        return password.matches(".*[a-z].*") ? 4 : 0;
    }

    /**
     * Gets points for the presence of both lower and uppercase letters in the password.
     *
     * @param password The password to check for lower and uppercase letters.
     * @return The points awarded for the presence of both lower and uppercase letters in the password.
     */
    private int getPointsForLowerAndUpperCaseLettersCombined(String password) {
        return password.matches(".[a-z]+.") && password.matches(".[A-Z]+.") ? 8 : 0;
    }

    /**
     * Gets points for the presence of special characters in the password.
     *
     * @param password The password to check for special characters.
     * @return The points awarded for the presence of special characters in the password.
     */
    private int getPointsForSpecialCharacters(String password) {
        return password.matches(".[^a-zA-Z0-9 ]+.") ? 12 : 0;
    }

    /**
     * Gets points for the entropy of the password.
     *
     * @param password The password to calculate the entropy of.
     * @return The points awarded for the entropy of the password.
     */
    private int getPointsForEntropy(String password) {
        int characterSpace = calculateCharacterSet(password);
        double entropy = password.length() * (Math.log(characterSpace) / Math.log(2));
        return mapEntropyToPoints(entropy);
    }

    /**
     * Calculates the character set of the password.
     *
     * @param password The password to calculate the character set of.
     * @return The size of the character set of the password.
     */
    private int calculateCharacterSet(String password) {
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigits = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        int characterSetSize = 0;
        if (hasLower) characterSetSize += 26;
        if (hasUpper) characterSetSize += 26;
        if (hasDigits) characterSetSize += 10;
        if (hasSpecial) characterSetSize += 20;

        return characterSetSize;
    }

    /**
     * Maps the entropy of the password to points.
     *
     * @param entropy The entropy of the password.
     * @return The points awarded for the entropy of the password.
     */
    private int mapEntropyToPoints(double entropy) {
        if (entropy >= 100) return 10;
        if (entropy >= 80) return 8;
        if (entropy >= 60) return 6;
        if (entropy >= 40) return 4;
        if (entropy >= 20) return 2;
        return 0;
    }

    /**
     * Applies similarity checks to the password.
     *
     * @param password The password to apply similarity checks to.
     * @return The points deducted for the password being similar to common passwords or English words.
     */
    private int applySimilarityChecks(String password) {
        List<String> commonPassword = getCommonPasswords();
        List<String> englishVocabulary = getEnglishVocabulary();

        if (commonPassword.contains(password) || englishVocabulary.contains(password)) {
            return -100;
        }

        return applyFuzzySearchForEnglishWords(password, englishVocabulary) + applyFuzzySearchForEnglishWords(password, commonPassword);
    }

    /**
     * Gets the English vocabulary.
     *
     * @return A list of English words.
     */
    private List<String> getEnglishVocabulary() {
        return readPasswordReferenceFiles("ch/zhaw/it/pm/vault_guard/dictionaries/english.txt");
    }

    /**
     * Gets the common passwords.
     *
     * @return A list of common passwords.
     */
    private List<String> getCommonPasswords() {
        return readPasswordReferenceFiles("ch/zhaw/it/pm/vault_guard/dictionaries/known_passwords.txt");
    }

    /**
     * Applies a fuzzy search for English words to the password.
     *
     * @param password          The password to apply the fuzzy search to.
     * @param englishVocabulary The list of English words to compare the password to.
     * @return The points deducted for the password being similar to English words.
     */
    private int applyFuzzySearchForEnglishWords(String password, List<String> englishVocabulary) {
        int similarWordsCounter = 0;
        for (String englishWord : englishVocabulary) {
            if (FuzzySearch.partialRatio(password, englishWord) > 85) {
                similarWordsCounter += 2;
            }
            if (FuzzySearch.tokenSetRatio(password, englishWord) > 85) {
                similarWordsCounter += 8;
            }
        }
        return similarWordsCounter * (-2);
    }

    /**
     * Reads password reference files.
     *
     * @param pathToFile The path to the file to read.
     * @return A list of lines from the file.
     */
    private List<String> readPasswordReferenceFiles(String pathToFile) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL ressourceUrl = classLoader.getResource(pathToFile);
            assert ressourceUrl != null;
            Path path = Paths.get(ressourceUrl.toURI());
            return Files.readAllLines(path);
        } catch (Exception ex) {
            log.error("Error reading password reference files: " + ex.getMessage());
            return List.of();
        }
    }
}
