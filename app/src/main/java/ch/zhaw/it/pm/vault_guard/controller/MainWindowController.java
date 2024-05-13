package ch.zhaw.it.pm.vault_guard.controller;

import ch.zhaw.it.pm.vault_guard.controller.model.LoginModel;
import ch.zhaw.it.pm.vault_guard.controller.model.MainModel;
import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.TOTPGenerator;
import ch.zhaw.it.pm.vault_guard.util.PasswordEntryCellFactory;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The MainWindowController class is used to control the MainWindow.fxml file.
 * It is used to control the main window of the application.
 */
public class MainWindowController implements PropertyChangeListener {

    @FXML
    private ListView<MainModelEntry> entryList;
    @FXML
    private Button viewVault;
    @FXML
    private AnchorPane passwordDetailDashboard;
    @FXML
    private Button viewFavorites;
    @FXML
    private Button viewLeakmonitor;
    @FXML
    private ImageView logo;
    @FXML
    private Button logout;
    @FXML
    private Button viewSettings;
    @FXML
    private Button addNewElement;
    @FXML
    private TextField searchEntry;

    private enum editMode {EDIT, VIEW}

    private editMode mode = editMode.VIEW;

    private MainModel mainModel;
    private TOTPGenerator totpGenerator;
    private PwdWindowEditController pwdWindowEditController;
    private LoginModel loginModel;
    private Stage settingsStage;

    private VaultViewState vaultViewState = VaultViewState.VAULT;

    private static final Log log = LogFactory.getLog(MainWindowController.class);

    /**
     * This method is used to initialize the main window controller.
     * sets the password detail dashboard to a preferred width of 0.
     * creates a new main model with the login model.
     * shows the vault.
     * adds a listener to the entry list.
     * adds an action to the logout button.
     * adds an action to the view vault button.
     *
     * @param loginModel The login model
     */
    public void initialize(LoginModel loginModel) {
        this.loginModel = loginModel;
        mainModel = new MainModel(loginModel);
        mainModel.addPropertyChangeListener(this);
        totpGenerator = new TOTPGenerator();
        entryList.setCellFactory(passwordEntryView -> new PasswordEntryCellFactory());

        showVault();
        setActiveButton(viewVault);
        addActionListeners();

        Image icon = new Image(Objects.requireNonNull(this.getClass().getResource("../images/login_logo.png")).toString());
        logo.setImage(icon);
    }

    /**
     * This method is used to add the action listeners for the main window controller.
     */
    private void addActionListeners() {
        entryList.addEventFilter(MouseEvent.MOUSE_PRESSED, this::preventChangesLossFilter);
        entryList.addEventFilter(KeyEvent.ANY, this::preventChangesLossFilter);
        entryList.getSelectionModel().selectedItemProperty().addListener(this::passwordEntrySelectListener);
        addSearchListener();
        logout.setOnAction(event -> initiateLogout());
        viewVault.setOnAction(event -> {
            showVault();
            setActiveButton(viewVault);
        });
        viewFavorites.setOnAction(event -> {
            showFavorites();
            setActiveButton(viewFavorites);
        });
        viewLeakmonitor.setOnAction(event -> {
            showCompromisedPasswords();
            setActiveButton(viewLeakmonitor);
        });
        addNewElement.setOnAction(this::handleNewEntry);
        viewSettings.setOnAction(event -> displaySettingsWindow());
    }

    /**
     * This method is used to initiate the logout.
     * If the settings window is still open it brings it to the front.
     * The setting window must be closed before the user can log out.
     * If not, it logs out the user, clears the entry list and the password detail dashboard.
     */
    private void initiateLogout() {
        if (settingsStage != null && settingsStage.isShowing()) {
            settingsStage.toFront();
        } else {
            logout();
            entryList.getItems().clear();
            passwordDetailDashboard.getChildren().clear();
            try {
                mainModel.saveData();
            } catch (IOException | MainModel.EncryptErrorException | StorageException e) {
                showErrorAlert("An error occurred while saving the data");
            }
        }
    }

    /**
     * This method is used to add a search listener.
     */
    private void addSearchListener() {
        searchEntry.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                switch (vaultViewState) {
                    case VAULT -> showVault();
                    case FAVORITES -> showFavorites();
                    case LEAKMONITOR -> showCompromisedPasswords();
                }
            } else {
                new Thread(() -> {
                    List<MainModelEntry> searchResult = mainModel.searchEntry(newValue, vaultViewState);
                    Platform.runLater(() -> refreshEntryList(searchResult, entryList.getSelectionModel().getSelectedItem()));
                }).start();
            }
        });
    }

    /**
     * This method is used to log out the user.
     */
    public void logout() {
        loginModel.logout();
    }


    /**
     * This method is used to handle the new entry.
     *
     * @param actionEvent The action event
     */
    private void handleNewEntry(ActionEvent actionEvent) {
        try {
            preventChangesLossFilter(actionEvent);
            if (!actionEvent.isConsumed()) {
                loadEditPasswordDetails(Optional.empty());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method manages the selection of the password entries.
     *
     * @param observable The observable value
     * @param oldValue   The old value
     * @param newValue   The new value
     */
    private void passwordEntrySelectListener(ObservableValue<? extends MainModelEntry> observable, MainModelEntry oldValue, MainModelEntry newValue) {
        if (newValue != null) {
            try {
                loadPasswordDetails(newValue);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method is used to consume the event if the user tries to access another password entry while the changes are not saved.
     * It shows a warning alert to the user, and if the user decides to cancel, the event is consumed, and the selection of the new value is prevented.
     * Otherwise, the event is not being consumed.
     *
     * @param event The event
     */
    private void preventChangesLossFilter(Event event) {
        if (mode == editMode.EDIT && pwdWindowEditController.isChanged()) {
            Alert alert = showEditWarningAlert();
            if (alert.getResult().getButtonData().isCancelButton()) {
                event.consume();
            } else {
                entryList.getSelectionModel().clearSelection();
            }
        }
    }

    /**
     * This method is used to show a warning alert to the user.
     *
     * @return The alert
     */
    private Alert showEditWarningAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("Unsaved changes");
        alert.setContentText("Do you really want to stop? If you stop now, your current changes will not be saved.");
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("No");
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Yes");
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(this.getClass().getResource("../images/alert.png")).toString()));
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        alert.setGraphic(imageView);
        alert.showAndWait();
        return alert;
    }

    /**
     * This method is used to load the password details.
     *
     * @param entry The password entry
     * @throws IOException If an error occurs
     */
    private void loadPasswordDetails(MainModelEntry entry) throws IOException {
        passwordDetailDashboard.getChildren().clear();
        if (entry != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/PasswordDashboard.fxml"));
            Pane newLoadedPane = fxmlLoader.load();
            passwordDetailDashboard.getChildren().add(newLoadedPane);
            PwdWindowController pwdWindowController = fxmlLoader.getController();
            pwdWindowController.setMainWindowController(this);
            pwdWindowController.setMainModel(mainModel);
            pwdWindowController.loadPasswordEntryDashboard(entry);
        }
        mode = editMode.VIEW;
    }

    /**
     * This method is used to load the edit password details.
     *
     * @param entry The password entry
     * @throws IOException If an error occurs
     */
    public void loadEditPasswordDetails(Optional<MainModelEntry> entry) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/PasswordDashboardEdit.fxml"));
        Pane newLoadedPane = fxmlLoader.load();
        passwordDetailDashboard.getChildren().clear();
        passwordDetailDashboard.getChildren().add(newLoadedPane);
        pwdWindowEditController = fxmlLoader.getController();
        pwdWindowEditController.cancelChangesEntry.setOnAction(event -> {
            MainModelEntry selectedEntry = entryList.getSelectionModel().getSelectedItem();
            preventChangesLossFilter(event);
            if (!event.isConsumed()) {
                try {
                    loadPasswordDetails(selectedEntry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                entryList.getSelectionModel().select(selectedEntry);
            }
        });
        pwdWindowEditController.generatePassword.setOnAction(event -> openPasswordGenerator());
        pwdWindowEditController.setMainModel(mainModel);
        if (entry.isPresent()) {
            pwdWindowEditController.loadPasswordEntryDashboard(entry.get());
        } else {
            entryList.getSelectionModel().clearSelection();
            pwdWindowEditController.loadEmptyPasswordEntryDashboard();
        }
        mode = editMode.EDIT;
    }

    /**
     * This method is used to open the password generator.
     */
    private void openPasswordGenerator() {
        pwdWindowEditController.handleSaveChanges();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/PasswordGenerator.fxml"));
            Pane newLoadedPane = fxmlLoader.load();
            passwordDetailDashboard.getChildren().clear();
            passwordDetailDashboard.getChildren().add(newLoadedPane);
            PwdGeneratorController passwordGeneratorController = fxmlLoader.getController();
            configureAbortPasswordAction(passwordGeneratorController);
            configureAcceptPasswordAction(passwordGeneratorController);
        } catch (IOException e) {
            showErrorAlert("An error occurred while opening the password generator");
        }
    }

    /**
     * This method is used to configure the abort password action.
     *
     * @param passwordGeneratorController The password generator controller
     */
    private void configureAbortPasswordAction(PwdGeneratorController passwordGeneratorController) {
        passwordGeneratorController.cancelPassword.setOnAction(event -> {
            try {
                loadEditPasswordDetails(Optional.of(entryList.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This method is used to configure the accept password action.
     *
     * @param passwordGeneratorController The password generator controller
     */
    private void configureAcceptPasswordAction(PwdGeneratorController passwordGeneratorController) {
        passwordGeneratorController.acceptPassword.setOnAction(event -> {
            String generatedPassword = passwordGeneratorController.getGeneratedPassword();
            try {
                loadEditPasswordDetails(Optional.of(entryList.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pwdWindowEditController.enterPassword.setText(generatedPassword);
        });
    }

    /**
     * This method is used to show only the entries that are marked as favorites.
     */
    private void showFavorites() {
        vaultViewState = VaultViewState.FAVORITES;
        entryList.getItems().clear();
        entryList.getItems().addAll(mainModel.getFavorites());
    }

    /**
     * This method is used to show only the entries with passwords that are marked as compromised.
     */
    private void showCompromisedPasswords() {
        vaultViewState = VaultViewState.LEAKMONITOR;
        entryList.getItems().clear();
        entryList.getItems().addAll(mainModel.getCompromised());
    }

    /**
     * This method is used to set the active button.
     *
     * @param activeButton The active button
     */
    private void setActiveButton(Button activeButton) {
        List<Button> buttons = Arrays.asList(viewVault, viewFavorites, viewLeakmonitor);
        for (Button button : buttons) {
            if (button == activeButton) {
                button.setStyle("-fx-background-color: #5B858C; -fx-cursor: 'hand';");
            } else {
                button.setStyle("-fx-background-color: #666666; -fx-cursor: 'hand';");
            }
        }
    }

    /**
     * This method is used to show the vault.
     */
    private void showVault() {
        vaultViewState = VaultViewState.VAULT;
        List<MainModelEntry> entryContentList = mainModel.getSortedEntryContentList();
        entryList.getItems().clear();
        entryList.getItems().addAll(entryContentList);
        entryList.setEditable(false);
    }

    /**
     * This method is used to view the settings in e separate Window.
     * If the settings window is already open, it will be brought to the front.
     * If the settings window is not open, it will be opened.
     * If an error occurs while opening the settings, an error alert will be shown.
     */
    private void displaySettingsWindow() {
        if (settingsStage == null || !settingsStage.isShowing()) {
            viewSettings.setStyle("-fx-background-color: #5B858C; -fx-cursor: 'hand';");
            try {
                FXMLLoader settingsFxmlLoader = new FXMLLoader(getClass().getResource("../FXML/SettingsDashboard.fxml"));
                Pane settingsDashboard = settingsFxmlLoader.load();
                SettingsController settingsController = settingsFxmlLoader.getController();
                Scene scene = new Scene(settingsDashboard);
                settingsStage = new Stage();
                settingsStage.setScene(scene);
                settingsStage.setResizable(false);
                settingsStage.setOnCloseRequest(event -> viewSettings.setStyle("-fx-background-color: #666666; -fx-cursor: 'hand';"));
                setUpSettings(settingsController, Optional.empty());
                settingsStage.show();
            } catch (IOException e) {
                log.info("An error occurred while opening the Settings");
                showErrorAlert("An error occurred while opening the Settings");
            }
        } else if (settingsStage.isShowing()) {
            settingsStage.toFront();
        } else {
            settingsStage.show();
        }
    }

    /**
     * This method is used to set up the settings.
     * It sets the main model and the main window controller.
     * It loads the data with the account password.
     * if the account password is not present, the settings will be loaded without an account password.
     * it the account password is present, it means, it was generated by the generator and therefore the settings will be loaded with the account password.
     *
     * @param settingsController The settings controller
     * @param accountPassword    The account password
     */
    public void setUpSettings(SettingsController settingsController, Optional<String> accountPassword) {
        settingsController.setMainModel(mainModel);
        settingsController.setMainWindowController(this);
        settingsController.loadData(accountPassword);
    }

    /**
     * This method is used to refresh the entry list.
     * It clears the entry list and adds all the entries from the entry content list.
     * It selects the selected entry and scrolls to it.
     * It is used to refresh the entry list after an entry was deleted.
     *
     * @param entryContentList The entry content list
     * @param selectedEntry    The selected entry
     */
    private void refreshEntryList(List<MainModelEntry> entryContentList, MainModelEntry selectedEntry) {
        entryList.getItems().clear();
        entryList.getItems().addAll(entryContentList);
        entryList.getSelectionModel().select(selectedEntry);
        entryList.scrollTo(selectedEntry);
    }

    /**
     * This method is used to get the TOTP generator.
     *
     * @return The TOTP generator
     */
    public TOTPGenerator getTotpGenerator() {
        return totpGenerator;
    }

    /**
     * This method is used to handle the delete entry.
     * It shows a confirmation alert to the user.
     * If the user decides to delete the entry, the entry will be deleted.
     * If an error occurs while deleting the entry, an error alert will be shown.
     */
    public void handleDeleteEntry() {
        MainModelEntry entry = entryList.getSelectionModel().getSelectedItem();
        if (entry != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Entry");
            alert.setHeaderText("Do you really want to delete the entry?");
            alert.setContentText("The entry will be deleted permanently.");
            Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setText("No");
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Yes");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    mainModel.deleteEntry(entry.getPasswordTitel());
                } catch (IOException | MainModel.EncryptErrorException | StorageException e) {
                    showErrorAlert("An error occurred while deleting the entry");
                }
            }
        }
    }

    /**
     * This method is used to show an error alert.
     *
     * @param headerText The content
     */
    private void showErrorAlert(String headerText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText("Please try again");
        alert.showAndWait();
    }

    /**
     * PropertyChangeListener implementation.
     * Called when the Model notifies about a change.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("deleteEntry")) {
            entryList.getSelectionModel().clearSelection();
            passwordDetailDashboard.getChildren().clear();
            showVault();
        } else if (evt.getPropertyName().equals("saveData")) {
            List<MainModelEntry> list = (List<MainModelEntry>) evt.getNewValue();
            if (!list.isEmpty()) {
                MainModelEntry entryToSelect;
                if (entryList.getSelectionModel().getSelectedItem() == null) {
                    entryToSelect = list.getLast();
                } else {
                    entryToSelect = entryList.getSelectionModel().getSelectedItem();
                }
                switch (vaultViewState) {
                    case VAULT -> refreshEntryList(mainModel.getSortedEntryContentList(), entryToSelect);
                    case FAVORITES -> refreshEntryList(mainModel.getFavorites(), entryToSelect);
                    case LEAKMONITOR -> refreshEntryList(mainModel.getCompromised(), entryToSelect);
                }
            }
        }
    }
}