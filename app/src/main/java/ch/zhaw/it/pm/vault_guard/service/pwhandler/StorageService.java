package ch.zhaw.it.pm.vault_guard.service.pwhandler;

import ch.zhaw.it.pm.vault_guard.util.Hashing;
import ch.zhaw.it.pm.vault_guard.util.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * The StorageService class provides methods for storing and retrieving data.
 * It uses the Hashing utility to hash the account username, which is then used as the filename.
 * The data is stored in the resources directory of the project.
 */
public class StorageService {

    private static final String PATH_TO_STORAGE = "/src/main/resources/";
    private static final String USER_DIR = "user.dir";

    /**
     * Hashes the account username and writes the provided content to a file with the hashed name.
     * If a file with the same name already exists, it is deleted and a new file is created.
     *
     * @param accountUser the account username to be hashed and used as the filename
     * @param fileContent the content to be written to the file
     * @throws StorageException if there is a failure in hashing the account username or in creating directories
     */
    public void write(String fileContent, String accountUser) throws StorageException {
        try {
            String filename = Hashing.hash(accountUser);
            writeFile(fileContent, filename);
        } catch (NoSuchAlgorithmException e) {
            throw new StorageException("Failed to hash account user name", e);
        } catch (StorageException e) {
            throw new StorageException("Failed to create directories", e);
        }
    }

    /**
     * Writes a string to the file.
     *
     * @param filename    the hashed account username to be used as the filename
     * @param fileContent the content to be written to the file
     * @throws StorageException if there is a failure in writing to the file
     */
    private void writeFile(String fileContent, String filename) throws StorageException {
        try {
            Path path = Paths.get(System.getProperty(USER_DIR) + PATH_TO_STORAGE + filename);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.writeString(path, fileContent);
        } catch (IOException e) {
            throw new StorageException("Failed to write file", e);
        }
    }

    /**
     * Hashes the account username and reads the content of the corresponding file.
     * If the file does not exist, it returns an empty Optional.
     *
     * @param accountUser the account username to be hashed and used as the filename
     * @return an Optional containing a String that has the content of the file if found, or an empty Optional if not found
     * @throws StorageException if there is a failure in hashing the account username or in creating directories
     */
    public Optional<String> read(String accountUser) throws StorageException {
        try {
            String filename = Hashing.hash(accountUser);
            return readFile(filename);
        } catch (NoSuchAlgorithmException e) {
            throw new StorageException("Failed to hash account user name", e);
        } catch (StorageException e) {
            throw new StorageException("Failed to create directories", e);
        }
    }

    /**
     * Reads the content of the file.
     *
     * @param filename the hashed account username to be used as the filename
     * @return an Optional containing a String with the content of the file if found, or an empty Optional if not found
     * @throws StorageException if there is a failure in reading the file
     */
    private Optional<String> readFile(String filename) throws StorageException {
        try {
            Path path = Paths.get(System.getProperty(USER_DIR) + PATH_TO_STORAGE + filename);
            if (!Files.exists(path)) {
                return Optional.empty();
            }
            String content = Files.readString(path);
            return Optional.of(content);
        } catch (IOException e) {
            throw new StorageException("Failed to read file", e);
        }
    }

    /**
     * Hashes the account username and creates a file with the hashed name.
     * If a file with the same name already exists, it returns false. Otherwise, it creates the file and returns true.
     *
     * @param accountName the account username to be hashed and used as the filename
     * @return true if the file was created successfully, false if the file already exists
     * @throws StorageException if there is a failure in hashing the account username
     */
    public Boolean createFile(String accountName) throws StorageException {
        try {
            Path path = Paths.get(System.getProperty(USER_DIR) + PATH_TO_STORAGE + Hashing.hash(accountName));
            if (!Files.exists(path)) {
                Files.createFile(path);
                return true;
            }
            return false;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new StorageException("Failed to create file", e);
        }
    }

    /**
     * Hashes the account username and delete a file if it exists.
     *
     * @param accountName the account username to be hashed and used as the filename
     * @throws StorageException if there is a failure in hashing the account username
     */
    public void deleteFile(String accountName) throws StorageException {
        try {
            Path path = Paths.get(System.getProperty(USER_DIR) + PATH_TO_STORAGE + Hashing.hash(accountName));
            Files.deleteIfExists(path);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new StorageException("Failed to delete file", e);
        }
    }
}


