package ch.zhaw.it.pm.vault_guard;

import ch.zhaw.it.pm.vault_guard.view.ViewGenerator;
import javafx.application.Application;

/**
 * App Class for the Vault Guard Application
 */
public class App {

    /**
     * Main method to start the application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(ViewGenerator.class, args);
    }
}
