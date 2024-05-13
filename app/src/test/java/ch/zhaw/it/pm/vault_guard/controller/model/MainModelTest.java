package ch.zhaw.it.pm.vault_guard.controller.model;

import ch.zhaw.it.pm.vault_guard.controller.VaultViewState;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.CryptographyHandler;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.StorageService;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.it.pm.vault_guard.testdata.MainModelTestData.getMainModelEntryTestData00;
import static ch.zhaw.it.pm.vault_guard.testdata.MainModelTestData.getMainModelEntryTestData01;
import static ch.zhaw.it.pm.vault_guard.testdata.MainModelTestData.getMainModelEntryTestData02;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;


/**
 * Test class for the MainModel
 * Tests the setupMainModel, addEntry, deleteEntry and saveData methods
 * The test data is set up in the setUpTestFileContent method
 * The mocking for the crypter, loginModel and storageService is set up in the setUpMocking method
 * The Methode setUpTestFileContent sets up the test data as it would be (similar) in the read File for all test methods
 * The setUpMocking method sets up the mocking for the crypter, loginModel and storageService
 * The setUpMainModel method initializes the model with the test data
 * The testSetupMainModel method tests the setupMainModel method and the sorting algorithm in the getSortedEntryContentList method
 * The testDeleteEntry method tests the deleteEntry method to delete an Entry from the model list
 * The testAddEntry method tests the addEntry method to add a new Entry to the model list
 * The testSaveData method verifies that the crypter.encrypt and storageService.writeFile is called with the correct parameters
 */
class MainModelTest {
    MainModel model;
    String testFileContent;
    String testAccountName = "testFileName";
    String testAccountPassword = "testPassword123.";
    CryptographyHandler crypter = mock(CryptographyHandler.class);
    LoginModel loginModel = mock(LoginModel.class);
    StorageService storageService = mock(StorageService.class);
    String startFileContent = "{\"accountName\":\"" + testAccountName + "\",\"accountPassword\":\"" + testAccountPassword + "\",\"Entries\":[]}";


    @BeforeEach
    void setUp() {
        setUpTestFileContent();
        try {
            setUpMocking();
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | StorageException e) {
            System.out.println("Error in setUpMocking");
            throw new RuntimeException(e);
        }
        model = new MainModel(crypter, storageService, loginModel);
        model.addPropertyChangeListener(evt -> {
        });
    }

    @Test
    void testSearchEntry() {
        model.addEntry(getMainModelEntryTestData00());
        model.addEntry(getMainModelEntryTestData01());
        model.addEntry(getMainModelEntryTestData02());

        List<MainModelEntry> results = model.searchEntry("testWebseite Nr.0", VaultViewState.VAULT);
        assertTrue(results.stream().anyMatch(entry -> entry.getWebsite().equals("testWebseite Nr.0")));
        assertTrue(results.stream().anyMatch(entry -> entry.getUsername().equals("testUsername Nr.0")));
        assertTrue(results.stream().anyMatch(entry -> entry.getPassword().equals("testPassword Nr.0")));
    }

    @Test
    void testSearchEntryNegative() {
        model.addEntry(getMainModelEntryTestData00());
        model.addEntry(getMainModelEntryTestData01());
        model.addEntry(getMainModelEntryTestData02());

        List<MainModelEntry> results = model.searchEntry("BlauhaarGorilla", VaultViewState.VAULT);
        assertFalse(results.stream().anyMatch(entry -> entry.getWebsite().equals("testWebseite Nr.0")));
        assertFalse(results.stream().anyMatch(entry -> entry.getUsername().equals("testUsername Nr.0")));
        assertFalse(results.stream().anyMatch(entry -> entry.getPassword().equals("testPassword Nr.0")));
    }


    @Test
    void sortedEntryContentListTest() {
        List<MainModelEntry> testManuallySortedList = new ArrayList<>();
        testManuallySortedList.add(getMainModelEntryTestData00());
        testManuallySortedList.add(getMainModelEntryTestData02());
        testManuallySortedList.add(getMainModelEntryTestData01());


        List<MainModelEntry> testAutomateSortedList = model.getSortedEntryContentList();

        for (int i = 0; i < testManuallySortedList.size(); i++) {
            assertEquals(testManuallySortedList.get(i).getPasswordTitel(), testAutomateSortedList.get(i).getPasswordTitel());
            assertEquals(testManuallySortedList.get(i).getUsername(), testAutomateSortedList.get(i).getUsername());
            assertEquals(testManuallySortedList.get(i).getWebsite(), testAutomateSortedList.get(i).getWebsite());
            assertEquals(testManuallySortedList.get(i).getOneTimePassword(), testAutomateSortedList.get(i).getOneTimePassword());
            assertEquals(testManuallySortedList.get(i).getPassword(), testAutomateSortedList.get(i).getPassword());
            assertEquals(testManuallySortedList.get(i).getPasswordStrengthCategories(), testAutomateSortedList.get(i).getPasswordStrengthCategories());
            assertEquals(testManuallySortedList.get(i).getFavourite(), testAutomateSortedList.get(i).getFavourite());
            assertEquals(testManuallySortedList.get(i).getCompromised(), testAutomateSortedList.get(i).getCompromised());
        }
    }

    @Test
    void testAddEntry() {

        List<MainModelEntry> testManuallySortedList = new ArrayList<>();
        testManuallySortedList.add(getMainModelEntryTestData00());
        testManuallySortedList.add(getMainModelEntryTestData02());
        testManuallySortedList.add(getMainModelEntryTestData01());

        List<MainModelEntry> testAutomateSortedList = model.getSortedEntryContentList();
        for (int i = 0; i < testManuallySortedList.size(); i++) {
            assertEquals(testManuallySortedList.get(i).getPasswordTitel(), testAutomateSortedList.get(i).getPasswordTitel());
            assertEquals(testManuallySortedList.get(i).getUsername(), testAutomateSortedList.get(i).getUsername());
            assertEquals(testManuallySortedList.get(i).getWebsite(), testAutomateSortedList.get(i).getWebsite());
            assertEquals(testManuallySortedList.get(i).getOneTimePassword(), testAutomateSortedList.get(i).getOneTimePassword());
            assertEquals(testManuallySortedList.get(i).getPassword(), testAutomateSortedList.get(i).getPassword());
            assertEquals(testManuallySortedList.get(i).getPasswordStrengthCategories(), testAutomateSortedList.get(i).getPasswordStrengthCategories());
            assertEquals(testManuallySortedList.get(i).getFavourite(), testAutomateSortedList.get(i).getFavourite());
            assertEquals(testManuallySortedList.get(i).getCompromised(), testAutomateSortedList.get(i).getCompromised());
        }
    }

    @Test
    void testDeleteEntry() throws IOException, MainModel.EncryptErrorException, StorageException {

        assertEquals(3, model.getSortedEntryContentList().size());

        model.deleteEntry("BtestPasswordTitel Nr.2");
        List<MainModelEntry> testManuallySortedList = new ArrayList<>();
        testManuallySortedList.add(getMainModelEntryTestData00());
        testManuallySortedList.add(getMainModelEntryTestData01());

        List<MainModelEntry> testAutomateSortedList = model.getSortedEntryContentList();
        for (int i = 0; i < testManuallySortedList.size(); i++) {
            assertEquals(testManuallySortedList.get(i).getPasswordTitel(), testAutomateSortedList.get(i).getPasswordTitel());
            assertEquals(testManuallySortedList.get(i).getUsername(), testAutomateSortedList.get(i).getUsername());
            assertEquals(testManuallySortedList.get(i).getWebsite(), testAutomateSortedList.get(i).getWebsite());
            assertEquals(testManuallySortedList.get(i).getOneTimePassword(), testAutomateSortedList.get(i).getOneTimePassword());
            assertEquals(testManuallySortedList.get(i).getPassword(), testAutomateSortedList.get(i).getPassword());
            assertEquals(testManuallySortedList.get(i).getPasswordStrengthCategories(), testAutomateSortedList.get(i).getPasswordStrengthCategories());
            assertEquals(testManuallySortedList.get(i).getFavourite(), testAutomateSortedList.get(i).getFavourite());
            assertEquals(testManuallySortedList.get(i).getCompromised(), testAutomateSortedList.get(i).getCompromised());
        }
    }

    @Test
    void testSaveData() throws StorageException {
        model.setAccountPassword(testAccountPassword);
        try {
            model.saveData();
            verify(crypter).encrypt(testFileContent, testAccountPassword);
        } catch (NoSuchAlgorithmException | IOException | MainModel.EncryptErrorException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | StorageException e) {
            System.err.println("Error in testSaveDate()");
            throw new RuntimeException(e);
        }
        verify(storageService).write(testFileContent, testAccountName);
    }

    /**
     * Set up the mocking for the crypter, loginModel and storageService
     * Mocking the crypter.encrypt method with return value same as parameter value testFileContent
     * Mocking the loginModel.getDecryptedContent method with return value testFileContent
     * Mocking the storageService.writeFile method with doNothing
     *
     * @throws NoSuchPaddingException    if the padding is not found
     * @throws IllegalBlockSizeException if the block size is illegal
     * @throws NoSuchAlgorithmException  if the algorithm is not found
     * @throws BadPaddingException       if the padding is bad
     * @throws InvalidKeyException       if the key is invalid
     * @throws StorageException          if the storage is not available
     */
    void setUpMocking() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, StorageException {

        when(crypter.encrypt(anyString(), eq(testAccountPassword))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return args[0];
        });
        when(this.loginModel.getDecryptedContent()).thenReturn(testFileContent);
        doNothing().when(storageService).write(testAccountName + startFileContent, testAccountName);
    }

    /**
     * Set up the test data as it would be (similar) in the read File for all test methods
     * The Test Content contains 3 Entries with the test data
     * Every Entry has a different first letter of the passwordTitel to test the sort alphabetically algorithms in the model.getSortedEntryContentList()
     */
    void setUpTestFileContent() {
        JSONObject jsonMainObject = new JSONObject(startFileContent);

        for (int i = 0; i < 3; i++) {
            char preChar = switch (i) {
                case 0 -> 'A';
                case 1 -> 'C';
                case 2 -> 'B';
                default -> 'Z';
            };
            JSONObject currentSavingObject = new JSONObject();
            currentSavingObject.put("passwordTitel", preChar + "testPasswordTitel Nr." + i);
            currentSavingObject.put("username", "testUsername Nr." + i);
            currentSavingObject.put("website", "testWebseite Nr." + i);
            currentSavingObject.put("email", "test_Email_" + i + "@gmail.com");
            currentSavingObject.put("oneTimePassword", "testOneTimePassword Nr." + i);
            currentSavingObject.put("password", "testPassword Nr." + i);
            currentSavingObject.put("passwordStrength", "WEAK");
            currentSavingObject.put("isFavourite", false);
            currentSavingObject.put("isCompromised", false);
            jsonMainObject.append("Entries", currentSavingObject);
        }
        testFileContent = jsonMainObject.toString();
    }
}
