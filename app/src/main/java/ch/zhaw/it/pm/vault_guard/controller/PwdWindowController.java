package ch.zhaw.it.pm.vault_guard.controller;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModel;
import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.TOTPGenerator;
import ch.zhaw.it.pm.vault_guard.service.pwgenerator.PasswordStrengthCategories;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * The PwdWindowController class is responsible for the password entry dashboard.
 * It provides methods for loading the password entry dashboard and handling the actions of the buttons.
 */
public class PwdWindowController implements PropertyChangeListener {


    @FXML
    private Label overviewEntryTitle;
    @FXML
    private ImageView overviewEntryIcon;
    @FXML
    private VBox passwordCredentials;
    @FXML
    private Text detailLabel;
    @FXML
    private Label detailContent;
    @FXML
    private Label passwordStrengthLabel;
    @FXML
    private Button favoriteTag;
    @FXML
    private ImageView warnIcon;
    @FXML
    private Button editPassword;
    @FXML
    private Button detailCopy;
    @FXML
    private Button passwordCopy;
    @FXML
    private Button viewPassword;
    @FXML
    private Label detailPassword;
    @FXML
    private Button deleteEntry;

    private boolean passwordVisible = false;

    private MainModelEntry passwordEntry;
    private MainWindowController mainWindowController;
    private MainModel mainModel;

    private static final String WARNICON_TOOLTIP = "Attention: Your Password is Compromised!" + System.lineSeparator() + "It is strongly recommended to change it.";
    private static final String IMAGE_PATH = "/ch/zhaw/it/pm/vault_guard/images/";

    /**
     * Initializes the controller.
     */
    public void initialize() {
        addActionListeners();
        Tooltip.install(warnIcon, new Tooltip(WARNICON_TOOLTIP));
    }

    /**
     * Sets the MainWindowController.
     *
     * @param mainWindowController The MainWindowController
     */
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    /**
     * Sets the MainModel.
     *
     * @param mainModel The MainModel
     */
    public void setMainModel(MainModel mainModel) {
        this.mainModel = mainModel;
    }

    /**
     * Adds the action listeners to the buttons.
     */
    private void addActionListeners() {
        favoriteTag.setOnAction(actionEvent -> togglePasswordEntryFavorite());
        editPassword.setOnAction(actionEvent -> handlePasswordEdit());
        deleteEntry.setOnAction(actionEvent -> mainWindowController.handleDeleteEntry());
    }

    /**
     * Loads the password entry dashboard.
     * It loads the password entry and sets the values of the text fields.
     * It sets the graphic of the favorite tag to a full or empty star.
     * It sets the visibility of the warning icon to visible or hidden.
     * It calls the loadDetailElements method to load the detail elements of the password entry.
     *
     * @param entry The password entry to be loaded
     * @throws RuntimeException If an I/O error occurs
     */
    public void loadPasswordEntryDashboard(MainModelEntry entry) {
        this.passwordEntry = entry;
        Optional<File> img = passwordEntry.getIcon();
        if (img.isPresent() && img.get().exists() && !img.get().isDirectory()) {
            overviewEntryIcon.setImage(new Image(img.get().toURI().toString()));
        } else {
            overviewEntryIcon.setImage(new Image(IMAGE_PATH + "icons/entry_default_icon.png"));
        }
        overviewEntryTitle.setText(passwordEntry.getPasswordTitel());
        SVGImage favImg;
        if (passwordEntry.getFavourite()) {
            favImg = SVGLoader.load(getClass().getResource("../images/star_filled.svg"));
        } else {
            favImg = SVGLoader.load(getClass().getResource("../images/star_outline.svg"));
        }
        favImg.toImage(26);
        favoriteTag.setGraphic(favImg);
        warnIcon.setVisible(passwordEntry.getCompromised());
        try {
            loadDetailElements();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the detail elements of the password entry.
     * It loads the elements of the password entry that are not an empty String.
     * It uses the FXMLLoader to load the FXML files.
     * It sets the text of the detail elements and the password strength label.
     * It sets the graphic of the copy buttons to an SVG image.
     * It sets the action of the copy buttons to copy the value to the system clipboard.
     * It sets the action of the view password button to switch the visibility of the password between visible and hidden.
     *
     * @throws IOException If an I/O error occurs
     */
    private void loadDetailElements() throws IOException {
        Map<String, String> detailElements = passwordEntry.getNoneEmptyElements();
        FXMLLoader fxmlLoader;
        Pane newLoadedPane;
        for (Map.Entry<String, String> entry : detailElements.entrySet()) {
            if (entry.getKey().equals("Password")) {
                fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/EntryDetailsPassword.fxml"));
                fxmlLoader.setController(this);
                newLoadedPane = fxmlLoader.load();
                passwordCredentials.getChildren().add(newLoadedPane);
                setPasswordStrengthLabel(passwordEntry.getPasswordStrengthCategories());
                passwordCopy.setOnAction(actionEvent -> handleCopyActions(entry.getValue()));
                setButtonGraphicSVG(passwordCopy, IMAGE_PATH + "copy-icon.svg", 20);
                viewPassword.setOnAction(actionEvent -> PwdWindowController.this.switchPasswordShowHide());
                setButtonGraphicSVG(viewPassword, IMAGE_PATH + "view-show-icon.svg", 30);
            } else {
                fxmlLoader = new FXMLLoader(getClass().getResource("../FXML/EntryDetailsDefault.fxml"));
                fxmlLoader.setController(this);
                newLoadedPane = fxmlLoader.load();
                passwordCredentials.getChildren().add(newLoadedPane);
                detailLabel.setText(entry.getKey());

                if (entry.getKey().equals("TOTP") && !entry.getValue().isEmpty()) {
                    TOTPGenerator totpGenerator = mainWindowController.getTotpGenerator();
                    totpGenerator.addPropertyChangeListener(this);
                    totpGenerator.runTOTPGenerator(entry.getValue());
                    detailContent.setText("Invalid Auth-Key!");
                    detailCopy.setOnAction(actionEvent -> handleCopyActions(detailContent.getText().substring(0, 6)));
                } else {
                    detailContent.setText(entry.getValue());
                    detailCopy.setOnAction(actionEvent -> handleCopyActions(entry.getValue()));
                }
                setButtonGraphicSVG(detailCopy, IMAGE_PATH + "copy-icon.svg", 20);
            }
        }
    }

    /**
     * Sets the password strength label to the given password strength.
     *
     * @param passwordStrengthCategories The password strength
     */
    private void setPasswordStrengthLabel(PasswordStrengthCategories passwordStrengthCategories) {
        passwordStrengthLabel.setText(passwordStrengthCategories.toString());
        passwordStrengthLabel.setStyle("-fx-background-color: " + passwordStrengthCategories.getColor() + ";");
    }

    /**
     * Handles the action when the Favorite Tag is clicked.
     * Toggles the favorite status of the password entry, by changing the graphic of the favorite tag.
     */
    private void togglePasswordEntryFavorite() {
        SVGImage favImg;
        if (passwordEntry.getFavourite()) {
            favImg = SVGLoader.load(getClass().getResource("../images/star_outline.svg"));
            passwordEntry.setFavourite(false);
        } else {
            favImg = SVGLoader.load(getClass().getResource("../images/star_filled.svg"));
            passwordEntry.setFavourite(true);
        }
        favImg.toImage(26);
        favoriteTag.setGraphic(favImg);
        try {
            mainModel.saveData();
        } catch (IOException | MainModel.EncryptErrorException | StorageException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while saving the data.");
            alert.setContentText("Please try again.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the action when the Copy button is clicked.
     * Copies the value to the system clipboard.
     *
     * @param value The value to be copied to the clipboard
     */
    private void handleCopyActions(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    /**
     * Switches the visibility of the password between visible and hidden.
     */
    private void switchPasswordShowHide() {
        if (passwordVisible) {
            setButtonGraphicSVG(viewPassword, IMAGE_PATH + "view-show-icon.svg", 30);
            detailPassword.setText("••••••••");
            detailPassword.setFont(javafx.scene.text.Font.font("Ayuthaya", 24.0));
            passwordVisible = false;
        } else {
            setButtonGraphicSVG(viewPassword, IMAGE_PATH + "view-hide-icon.svg", 30);
            detailPassword.setText(passwordEntry.getPassword());
            detailPassword.setFont(javafx.scene.text.Font.font("Ayuthaya", 16.0));
            passwordVisible = true;
        }
    }

    /**
     * Sets the graphic of a button to an SVG image.
     * The size of the image is set to the given size.
     * It uses the SVGLoader to load the SVG image.
     *
     * @param button The button to set the graphic
     * @param path   The path to the SVG image
     * @param size   The size of the image
     */
    private void setButtonGraphicSVG(Button button, String path, int size) {
        SVGImage img = SVGLoader.load(getClass().getResource(path));
        img.toImage(size);
        button.setGraphic(img);
    }

    /**
     * Handles the action when the Edit button is clicked.
     * Loads the Edit Password Details window.
     */
    private void handlePasswordEdit() {
        try {
            Optional<MainModelEntry> passwordEntryOptional = Optional.of(passwordEntry);
            mainWindowController.loadEditPasswordDetails(passwordEntryOptional);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("TOTP")) {
            Platform.runLater(() -> detailContent.setText(evt.getNewValue().toString()));
        }
    }
}
