package ch.zhaw.it.pm.vault_guard.controller.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import ch.zhaw.it.pm.vault_guard.controller.VaultViewState;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.CryptographyHandler;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.StorageService;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import ch.zhaw.it.pm.vault_guard.service.pwned.HaveIBeenPawnedService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;


/**
 * The MainModel class provides methods for managing the data of the main window.
 * It stores the decrypted content of the JSON file handle and sort the Entries and provides methods for adding, deleting, and saving entries.
 * It also provides methods for adding and removing PropertyChangeListeners.
 * The List holds all the entries objects that are displayed in the main window View
 */
public class MainModel {

    List<MainModelEntry> entryContentList = new ArrayList<>();
    private final CryptographyHandler crypter;
    private final StorageService storageService;
    private final LoginModel loginModel;
    private final HaveIBeenPawnedService haveIBeenPawnedService;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String accountPassword = "";
    private String accountName = "";

    /**
     * Constructor of the MainModel
     *
     * @param loginModel LoginModel object
     */
    public MainModel(LoginModel loginModel) {
        this.crypter = new CryptographyHandler();
        this.storageService = new StorageService();
        this.haveIBeenPawnedService = new HaveIBeenPawnedService();
        this.loginModel = loginModel;
        setupMainModel();
        checkAllEntriesIfCompromised();
    }

    /**
     * Constructor of the MainModel used for testing
     *
     * @param crypter        CryptographyHandler
     * @param storageService StorageService
     * @param loginModel     LoginModel
     */
    public MainModel(CryptographyHandler crypter, StorageService storageService, LoginModel loginModel) {
        this.crypter = crypter;
        this.storageService = storageService;
        this.haveIBeenPawnedService = new HaveIBeenPawnedService();
        this.loginModel = loginModel;
        setupMainModel();
    }


    /**
     * Method that sets up the MainModel (is called in the MainWindowController after the login was successful)
     * It gets the decrypted content from the loginModel and sorts the content in JSON file format
     */
    public void setupMainModel() {
        sortDecryptedContent(new JSONObject(loginModel.getDecryptedContent()));
    }


    /**
     * Method that returns the sorted entryContentList
     * It sorts the entryContentList alphabetically by the passwordTitel
     *
     * @return sorted entryContentList
     */
    public List<MainModelEntry> getSortedEntryContentList() {
        entryContentList.sort(Comparator.comparing(MainModelEntry::getPasswordTitel, String::compareToIgnoreCase));
        return entryContentList;
    }

    /**
     * This method performs a fuzzy search on the entries in the MainModel based on the provided search value.
     * It searches both the website and password title fields of the entries.
     * The method returns a list of MainModelEntry objects that match the search value in either of these fields.
     *
     * @param searchValue The value to be searched for in the website and password title fields of the entries.
     * @return A list of MainModelEntry objects that match the search value in either the website or password title field.
     */
    public List<MainModelEntry> searchEntry(String searchValue, VaultViewState viewState) {
        List<MainModelEntry> searchableEntries = switch (viewState) {
            case FAVORITES -> getFavorites();
            case LEAKMONITOR -> getCompromised();
            default -> getSortedEntryContentList();
        };

        List<ExtractedResult> mergedResults = new ArrayList<>();
        mergedResults.addAll(fuzzySearchResults(searchValue, searchableEntries, MainModelEntry::getWebsite));
        mergedResults.addAll(fuzzySearchResults(searchValue, searchableEntries, MainModelEntry::getPasswordTitel));

        mergedResults.removeIf(result -> result.getScore() < 85);

        return mergedResults.stream()
                .map(result -> searchableEntries.get(result.getIndex()))
                .distinct()
                .toList();
    }

    /**
     * Helper method to perform a fuzzy search on a specific field of the MainModelEntry objects.
     *
     * @param searchValue      The value to be searched for in the specified field.
     * @param mainModelEntries The list of MainModelEntry objects to be searched.
     * @param mapper           A function that takes a MainModelEntry object and returns the field to be searched.
     * @return A list of ExtractedResult objects that match the search value in the specified field.
     */
    private List<ExtractedResult> fuzzySearchResults(String searchValue, List<MainModelEntry> mainModelEntries, Function<MainModelEntry, String> mapper) {
        return FuzzySearch.extractAll(searchValue, mainModelEntries.stream().map(mapper).toList());
    }


    /**
     * Method that adds a new entry to the entryContentList
     * It creates a new MainModelEntry object and adds it to the entryContentList
     *
     * @param mainModelEntry MainModelEntry object
     */
    public void addEntry(MainModelEntry mainModelEntry) {
        entryContentList.add(mainModelEntry);
    }

    /**
     * Method that deletes an entry from the entryContentList
     * It iterates over the entryContentList and removes the entry with the given passwordTitel
     * It informs the listeners that the content has changed
     *
     * @param entryTitel String entryTitel
     */
    public void deleteEntry(String entryTitel) throws IOException, EncryptErrorException, StorageException {
        Iterator<MainModelEntry> iterator = entryContentList.iterator();
        while (iterator.hasNext()) {
            MainModelEntry entry = iterator.next();
            if (entry.getPasswordTitel().equals(entryTitel)) {
                iterator.remove();
                break;
            }
        }
        saveData();
        informListeners("deleteEntry", "", entryTitel);
    }

    /**
     * Method that saves the data to the file in Json format
     * It creates a JSONObject and a JSONArray and puts the accountName and accountPassword in the JSONObject
     * For each MainModelEntry in the entryContentList, it creates a JSONObject and puts it in the JSONArray
     * It encrypts the JSON file content with the accountPassword and writes the encrypted content to the file
     * It calls the writeFile method from the storageService to save the content to the file
     * The String that is written to the file contains the content and the test phrase (accountName) to later check if the decryption was successful
     *
     * @throws IOException           if an I/O error occurs
     * @throws EncryptErrorException if the file could not be encrypted
     */
    public void saveData() throws IOException, EncryptErrorException, StorageException {
        JSONObject jsonMainObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        jsonMainObject.put("accountName", accountName);
        jsonMainObject.put("accountPassword", accountPassword);

        for (MainModelEntry entryToBeSaved : entryContentList) {
            JSONObject currentSavingObject = new JSONObject();
            currentSavingObject.put("passwordTitel", entryToBeSaved.getPasswordTitel());
            currentSavingObject.put("username", entryToBeSaved.getUsername());
            currentSavingObject.put("website", entryToBeSaved.getWebsite());
            currentSavingObject.put("email", entryToBeSaved.getEmail());
            currentSavingObject.put("oneTimePassword", entryToBeSaved.getOneTimePassword());
            currentSavingObject.put("password", entryToBeSaved.getPassword());
            currentSavingObject.put("isFavourite", entryToBeSaved.getFavourite());
            currentSavingObject.put("isCompromised", entryToBeSaved.getCompromised());
            currentSavingObject.put("passwordStrength", entryToBeSaved.getPasswordStrengthCategories().toString());
            jsonArray.put(currentSavingObject);
        }
        jsonMainObject.put("Entries", jsonArray);
        String encryptedContent;
        try {
            encryptedContent = crypter.encrypt(jsonMainObject.toString(), accountPassword);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new EncryptErrorException("File could not be encrypted");
        }
        storageService.write(encryptedContent, accountName);
        informListeners("saveData", "", entryContentList);
    }

    /**
     * Method that sorts the decrypted content in JSON file format
     * It gets the accountName and accountPassword from the decryptedContent
     * For each entry in the JSON file, it creates a new MainModelEntry and adds it to the entryContentList
     *
     * @param decryptedContent JSONObject decryptedContent
     */
    private void sortDecryptedContent(JSONObject decryptedContent) {
        accountName = decryptedContent.getString("accountName");
        accountPassword = decryptedContent.getString("accountPassword");
        JSONArray tt = decryptedContent.getJSONArray("Entries");
        for (int i = 0; i < tt.length(); i++) {
            JSONObject currentEntryObject = tt.getJSONObject(i);
            entryContentList.add(new MainModelEntry
                    (
                            currentEntryObject.getString("passwordTitel"),
                            currentEntryObject.getString("username"),
                            currentEntryObject.getString("website"),
                            currentEntryObject.getString("email"),
                            currentEntryObject.getString("oneTimePassword"),
                            currentEntryObject.getString("password"),
                            currentEntryObject.getString("passwordStrength"),
                            currentEntryObject.getBoolean("isFavourite"),
                            currentEntryObject.getBoolean("isCompromised")
                    ));
        }
    }

    /**
     * Method that checks if the entry is compromised
     * It calls the HaveIBeenPwned service to check if the password is compromised
     * if the password is compromised, it sets the compromised flag in the MainModelEntry object
     *
     * @param entry MainModelEntry entry
     */
    public void checkEntryIfCompromised(MainModelEntry entry) {
        if (!entry.getPassword().isEmpty() && haveIBeenPawnedService.checkPassword(entry.getPassword()) > 0) {
            entry.setCompromised(true);
        }
    }

    /**
     * Method that checks all entries if they are compromised
     * It iterates over the entryContentList and calls the checkEntryIfCompromised method for each entry
     */
    public void checkAllEntriesIfCompromised() {
        for (MainModelEntry entry : entryContentList) {
            checkEntryIfCompromised(entry);
        }
    }

    /**
     * Method that sets the accountPassword
     *
     * @param newAccountPassword String newAccountName
     */
    public void setAccountPassword(String newAccountPassword) {
        this.accountPassword = newAccountPassword;
    }

    /**
     * Method that gets the accountPassword
     *
     * @return String accountPassword
     */
    public String getAccountPassword() {
        return accountPassword;
    }

    /**
     * Method that sets the accountName
     *
     * @param newAccountName String newAccountName
     */
    public void setAccountName(String newAccountName) {
        this.accountName = newAccountName;
    }

    /**
     * Method that gets the accountName
     *
     * @return String accountName
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Method that adds a PropertyChangeListener
     *
     * @param pcl PropertyChangeListener pcl
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Method that removes a PropertyChangeListener
     *
     * @param pcl PropertyChangeListener pcl
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Method that informs the listeners that the content in the MainModelEntry List entryContentList has changed
     */
    public void informListeners(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Static Exception class that is thrown when the file could not be encrypted
     */
    public static class EncryptErrorException extends Exception {
        /**
         * Constructor of the EncryptErrorException
         *
         * @param message String
         */
        public EncryptErrorException(String message) {
            super(message);
        }

        /**
         * Constructor of the EncryptErrorException
         *
         * @param message String
         * @param cause   Throwable
         */
        public EncryptErrorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Method that creates a favorites list of MainModelEntry objects
     *
     * @return the List of all entries that are marked as favorites
     */
    public List<MainModelEntry> getFavorites() {
        List<MainModelEntry> favorites = new ArrayList<>();
        for (MainModelEntry entry : entryContentList) {
            if (entry.getFavourite()) {
                favorites.add(entry);
            }
        }
        return favorites;
    }

    /**
     * Method that creates a list of compromised MainModelEntry objects
     *
     * @return the List of all entries that are compromised
     */
    public List<MainModelEntry> getCompromised() {
        List<MainModelEntry> compromised = new ArrayList<>();
        for (MainModelEntry entry : entryContentList) {
            if (entry.getCompromised()) {
                compromised.add(entry);
            }
        }
        return compromised;
    }

}
