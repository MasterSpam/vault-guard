package ch.zhaw.it.pm.vault_guard.util;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;

import javafx.scene.image.ImageView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * This class is used to create a custom cell factory for the password list view.
 * It is used to display the password entries in the list view.
 */
public class PasswordEntryCellFactory extends ListCell<MainModelEntry> {

    @FXML
    private GridPane passwordListElementPane;
    @FXML
    private Label entryTitle;
    @FXML
    private Label entryUsername;
    @FXML
    private ImageView entryIcon;

    private FXMLLoader mLLoader;
    private static final Log log = LogFactory.getLog(PasswordEntryCellFactory.class);

    @Override
    protected void updateItem(MainModelEntry entry, boolean empty) {
        super.updateItem(entry, empty);
        setStyle("-fx-background-color: #262626;");
        if (empty || entry == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("../FXML/PasswordListElement.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    log.error("Error loading PasswordListElement.fxml", e);
                }
            }
            setStyle("-fx-padding: 0px;");
            entryTitle.setText(entry.getPasswordTitel());
            entryUsername.setText(entry.getUsername());
            if (entry.getIcon().isPresent() && entry.getIcon().get().exists() && !entry.getIcon().get().isDirectory()) {
                entryIcon.setImage(new Image(entry.getIcon().get().toURI().toString()));
            } else {
                entryIcon.setImage(new Image("/ch/zhaw/it/pm/vault_guard/images/icons/entry_default_icon.png"));
            }
            setText(null);
            setGraphic(passwordListElementPane);

            if (getListView().getSelectionModel().getSelectedItems().contains(entry)) {
                passwordListElementPane.setStyle("-fx-background-color: #5B858C;  -fx-border-color: #FFFFFF; -fx-border-width: 0.5;");
            } else {
                passwordListElementPane.setStyle("-fx-background-color: #3D3D3D;  -fx-border-color: #FFFFFF; -fx-border-width: 0.2;");
            }
        }
    }
}
