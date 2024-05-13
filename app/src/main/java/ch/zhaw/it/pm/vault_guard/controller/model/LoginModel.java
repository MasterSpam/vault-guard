package ch.zhaw.it.pm.vault_guard.controller.model;

import ch.zhaw.it.pm.vault_guard.service.pwhandler.CryptographyHandler;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.StorageService;
import ch.zhaw.it.pm.vault_guard.util.StorageException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static ch.zhaw.it.pm.vault_guard.controller.model.LoginState.*;

/**
 * The class LoginModel is responsible for the login approval and creation of new users.
 * It uses the CryptographyHandler to encrypt and decrypt the content and the StorageService to read and write the content.
 * The class uses the PropertyChangeSupport to inform the listeners about the login state.
 */
public class LoginModel {
    private final CryptographyHandler crypter;
    private final StorageService storageService;
    private String decryptedContent = "";
    private LoginState loginState = LoginState.LOGOUT;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Constructor of the class LoginModel used for testing
     *
     * @param crypter        CryptographyHandler
     * @param storageService StorageService
     */
    public LoginModel(CryptographyHandler crypter, StorageService storageService) {
        this.crypter = crypter;
        this.storageService = storageService;
    }

    /**
     * Constructor of the class LoginModel
     */
    public LoginModel() {
        this(new CryptographyHandler(), new StorageService());
    }

    /**
     * Method to log in the user
     * The method reads the file content with the account name as File name
     * it decrypts the content with the password
     * if the file was read and decrypted successfully the login state is set to SUCCESS
     * if not (when the file does not exist or the decryption process failed) the login state is set to FAIL
     * if an exception occurred while reading the file the login state is set to ERROR
     * At the end the listeners are informed about the login state
     *
     * @param accountName String
     * @param password    String
     */
    public void login(String accountName, String password) {
        LoginState tempState;
        try {
            Optional<String> rawFileContent = storageService.read(accountName);
            if (rawFileContent.isPresent()) {
                Optional<String> optionalDecryptedContent = crypter.decrypt(rawFileContent.get(), password);
                if (optionalDecryptedContent.isPresent()) {
                    decryptedContent = optionalDecryptedContent.get();
                    tempState = LOGIN;
                } else {
                    tempState = FAIL;
                }
            } else {
                tempState = FAIL;
            }
        } catch (StorageException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            tempState = ERROR;
        }
        informListeners(tempState);
    }

    /**
     * Method to get the decrypted content (used by the MainModel to get the decrypted content to show it in the view)
     *
     * @return String decryptedContent
     */
    public String getDecryptedContent() {
        return decryptedContent;
    }

    /**
     * Inform the listeners that the login state is set to "LOGOUT"
     */
    public void logout() {
        informListeners(LOGOUT);
    }

    /**
     * Method to create a new user
     * The method creates a new file with the account name as File name
     * The File content is a JSON String with the account name, the password and an empty list of Entry's
     * it encrypts the content with the password
     * if the file was created and encrypted successfully the login state is set to "LOGIN"
     * if not (when the file already exists) the login state is set to USERNAME_ERROR
     * if an exception occurred while creating the file or encrypting the content the login state is set to FAIL
     * At the end the listeners are informed about the login state
     *
     * @param accountName String
     * @param password    String
     */
    public void createNewUser(String accountName, String password) {
        LoginState tempState;
        try {
            if (Boolean.TRUE.equals(storageService.createFile(accountName))) {
                decryptedContent = "{\"accountName\":\"" + accountName + "\",\"accountPassword\":\"" + password + "\",\"Entries\":[]}";
                String encryptedContent = crypter.encrypt(decryptedContent, password);
                storageService.write(encryptedContent, accountName);
                tempState = LOGIN;
            } else {
                tempState = USERNAME_ERROR;
            }
        } catch (StorageException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeyException ignored) {
            tempState = FAIL;
        }
        informListeners(tempState);
    }

    /**
     * Method to add a PropertyChangeListener
     *
     * @param pcl PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Method to remove a PropertyChangeListener
     *
     * @param pcl PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Method to inform the listeners about the login state
     * it returns LOGIN when the user is logged in
     * it returns LOGOUT when the user is logged out (Default state)
     * it returns ERROR when an exception occurred while reading the file
     * it returns USERNAME_ERROR when the account name already exists
     * it returns FAIL when login data (account name and password) are incorrect or the file content could not be read or decrypted
     *
     * @param loginState LoginState
     */
    public void informListeners(LoginState loginState) {
        support.firePropertyChange("state", this.loginState, loginState);
        this.loginState = loginState;
    }

}
