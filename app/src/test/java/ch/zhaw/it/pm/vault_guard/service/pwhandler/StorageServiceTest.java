package ch.zhaw.it.pm.vault_guard.service.pwhandler;

import ch.zhaw.it.pm.vault_guard.util.Hashing;
import ch.zhaw.it.pm.vault_guard.util.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;


class StorageServiceTest {

    private StorageService storageService;
    private final String testUser = "testUser";

    @BeforeEach
    public void setUp() {
        storageService = new StorageService();
    }

    @AfterEach
    void tearDown() throws NoSuchAlgorithmException, IOException {
        String filename = Hashing.hash(testUser);
        String PATH_TO_STORAGE = "/src/main/resources/";
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + PATH_TO_STORAGE + filename));
    }

    @Test
    void testWriteWithValidInput() {
        assertDoesNotThrow(() -> storageService.write(testUser, "testContent"));
    }

    @Test
    void testWriteWithInvalidInput() {
        assertThrows(NullPointerException.class, () -> storageService.write(null, "testContent"));
        assertThrows(IllegalArgumentException.class, () -> storageService.write(testUser, null));
    }

    @Test
    void testReadWithExistingFile() throws StorageException {
        String accountUser = testUser;
        String fileContent = "testContent";
        storageService.write(fileContent, accountUser);

        Optional<String> readContent = storageService.read(accountUser);

        assertTrue(readContent.isPresent());
        assertEquals(fileContent, readContent.get());
    }

    @Test
    void testReadWithNonExistingFile() throws StorageException {
        Optional<String> readContent = storageService.read("nonExistingUser");
        assertFalse(readContent.isPresent());
    }

    @Test
    void testReadWithInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> storageService.read(null));
    }
}