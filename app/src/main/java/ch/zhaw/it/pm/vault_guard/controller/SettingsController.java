package ch.zhaw.it.pm.vault_guard.controller;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModel;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.StorageService;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * The SettingsController class is used to control the settings dashboard.
 * It is responsible for handling the user input and updating the view.
 */
public class SettingsController {
    @FXML
    private AnchorPane settingsDashboard;
    @FXML
    private TextField accountUsername;
    @FXML
    private TextField accountPassword;
    @FXML
    private Text deleteAccountWarning;
    @FXML
    private Button generatePasswordButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private Button confirmDeleteAccountButton;
    @FXML
    private Button editAccountButton;
    @FXML
    private Button cancelSettingsChangesButton;
    @FXML
    private Button saveChangesButton;

    private MainModel mainModel;
    private StorageService storageService;
    private MainWindowController mainWindowController;
    private static final String HIDDEN_PASSWORD = "••••••••";
    private static final Log log = LogFactory.getLog(SettingsController.class);

    /**
     * Initializes the controller.
     * Sets all the action events for the buttons.
     * Sets the AccountUsername and AccountPassword fields to not editable.
     * Sets the storage service.
     */
    public void initialize() {
        saveChangesButton.setOnAction(event -> saveChanges());
        cancelSettingsChangesButton.setOnAction(event -> closeSettings());
        deleteAccountButton.setOnAction(event -> deleteAccountWarning());
        confirmDeleteAccountButton.setOnAction(event -> deleteAccount());
        generatePasswordButton.setOnAction(event -> openPasswordGenerator());
        editAccountButton.setOnAction(event -> editAccount());
        confirmDeleteAccountButton.setVisible(false);
        deleteAccountWarning.setVisible(false);
        accountUsername.setEditable(false);
        accountPassword.setEditable(false);
        storageService = new StorageService();
    }

    /**
     * Sets the MainWindowController.
     *
     * @param mainModel The main model
     */
    public void setMainModel(MainModel mainModel) {
        this.mainModel = mainModel;
    }

    /**
     * Sets the MainWindowController.
     *
     * @param mainWindowController The main window controller
     */
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    /**
     * This method is used to load the data of the account.
     * If the generated password is empty, means there is no generated password yet, the password is set to "********" (Hide).
     * Otherwise, if the Settings are called by the Generator the generated password is set.
     * The account name is set to the AccountUsername field.
     *
     * @param generatedPassword The generated password (optionally)
     */
    public void loadData(Optional<String> generatedPassword) {
        if (generatedPassword.isEmpty()) {
            accountPassword.setText(HIDDEN_PASSWORD);
        } else {
            editAccount();
            accountPassword.setText(generatedPassword.get());
        }
        accountUsername.setText(mainModel.getAccountName());
    }

    /**
     * This method is used to show the "delete the account" warning and the confirm delete button.
     */
    private void deleteAccountWarning() {
        deleteAccountButton.setVisible(false);
        deleteAccountWarning.setVisible(true);
        confirmDeleteAccountButton.setVisible(true);
    }

    /**
     * This method is used to delete the account.
     * It logs out the user and deletes the account file.
     */
    private void deleteAccount() {
        mainWindowController.logout();
        try {
            storageService.deleteFile(mainModel.getAccountName());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        closeSettings();
    }

    /**
     * This method is used to edit the account.
     * it's used to make the AccountUsername and AccountPassword fields editable.
     */
    private void editAccount() {
        accountUsername.setEditable(true);
        accountPassword.setEditable(true);
        accountPassword.setText(mainModel.getAccountPassword());
        editAccountButton.setVisible(false);
        accountPassword.setStyle("-fx-border-color: #00AD3F; -fx-border-width: 3; -fx-background-color: #444444; -fx-text-fill: #FFFFFF");
        accountUsername.setStyle("-fx-border-color: #00AD3F; -fx-border-width: 3; -fx-background-color: #444444; -fx-text-fill: #FFFFFF");
    }

    /**
     * This method is used to save the changes made to the account.
     * If the account name is the same as the current account name, only the password is updated (doesn't matter if the password is the same or not).
     * If the account name is different, the account file is created with the new account name and the old account file is deleted.
     * If the account name already exists, an error alert is displayed.
     * If an error occurs while saving the changes, an error alert is displayed.
     */
    private void saveChanges() {
        try {
            if (mainModel.getAccountName().equals(accountUsername.getText())) {
                if (!HIDDEN_PASSWORD.equals(accountPassword.getText())) {
                    mainModel.setAccountPassword(accountPassword.getText());
                }
                mainModel.saveData();
                closeSettings();
            } else if (storageService.createFile(accountUsername.getText())) {
                storageService.deleteFile(mainModel.getAccountName());
                mainModel.setAccountName(accountUsername.getText());
                mainModel.setAccountPassword(accountPassword.getText());
                mainModel.saveData();
                closeSettings();
            } else {
                showErrorAlert("Account name already exists", "Please choose another account name");
            }
        } catch (Exception e) {
            log.error("Error while saving the changes: " + e.getMessage());
            showErrorAlert("An error occurred while saving the changes", "Please try again");
        }
    }

    /**
     * This method is used to close the settings.
     */
    private void closeSettings() {
        Stage stage = (Stage) settingsDashboard.getScene().getWindow();
        stage.fireEvent(
                new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)
        );
        stage.close();
    }

    /**
     * This method displays an error alert if an error occurs while saving the changes.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * This method is used to open the password generator.
     * It loads the password generator and sets the abort and accept password actions.
     * If an error occurs while opening the password generator, an error alert is displayed.
     */
    private void openPasswordGenerator() {
        editAccount();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/PasswordGenerator.fxml"));
            Pane newLoadedPane = fxmlLoader.load();
            settingsDashboard.getChildren().add(newLoadedPane);
            PwdGeneratorController passwordGeneratorController = fxmlLoader.getController();
            configureAbortPasswordAction(passwordGeneratorController);
            configureAcceptPasswordAction(passwordGeneratorController);
        } catch (IOException e) {
            showErrorAlert("An error occurred while opening the password generator", "Please try again");
        }
    }

    /**
     * This method is used to configure the abort password action.
     * since no password was generated at this point the parameter for the new settings dashboard is empty.
     *
     * @param passwordGeneratorController The password generator controller
     */
    private void configureAbortPasswordAction(PwdGeneratorController passwordGeneratorController) {
        passwordGeneratorController.cancelPassword.setOnAction(event -> {
            try {
                loadSettingsDashboard(Optional.empty());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This method is used to configure the accept password action.
     * The generated password is passed to the new settings dashboard.
     *
     * @param passwordGeneratorController The password generator controller
     */
    private void configureAcceptPasswordAction(PwdGeneratorController passwordGeneratorController) {
        passwordGeneratorController.acceptPassword.setOnAction(event -> {
            String generatedPassword = passwordGeneratorController.getGeneratedPassword();
            try {
                loadSettingsDashboard(Optional.of(generatedPassword));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This method is used to load the settings dashboard.
     * it loads the settings dashboard and sets the settings controller.
     * The new generated password is passed (also maybe empty if no password was generated) together with the settings controller to the mainWindowController.
     * If an error occurs while opening the password generator, an error alert is displayed.
     *
     * @param newGeneratedPassword The new generated password (optionally)
     */
    private void loadSettingsDashboard(Optional<String> newGeneratedPassword) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/SettingsDashboard.fxml"));
            Pane newLoadedPane = fxmlLoader.load();
            settingsDashboard.getChildren().add(newLoadedPane);
            SettingsController newSettingsController = fxmlLoader.getController();
            mainWindowController.setUpSettings(newSettingsController, newGeneratedPassword);
        } catch (IOException e) {
            showErrorAlert("An error occurred while opening the password generator", "Please try again");
        }
    }
}

