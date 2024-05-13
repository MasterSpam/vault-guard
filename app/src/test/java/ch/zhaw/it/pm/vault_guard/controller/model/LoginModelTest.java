package ch.zhaw.it.pm.vault_guard.controller.model;

import ch.zhaw.it.pm.vault_guard.service.pwhandler.CryptographyHandler;
import ch.zhaw.it.pm.vault_guard.service.pwhandler.StorageService;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

/**
 * The class LoginModelTest tests the class LoginModel
 * It tests the login method and the createNewUser method
 * The class uses the Mock CryptographyHandler to encrypt and decrypt the content and the Mock StorageService to read and write the content and create a new File.
 * The Login method test the login process with a correct account name and password, with a wrong account name and with a wrong password
 * The createNewUserSuccess method test the creation of a new user
 * The createNewUserFail method test the creation of a new user with an already existing account name
 * The PropertyChangeEvent is tested with a valid login, a valid new user creation and an invalid login
 * The setUpMocking method set up the mocking for the crypter and storageService
 */
class LoginModelTest {
    LoginModel model;
    String testAccountName = "testFileName";
    String testAccountPassword = "testPassword123.";
    String testFileContent = "{\"accountName\":\"" + testAccountName + "\",\"accountPassword\":\"" + testAccountPassword + "\",\"Entries\":[]}";
    CryptographyHandler crypter = mock(CryptographyHandler.class);
    StorageService storageService = mock(StorageService.class);
    PropertyChangeListener Listener = mock(PropertyChangeListener.class);
    ArgumentCaptor<PropertyChangeEvent> argumentCaptor = ArgumentCaptor.forClass(PropertyChangeEvent.class);


    @BeforeEach
    public void setup() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, StorageException {
        model = new LoginModel(crypter, storageService);
        model.addPropertyChangeListener(Listener);
        setUpMocking();
    }

    @Test
    void testLogin() {
        model.login(testAccountName, testAccountPassword);
        assertEquals(testFileContent, model.getDecryptedContent());
    }

    @Test
    void testLoginFailedFileReading() {
        model.login("some String", testAccountPassword);
        assertEquals("", model.getDecryptedContent());
    }

    @Test
    void testLoginFailedDecrypting() {
        model.login(testAccountName, "some String");
        assertEquals("", model.getDecryptedContent());
    }

    @Test
    void testCreateNewUserSuccess() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, StorageException {
        model.createNewUser(testAccountName, testAccountPassword);
        verify(storageService, times(1)).createFile(testAccountName);
        verify(crypter, times(1)).encrypt(testFileContent, testAccountPassword);
        verify(storageService, times(1)).write(testAccountName, testAccountName);
        verify(Listener, times(1)).propertyChange(argumentCaptor.capture());
        PropertyChangeEvent event = argumentCaptor.getValue();
        assertEquals(LoginState.LOGIN, event.getNewValue());
    }

    @Test
    void testCreateNewUserFail() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, StorageException {
        Mockito.when(storageService.createFile(testAccountName)).thenReturn(false);

        model.createNewUser(testAccountName, testAccountPassword);
        verify(storageService, times(1)).createFile(testAccountName);
        verify(crypter, times(0)).encrypt(testAccountName, testAccountPassword);
        verify(storageService, times(0)).write(testAccountName, testAccountName);
        verify(Listener, times(1)).propertyChange(argumentCaptor.capture());
        PropertyChangeEvent event = argumentCaptor.getValue();
        assertEquals(LoginState.USERNAME_ERROR, event.getNewValue());
    }

    @Test
    void testPropertyChangeEventLoginValid() {
        model.login(testAccountName, testAccountPassword);
        verify(Listener, times(1)).propertyChange(argumentCaptor.capture());
        PropertyChangeEvent event = argumentCaptor.getValue();
        assertEquals(LoginState.LOGIN, event.getNewValue());
    }

    @Test
    void testPropertyChangeEventNewUserValid() {
        model.createNewUser(testAccountName, testAccountPassword);
        verify(Listener, times(1)).propertyChange(argumentCaptor.capture());
        PropertyChangeEvent event = argumentCaptor.getValue();
        assertEquals(LoginState.LOGIN, event.getNewValue());
    }

    @Test
    void testPropertyChangeEventInvalid() {
        model.login("Some String", "Some String");
        verify(Listener, times(1)).propertyChange(argumentCaptor.capture());
        PropertyChangeEvent event = argumentCaptor.getValue();
        assertEquals(LoginState.FAIL, event.getNewValue());
    }


    /**
     * Set up the mocking for the crypter, loginModel and storageService
     * Mocking the crypter.encrypt method with return value same as parameter value testFileContent
     * Mocking the loginModel.getDecryptedContent method with return value testFileContent
     * Mocking the storageService.writeFile method with doNothing
     */
    void setUpMocking() throws StorageException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Mockito.when(storageService.createFile(testAccountName)).thenReturn(true);
        doNothing().when(storageService).write(testFileContent, testAccountName);
        when(storageService.read(testAccountName)).thenReturn(Optional.ofNullable(testFileContent));
        when(crypter.decrypt(testFileContent, testAccountPassword)).thenReturn(Optional.of(testFileContent));
        when(crypter.encrypt(testFileContent, testAccountPassword)).thenReturn(testAccountName);
    }
}