package ch.zhaw.it.pm.vault_guard.controller;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModel;
import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCalculator;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCategories;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The PwdWindowEditController class is used to control the PasswordDashboardEdit.fxml file.
 * It is used to edit a password entry.
 */
public class PwdWindowEditController {

    @FXML
    private ImageView overviewEntryIcon;
    @FXML
    private TextField changeTitleInput;
    @FXML
    private ImageView warnIcon;
    @FXML
    private TextField enterUsername;
    @FXML
    public TextField enterPassword;
    @FXML
    private TextField enterEmail;
    @FXML
    private TextField enterWebsite;
    @FXML
    private TextField enterAuthKey;
    @FXML
    public Button generatePassword;
    @FXML
    public Button cancelChangesEntry;
    @FXML
    private Button saveChangesEntry;
    @FXML
    private Label passwordStrengthEditLabel;

    private MainModelEntry entry;
    private MainModel mainModel;

    private boolean isChanged;
    private PasswordStrengthCalculator passwordStrengthCalculator;
    private PasswordStrengthCategories passwordStrength;

    ChangeListener<String> listenerForTextChanges = ((observable, oldValue, newValue) -> isChanged = true);
    private static final String DEFAULT_ICON_PNG_PATH = "/ch/zhaw/it/pm/vault_guard/images/icons/entry_default_icon.png";
    private static final Log log = LogFactory.getLog(PwdWindowEditController.class);

    /**
     * This method is used to initialize the controller.
     */
    public void initialize() {
        passwordStrengthCalculator = new PasswordStrengthCalculator();
        addTextListener();
    }

    /**
     * This method is used to handle the changes of the password.
     *
     * @param password The new password
     */
    private void handlePasswordChanges(String password) {
        PasswordStrengthCategories oldPasswordStrength = passwordStrength;
        new Thread(() -> {
            passwordStrength = passwordStrengthCalculator.calculateStrength(password);
            Platform.runLater(() -> {
                if (oldPasswordStrength != passwordStrength) {
                    passwordStrengthEditLabel.setText(passwordStrength.toString());
                    passwordStrengthEditLabel.setStyle("-fx-background-color: " + passwordStrength.getColor() + ";");
                    enterPassword.setStyle("-fx-border-color: " + passwordStrength.getColor() + " ; -fx-border-width: 2; -fx-background-color: #444444; -fx-text-fill: #FFFFFF;");
                }
            });
        }).start();
    }

    /**
     * This method is used to set the main model.
     *
     * @param mainModel The main model
     */
    public void setMainModel(MainModel mainModel) {
        this.mainModel = mainModel;
    }

    /**
     * This method is used to load the password entry Edit Dashboard.
     * It loads the password entry and sets the values of the text fields.
     *
     * @param entry The password entry to be loaded
     */
    public void loadPasswordEntryDashboard(MainModelEntry entry) {
        this.entry = entry;
        changeTitleInput.setText(entry.getPasswordTitel());
        enterUsername.setText(entry.getUsername());
        enterPassword.setText(entry.getPassword());
        enterAuthKey.setText(entry.getOneTimePassword());
        enterEmail.setText(entry.getEmail());
        enterWebsite.setText(entry.getWebsite());
        warnIcon.setVisible(entry.getCompromised());
        if (entry.getIcon().isPresent()) {
            overviewEntryIcon.setImage(new Image(entry.getIcon().get().toURI().toString()));
        } else {
            overviewEntryIcon.setImage(new Image(DEFAULT_ICON_PNG_PATH));
        }
        changePasswordStrengthVisual(entry);
        isChanged = false;
    }

    /**
     * This method is used to change the password strength visual.
     *
     * @param entry The password entry
     */
    private void changePasswordStrengthVisual(MainModelEntry entry) {
        PasswordStrengthCategories strengthCategory = entry.getPasswordStrengthCategories();
        passwordStrengthEditLabel.setText(strengthCategory.toString());
        passwordStrengthEditLabel.setStyle("-fx-background-color: " + strengthCategory.getColor() + ";");
        enterPassword.setStyle("-fx-border-color: " + strengthCategory.getColor() + " ; -fx-border-width: 2; -fx-background-color: #444444; -fx-text-fill: #FFFFFF;");
    }

    /**
     * This method is used to load a new empty password entry edit dashboard.
     */
    public void loadEmptyPasswordEntryDashboard() {
        warnIcon.setVisible(false);
    }

    /**
     * This method is used to save the changes of the password entry.
     * It validates the title and creates a new entry if it does not exist.
     */
    public void handleSaveChanges() {
        validateTitle();

        if (entry == null) {
            createNewEntry();
        } else {
            updateExistingEntry();
        }

        saveChanges();
    }

    /**
     * This method is used validate the title.
     */
    private void validateTitle() {
        if (changeTitleInput.getText().isEmpty()) {
            showErrorAlert("Attention", "Title is empty", "Please enter a title");
            throw new IllegalArgumentException("Title is empty");
        }
    }

    /**
     * This method is used to create a new entry.
     */
    private void createNewEntry() {
        entry = new MainModelEntry(
                changeTitleInput.getText(),
                enterUsername.getText(),
                enterWebsite.getText(),
                enterEmail.getText(),
                "",
                enterPassword.getText(),
                "WEAK",
                false,
                false
        );
        mainModel.addEntry(entry);
    }

    /**
     * This method is used to update an existing entry.
     */
    private void updateExistingEntry() {
        entry.setPasswordTitel(changeTitleInput.getText());
        entry.setUsername(enterUsername.getText());
        entry.setPassword(enterPassword.getText());
        entry.setEmail(enterEmail.getText());
        if (passwordStrength != null) {
            entry.setPasswordStrengthCategories(passwordStrength.toString());
        }
        updateWebsiteAndIcon();
        entry.setOneTimePassword(enterAuthKey.getText());
        entry.setCompromised(false);
        mainModel.checkEntryIfCompromised(entry);
    }

    /**
     * This method is used to update the website and icon.
     */
    private void updateWebsiteAndIcon() {
        if (!entry.getWebsite().equals(enterWebsite.getText())) {
            entry.setWebsite(enterWebsite.getText());
            entry.loadIcon();
            updateIconInView();
        }
    }

    /**
     * This method is used to update the icon in the view.
     */
    private void updateIconInView() {
        if (entry.getIcon().isPresent()) {
            overviewEntryIcon.setImage(new Image(entry.getIcon().get().toURI().toString()));
        } else {
            overviewEntryIcon.setImage(new Image(DEFAULT_ICON_PNG_PATH));
        }
    }

    /**
     * This method is used to save the changes.
     */
    private void saveChanges() {
        try {
            mainModel.saveData();
        } catch (Exception e) {
            log.error("An error occurred while saving the changes", e);
            showErrorAlert("Error", "An error occurred while saving the changes", "Please try again");
        }
    }


    /**
     * This method displays an error alert if an error occurs while saving the changes.
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * This method is used to add a text listener to the text fields.
     */
    private void addTextListener() {
        generatePassword.visibleProperty().bind(changeTitleInput.textProperty().isEmpty().not());
        saveChangesEntry.setOnAction(event -> handleSaveChanges());

        enterPassword.textProperty().addListener((observable, oldValue, newValue) -> handlePasswordChanges(newValue));
        changeTitleInput.textProperty().addListener(listenerForTextChanges);
        enterUsername.textProperty().addListener(listenerForTextChanges);
        enterPassword.textProperty().addListener(listenerForTextChanges);
        enterEmail.textProperty().addListener(listenerForTextChanges);
        enterWebsite.textProperty().addListener(listenerForTextChanges);
        enterAuthKey.textProperty().addListener(listenerForTextChanges);
    }

    /**
     * This method is used to check if the password entry has been changed.
     *
     * @return True if the password entry has been changed, false otherwise
     */
    public boolean isChanged() {
        return isChanged;
    }
}
