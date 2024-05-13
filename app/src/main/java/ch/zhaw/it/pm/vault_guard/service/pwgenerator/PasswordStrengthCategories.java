package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

/**
 * Enum representing the categories of password strength.
 * The categories are VERY_WEAK, WEAK, MODERATE, STRONG, and VERY_STRONG.
 */
public enum PasswordStrengthCategories {
    VERY_WEAK("#f80000"),
    WEAK("#FF3500"),
    MODERATE("#ff8000"),
    STRONG("#60b700"),
    VERY_STRONG("#00ad3f");

    private final String color;

    /**
     * Constructor for the PasswordStrengthCategories enum.
     *
     * @param color The color associated with the category.
     */
    PasswordStrengthCategories(String color) {
        this.color = color;
    }

    /**
     * Returns the color associated with the category.
     *
     * @return The color associated with the category.
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the password strength category based on the given points.
     * The points are calculated based on various factors such as length, presence of numbers,
     * uppercase letters, lowercase letters, special characters, and entropy.
     *
     * @param points The points calculated for the password.
     * @return The category of strength the password falls into.
     */
    public static PasswordStrengthCategories getStrengthByPoints(int points) {
        if (points <= 30) {
            return VERY_WEAK;
        } else if (points <= 60) {
            return WEAK;
        } else if (points < 90) {
            return MODERATE;
        } else if (points < 120) {
            return STRONG;
        } else {
            return VERY_STRONG;
        }
    }
}