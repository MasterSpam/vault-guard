package ch.zhaw.it.pm.vault_guard.view;

import ch.zhaw.it.pm.vault_guard.controller.LoginController;
import ch.zhaw.it.pm.vault_guard.controller.MainWindowController;
import ch.zhaw.it.pm.vault_guard.controller.model.LoginModel;
import ch.zhaw.it.pm.vault_guard.util.ExecutorManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * This class is used to generate the view of the application.
 * It is used to open the login window and the main window.
 */
public class ViewGenerator extends Application {


    /**
     * This method is used to open the login window.
     * It replaces the scene of the stage with the login window.
     */
    @Override
    public void start(Stage primaryStage) throws RuntimeException {
        primaryStage.setOnCloseRequest(event -> ExecutorManager.shutdownAll());
        openLoginWindow(primaryStage);
    }

    /**
     * This method is used to open the login window.
     * It replaces the scene of the stage with the login window.
     *
     * @param stage the stage to be used for the login window
     */
    public void openLoginWindow(Stage stage) throws RuntimeException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ch/zhaw/it/pm/vault_guard/FXML/LoginWindow.fxml"));
        Pane rootNode;
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LoginController loginController = loader.getController();
        loginController.initialize(stage);

        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add("/ch/zhaw/it/pm/vault_guard/css/style.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * This method is used to open the main window.
     * It replaces the scene of the stage with the main window.
     */
    public void loginToMain(Stage stage, LoginModel loginModel) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ch/zhaw/it/pm/vault_guard/FXML/MainWindow.fxml"));
        Pane rootNode;
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MainWindowController mainWindowController = loader.getController();
        mainWindowController.initialize(loginModel);

        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add("/ch/zhaw/it/pm/vault_guard/css/style.css");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinHeight(500);
        stage.setMinWidth(900);
        stage.show();
    }
}
