package ch.zhaw.it.pm.vault_guard.controller;

import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordGenerator;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCalculator;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCategories;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

/**
 * The PwdGeneratorController class is responsible for generating passwords.
 * It provides methods for generating passwords with different lengths and characters.
 */
public class PwdGeneratorController {


    @FXML
    private Slider passwordLengthSlide;

    @FXML
    private CheckBox enableUppercaseLetters;

    @FXML
    private CheckBox enableNumerals;

    @FXML
    private CheckBox enableSpecialCharacters;

    @FXML
    private TextField enterIllegalCharacters;

    @FXML
    private Label generatedPassword;

    @FXML
    private Label passwordLengthLabel;

    @FXML
    private Label passwordStrength;

    @FXML
    public Button cancelPassword;

    @FXML
    public Button acceptPassword;

    private int currentValue;

    private final PasswordStrengthCalculator strengthCalculator = new PasswordStrengthCalculator();

    /**
     * Initialize the Password Generator
     * generates an initial password with the initial values
     */
    public void initialize() {
        setAction();
        this.currentValue = (int) passwordLengthSlide.getValue();
        updatePassword();
    }

    /**
     * Set the action for the password generator
     */
    private void setAction() {
        passwordLengthSlide.valueProperty().addListener((observable, oldValue, newValue) -> {
            int currentPasswordLength = (int) passwordLengthSlide.getValue();
            if (currentValue != currentPasswordLength) {
                updatePassword();
                currentValue = currentPasswordLength;
            }
        });

        enableUppercaseLetters.setOnAction(event -> updatePassword());

        enableNumerals.setOnAction(event -> updatePassword());

        enableSpecialCharacters.setOnAction(event -> updatePassword());

        enterIllegalCharacters.setOnKeyTyped(event -> updatePassword());
    }

    /**
     * Update the password based on the current settings
     */
    private void updatePassword() {
        passwordLengthLabel.setText(String.valueOf((int) passwordLengthSlide.getValue()));
        generateNewPassword();
        updatePasswordStrength();
    }

    /**
     * Updates the password strength label based on the current password
     * It sets the background color of the label to the color of the strength category
     */
    private void updatePasswordStrength() {
        String password = generatedPassword.getText();
        Task<PasswordStrengthCategories> strengthTask = new Task<>() {
            @Override
            protected PasswordStrengthCategories call() {
                return strengthCalculator.calculateStrength(password);
            }

            @Override
            protected void succeeded() {
                PasswordStrengthCategories strength = getValue();
                Platform.runLater(() -> {
                    passwordStrength.setText(strength.toString());
                    generatedPassword.setStyle("-fx-background-color: " + strength.getColor());
                });
            }
        };
        new Thread(strengthTask).start();
    }

    /**
     * Generates a new password based on the current settings
     */
    private void generateNewPassword() {
        int passwordLength = (int) passwordLengthSlide.getValue();
        boolean includeUppercase = enableUppercaseLetters.isSelected();
        boolean includeNumerals = enableNumerals.isSelected();
        boolean includeSpecialSymbols = enableSpecialCharacters.isSelected();
        String forbiddenChars = enterIllegalCharacters.getText();

        String password = PasswordGenerator.generatePassword(passwordLength, includeNumerals, includeUppercase, includeSpecialSymbols, forbiddenChars);
        generatedPassword.setText(password);
    }

    /**
     * Get the generated password
     *
     * @return the generated password
     */
    public String getGeneratedPassword() {
        return generatedPassword.getText();
    }
}