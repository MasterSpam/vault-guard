package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;


/**
 * This class generates a random password based on the given parameters.
 * The password will contain at least one character from each selected category.
 * The password will not contain any characters from the forbiddenChars string.
 * The password will be shuffled before returning it.
 */
public class PasswordGenerator {

    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMERALS = "0123456789";
    private static final String SPECIAL_SYMBOLS = "!@#$%&*()_+-=[]|/?><";
    private static final int DEFAULT_MIN_PASSWORD_SIZE = 4;

    private static final Random random = new Random();

    /**
     * Generates a random password based on the given parameters.
     *
     * @param length                The length of the password.
     * @param includeNumerals       Whether to include numerals in the password.
     * @param includeUppercase      Whether to include uppercase characters in the password.
     * @param includeSpecialSymbols Whether to include special symbols in the password.
     * @param forbiddenChars        A string containing characters that should not be included in the password.
     * @return A randomly generated password.
     */
    public static String generatePassword(int length, boolean includeNumerals, boolean includeUppercase, boolean includeSpecialSymbols, String forbiddenChars) {

        if (length < DEFAULT_MIN_PASSWORD_SIZE) {
            throw new IllegalArgumentException("Password length must be at least 4 characters.");
        }

        StringBuilder characterSet = new StringBuilder(LOWERCASE_CHARS);
        List<String> mandatoryCharacters = new ArrayList<>();

        if (includeUppercase) {
            characterSet.append(UPPERCASE_CHARS);
            mandatoryCharacters.add(pickRandomChar(UPPERCASE_CHARS, forbiddenChars));
        }
        if (includeNumerals) {
            characterSet.append(NUMERALS);
            mandatoryCharacters.add(pickRandomChar(NUMERALS, forbiddenChars));
        }
        if (includeSpecialSymbols) {
            characterSet.append(SPECIAL_SYMBOLS);
            mandatoryCharacters.add(pickRandomChar(SPECIAL_SYMBOLS, forbiddenChars));
        }

        removeForbiddenCharacters(characterSet, forbiddenChars);
        if (characterSet.isEmpty()) {
            return "";
        }

        ensureMandatoryCharacters(mandatoryCharacters, includeUppercase, includeNumerals, includeSpecialSymbols, characterSet.toString());

        return buildPassword(characterSet.toString(), length, mandatoryCharacters);
    }

    /**
     * Removes forbidden characters from the character set.
     *
     * @param characterSet   The character set to remove forbidden characters from.
     * @param forbiddenChars A string containing characters that should not be included in the password.
     */
    private static void removeForbiddenCharacters(StringBuilder characterSet, String forbiddenChars) {
        if (forbiddenChars != null && !forbiddenChars.isEmpty()) {
            String regex = "[" + Pattern.quote(forbiddenChars) + "]";
            String filtered = characterSet.toString().replaceAll(regex, "");
            characterSet.setLength(0);
            characterSet.append(filtered);
        }
    }

    /**
     * Ensures that at least one character from each selected category is included in the password.
     *
     * @param mandatoryCharacters   A list of characters that must be included in the password.
     * @param includeUppercase      Whether to include uppercase characters in the password.
     * @param includeNumerals       Whether to include numerals in the password.
     * @param includeSpecialSymbols Whether to include special symbols in the password.
     * @param availableChars        The characters that are available for selection.
     */
    private static void ensureMandatoryCharacters(List<String> mandatoryCharacters, boolean includeUppercase, boolean includeNumerals, boolean includeSpecialSymbols, String availableChars) {
        if (includeUppercase && mandatoryCharacters.stream().noneMatch(UPPERCASE_CHARS::contains)) {
            mandatoryCharacters.add(pickRandomChar(UPPERCASE_CHARS, availableChars));
        }
        if (includeNumerals && mandatoryCharacters.stream().noneMatch(NUMERALS::contains)) {
            mandatoryCharacters.add(pickRandomChar(NUMERALS, availableChars));
        }
        if (includeSpecialSymbols && mandatoryCharacters.stream().noneMatch(SPECIAL_SYMBOLS::contains)) {
            mandatoryCharacters.add(pickRandomChar(SPECIAL_SYMBOLS, availableChars));
        }
    }

    /**
     * Picks a random character from the given character set that is not in the available characters.
     * If no such character is found, null is returned.
     *
     * @param chars          The character set to pick a random character from.
     * @param forbiddenChars The characters that are already in use.
     * @return A random character that is not in the available characters.
     */
    private static String pickRandomChar(String chars, String forbiddenChars) {
        List<Character> validChars = chars.chars()
                .mapToObj(c -> (char) c)
                .filter(c -> forbiddenChars.indexOf(c) == -1)
                .toList();
        return validChars.isEmpty() ? "" : String.valueOf(validChars.get(random.nextInt(validChars.size())));
    }

    /**
     * Builds the final password by adding random characters to the mandatory characters.
     * The password will be shuffled before returning it to ensure randomness.
     *
     * @param characterSet        The character set to pick random characters from.
     * @param length              The length of the password.
     * @param mandatoryCharacters A list of characters that must be included in the password.
     * @return The final password.
     */
    private static String buildPassword(String characterSet, int length, List<String> mandatoryCharacters) {
        Collections.shuffle(mandatoryCharacters, random);
        StringBuilder password = new StringBuilder();
        mandatoryCharacters.forEach(password::append);

        for (int i = mandatoryCharacters.size(); i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            password.append(characterSet.charAt(randomIndex));
        }

        List<Character> pwdChars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            pwdChars.add(c);
        }

        Collections.shuffle(pwdChars, random);
        StringBuilder finalPassword = new StringBuilder();
        pwdChars.forEach(finalPassword::append);

        return finalPassword.toString();
    }
}
