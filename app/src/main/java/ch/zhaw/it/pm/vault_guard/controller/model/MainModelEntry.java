package ch.zhaw.it.pm.vault_guard.controller.model;

import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCategories;
import ch.zhaw.it.pm.vault_guard.util.IconLoader;

import java.io.File;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The MainModelEntry class represents a single entry in the main window list pane.
 * It contains all the information from an Entry which includes:
 * - The Title of the Entry (passwordTitle)
 * - The Username used in the Entry (Username)
 * - The Website where the password is used (website)
 * - A one time password (oneTimePassword)
 * - The password itself (password)
 * - A boolean if the entry is a favourite (isFavourite)
 * - A boolean if the password is compromised (isCompromised)
 * - The strength of the password (passwordStrengthCategories)
 * <p>
 * The password strength in MainModelEntry is initially set to "WEAK". This is a precaution until the password is analyzed and confirmed strong.
 * This approach promotes strong password usage and prevents misjudging weak passwords as strong. The password strength is updated according to its actual characteristics.
 */
public class MainModelEntry {
    String passwordTitel;
    String username;
    String website;
    String email;
    String oneTimePassword;
    String password;
    boolean isFavourite;
    boolean isCompromised;
    PasswordStrengthCategories passwordStrengthCategories = PasswordStrengthCategories.WEAK;
    private File icon;
    private final IconLoader iconLoader;

    /**
     * Constructor for the MainModelEntry class.
     *
     * @param passwordTitel              The Titel of the Entry
     * @param username                   The Username used in the Entry
     * @param website                    The Website where the password is used
     * @param oneTimePassword            A one time password
     * @param password                   The password itself
     * @param passwordStrengthCategories The strength of the password
     * @param isFavourite                A boolean if the entry is a favourite
     * @param isCompromised              A boolean if the password is compromised
     */
    public MainModelEntry(String passwordTitel, String username, String website, String email, String oneTimePassword, String password, String passwordStrengthCategories, boolean isFavourite, boolean isCompromised) {
        this.passwordTitel = passwordTitel;
        this.username = username;
        this.website = website;
        this.email = email;
        this.oneTimePassword = oneTimePassword;
        this.password = password;
        this.isFavourite = isFavourite;
        this.isCompromised = isCompromised;
        setPasswordStrengthCategories(passwordStrengthCategories);
        iconLoader = new IconLoader();
        readIconFromFile();
    }

    /**
     * Returns the passwordTitel (main titel of the entry)
     *
     * @return The passwordTitel
     */
    public String getPasswordTitel() {
        return passwordTitel;
    }

    /**
     * Sets the passwordTitel (main titel of the entry)
     *
     * @param passwordTitel The passwordTitel
     */
    public void setPasswordTitel(String passwordTitel) {
        this.passwordTitel = passwordTitel;
    }

    /**
     * Returns the Username used in the entry
     *
     * @return The Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the Username used in the entry
     *
     * @param username The Username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the Website where the password is used
     *
     * @return The Website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the Website where the password is used
     *
     * @param website The Website
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Sets the Email where the password is used
     *
     * @param email The Email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the Email where the password is used
     *
     * @return The Email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the one time password
     *
     * @return The one time password
     */
    public String getOneTimePassword() {
        return oneTimePassword;
    }

    /**
     * Sets the one time password
     *
     * @param oneTimePassword The one time password
     */
    public void setOneTimePassword(String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    /**
     * Returns the password itself
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password itself
     *
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a boolean if the entry is a favourite
     *
     * @return A boolean if the entry is a favourite
     */
    public boolean getFavourite() {
        return isFavourite;
    }

    /**
     * Sets a boolean if the entry is a favourite
     *
     * @param favourite A boolean if the entry is a favourite
     */
    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    /**
     * Returns a boolean if the password is compromised
     *
     * @return A boolean if the password is compromised
     */
    public boolean getCompromised() {
        return isCompromised;
    }

    /**
     * Sets a boolean if the password is compromised
     *
     * @param compromised A boolean if the password is compromised
     */
    public void setCompromised(boolean compromised) {
        isCompromised = compromised;
    }

    /**
     * Returns the strength of the password
     *
     * @return The strength of the password
     */
    public PasswordStrengthCategories getPasswordStrengthCategories() {
        return passwordStrengthCategories;
    }

    /**
     * Sets the strength of the password
     *
     * @param passwordStrengthCategories The strength of the password as a String (Default is WEAK)
     */
    public void setPasswordStrengthCategories(String passwordStrengthCategories) {
        switch (passwordStrengthCategories) {
            case "VERY_WEAK" -> this.passwordStrengthCategories = PasswordStrengthCategories.VERY_WEAK;
            case "MODERATE" -> this.passwordStrengthCategories = PasswordStrengthCategories.MODERATE;
            case "STRONG" -> this.passwordStrengthCategories = PasswordStrengthCategories.STRONG;
            case "VERY_STRONG" -> this.passwordStrengthCategories = PasswordStrengthCategories.VERY_STRONG;
            default -> this.passwordStrengthCategories = PasswordStrengthCategories.WEAK;
        }
    }

    /**
     * Returns the icon of the entry
     *
     * @return The icon of the entry
     */
    public Optional<File> getIcon() {
        return (icon == null) ? Optional.empty() : Optional.of(icon);
    }

    /**
     * Sets the icon of the entry
     *
     * @param icon The icon of the entry
     */
    public void setIcon(File icon) {
        this.icon = icon;
    }

    /**
     * Reads the icon from the file
     */
    public void readIconFromFile() {
        String cleanedUrl = iconLoader.cleanupURL(getWebsite());
        File iconFile = new File("src/main/resources/ch/zhaw/it/pm/vault_guard/images/icons/" + cleanedUrl.replace("https://", "").replace("http://", "").replace("/", "").replace("www.", "").replace("favicon.ico", "") + ".png");
        if (iconFile.exists()) {
            setIcon(iconFile);
        }
    }

    /**
     * This method returns a map with all elements that are not empty.
     *
     * @return Map with all elements that are not empty.
     */
    public Map<String, String> getNoneEmptyElements() {
        Map<String, String> noneEmptyElements = new LinkedHashMap<>();
        if (!username.isEmpty()) {
            noneEmptyElements.put("Username", username);
        }
        if (!website.isEmpty()) {
            noneEmptyElements.put("Website", website);
        }
        if (!email.isEmpty()) {
            noneEmptyElements.put("Email", email);
        }
        if (!password.isEmpty()) {
            noneEmptyElements.put("Password", password);
        }
        if (!oneTimePassword.isEmpty()) {
            noneEmptyElements.put("TOTP", oneTimePassword);
        }
        return noneEmptyElements;
    }

    /**
     * This method loads the icon from the website.
     */
    public void loadIcon() {
        iconLoader.loadIconFromWebsite(this);
    }
}
