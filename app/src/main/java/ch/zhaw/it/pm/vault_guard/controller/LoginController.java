package ch.zhaw.it.pm.vault_guard.controller;


import ch.zhaw.it.pm.vault_guard.controller.model.LoginModel;
import ch.zhaw.it.pm.vault_guard.controller.model.LoginState;
import ch.zhaw.it.pm.vault_guard.view.ViewGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The LoginController class is responsible for the login window.
 * It handles the user input and calls the loginModel to check the login credentials.
 * It also listens to the loginModel and reacts to changes in the login state.
 * The LoginController is also responsible for creating a new vault.
 */
public class LoginController implements PropertyChangeListener {
    @FXML
    private Button loginButton;
    @FXML
    private Button newVault;
    @FXML
    private TextField inputUsername;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private PasswordField inputPasswordRepeat;
    @FXML
    private Text inputPasswordRepeatText;
    @FXML
    private Label userFeedback;
    @FXML
    private Button createNewVault;
    @FXML
    private Button cancelNewVault;

    private String accountName;
    private String accountPassword;
    private LoginModel loginModel;
    private Stage primaryStage;
    private boolean isCreatingNewVault = false;

    /**
     * Initializes the LoginController
     * - Sets the visibility of the input fields and buttons correctly
     * - Adds a listener to the loginModel
     * - Sets the actions for the buttons
     *
     * @param primaryStage The primary stage
     */
    public void initialize(Stage primaryStage) {
        setInitializeVisibility();
        this.loginModel = new LoginModel();
        loginModel.addPropertyChangeListener(this);

        addActionListeners();
        this.primaryStage = primaryStage;
    }


    /**
     * Adds action listeners to the buttons
     */
    private void addActionListeners() {
        loginButton.setOnAction(event -> logIntoMain(loginModel));
        createNewVault.setOnAction(event -> creatingNewVault());
        newVault.setOnAction(event -> switchLoginMode());
        cancelNewVault.setOnAction(event -> switchLoginMode());

        inputPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isCreatingNewVault) {
                    createNewVault.fire();
                } else {
                    loginButton.fire();
                }
            }
        });

        inputUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isCreatingNewVault) {
                    createNewVault.fire();
                } else {
                    loginButton.fire();
                }
            }
        });

        inputPasswordRepeat.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                createNewVault.fire();
            }
        });
    }

    /**
     * Sets the visibility of the input fields and buttons correctly
     */
    private void setInitializeVisibility() {
        setUserFeedback("");
        inputPasswordRepeat.setVisible(false);
        inputPasswordRepeatText.setVisible(false);
        createNewVault.setVisible(false);
        cancelNewVault.setVisible(false);
    }


    /**
     * This method gets called when a bound property is changed.
     * The following States are possible:
     * - LOGIN: Login successful -> Open Main Window
     * - LOGOUT: Logout successful -> Open Login Window
     * - FAIL: Login failed -> Show error message
     * - USERNAME_ERROR: Username error -> Show error message (only when creating a new account)
     * - ERROR: Error
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            LoginState state = (LoginState) evt.getNewValue();
            switch (state) {
                case LOGIN -> this.loadMain();
                case LOGOUT -> this.loadLogin();
                case FAIL -> this.setUserFeedback("Wrong Username or Password!");
                case USERNAME_ERROR -> this.setUserFeedback("Username already exists!");
                default -> this.setUserFeedback("Something went wrong!");
            }
        }
    }


    /**
     * Switches between the login and create new vault mode
     * - Sets the visibility of the input fields and buttons according to the mode
     * - Clears the input fields
     * - Sets the user feedback to an empty string
     */
    private void switchLoginMode() {
        isCreatingNewVault = !isCreatingNewVault;
        inputPasswordRepeatText.setVisible(!inputPasswordRepeatText.isVisible());
        inputPasswordRepeat.setVisible(!inputPasswordRepeat.isVisible());
        loginButton.setVisible(!loginButton.isVisible());
        createNewVault.setVisible(!createNewVault.isVisible());
        newVault.setVisible(!newVault.isVisible());
        cancelNewVault.setVisible(!cancelNewVault.isVisible());
        setUserFeedback("");
        inputUsername.clear();
        inputPassword.clear();
        inputPasswordRepeat.clear();
    }

    /**
     * Logs into the main window
     * - Gets the account name and password from the input fields
     * - Calls the login method of the loginModel
     *
     * @param loginModel The login model
     */
    private void logIntoMain(LoginModel loginModel) {
        accountName = inputUsername.getText();
        accountPassword = inputPassword.getText();
        loginModel.login(accountName, accountPassword);
    }

    /**
     * Creates a new vault
     * - Gets the account name and password from the input fields
     * - Checks if the account name and password are not empty
     * - Calls the createNewUser method of the loginModel
     * - Checks if the passwords match and sets the user feedback accordingly
     */
    private void creatingNewVault() {
        accountName = inputUsername.getText();
        accountPassword = inputPassword.getText();
        if (accountName.isEmpty()) {
            setUserFeedback("Please enter a username!");
            return;
        } else if (accountPassword.isEmpty()) {
            setUserFeedback("Please enter a password!");
            return;
        }
        String accountPasswordRepeat = inputPasswordRepeat.getText();
        if (accountPassword.equals(accountPasswordRepeat)) {
            this.loginModel = new LoginModel();
            loginModel.addPropertyChangeListener(this);
            loginModel.createNewUser(accountName, accountPassword);
        } else {
            setUserFeedback("Passwords do not match!");
        }
    }

    /**
     * Load the main window by calling the ViewGenerator class
     */
    private void loadMain() {
        ViewGenerator viewGenerator = new ViewGenerator();
        Stage stage = (Stage) loginButton.getScene().getWindow();
        viewGenerator.loginToMain(stage, loginModel);
    }

    /**
     * Load the login window by calling the ViewGenerator class
     */
    private void loadLogin() {
        ViewGenerator viewGenerator = new ViewGenerator();
        viewGenerator.openLoginWindow(primaryStage);
    }

    /**
     * Sets the user feedback
     *
     * @param feedback The feedback to be set
     */
    private void setUserFeedback(String feedback) {
        userFeedback.setText(feedback);
    }
}
