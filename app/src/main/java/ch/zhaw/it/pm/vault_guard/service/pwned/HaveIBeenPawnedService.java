package ch.zhaw.it.pm.vault_guard.service.pwned;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import java.net.HttpURLConnection;

/**
 * This class checks if a password has been leaked in a data breach.
 * It uses the Have I Been Pwned API to check if the password has been leaked.
 * The API uses the SHA-1 hash of the password to check if it has been leaked.
 * The password is hashed using the SHA-1 algorithm and the first 5 characters of the hash are sent to the API.
 * The API returns a list of hashes that match the first 5 characters.
 * The class then checks if the full hash of the password is in the list.
 * If the full hash is in the list, the password has been leaked and the class returns the number of times it has been leaked.
 * If the full hash is not in the list, the password has not been leaked and the class returns 0.
 */
public class HaveIBeenPawnedService {

    private static final String HIBPURL = "https://api.pwnedpasswords.com/range/";
    private static final String HASH_ALGORITHM = "SHA-1";

    /**
     * Checks if the given password has been leaked in a data breach.
     * The password is hashed using the SHA-1 algorithm and the first 5 characters of the hash are sent to the Have I Been Pwned API.
     * The API returns a list of hashes that match the first 5 characters.
     * The method then checks if the full hash of the password is in the list.
     * If the full hash is in the list, the password has been leaked and the method returns the number of times it has been leaked.
     * If the full hash is not in the list, the password has not been leaked and the method returns 0.
     * If there is no internet connection available, the method returns 0.
     * If there is an exception during the process, a PasswordCheckException is thrown.
     *
     * @param password The password to check
     * @return The number of times the password has been leaked, or 0 if it has not been leaked or there is no internet connection
     * @throws PasswordCheckException If there is an exception during the process
     */
    public int checkPassword(String password) throws PasswordCheckException {
        if (!isInternetAvailable()) {
            return 0;
        }

        try {
            String sha1Password = sha1Hex(password);
            String shaPrefix = sha1Password.substring(0, 5);
            String shaSuffix = sha1Password.substring(5).toUpperCase();

            String response = checkPwnedApi(shaPrefix);
            return parseResponse(shaSuffix, response);

        } catch (NoSuchAlgorithmException | IOException | URISyntaxException e) {
            throw new PasswordCheckException("Failed to check password: " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether an Internet connection exists.
     *
     * @return true if a connection to api can be established, otherwise false.
     */
    private static boolean isInternetAvailable() {
        try {
            URI uri = new URI(HIBPURL + "00000");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setUseCaches(false);
            int responseCode = connection.getResponseCode();
            return (responseCode == 200);
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }

    /**
     * Hashes the given input using the SHA-1 algorithm.
     *
     * @param input The input to hash
     * @return The SHA-1 hash of the input
     */
    static String sha1Hex(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    /**
     * Prepares a connection to the Have I Been Pwned API with the given SHA-1 prefix.
     *
     * @param shaPrefix The SHA-1 prefix to send to the API
     * @return The prepared connection
     */
    private static HttpURLConnection prepareConnection(String shaPrefix) throws IOException, URISyntaxException {
        String urlString = HIBPURL + shaPrefix;
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    /**
     * Reads the response from the given connection.
     *
     * @param connection The connection to read the response from
     * @return The response from the connection
     */
    static String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append(System.lineSeparator());
            }
        } finally {
            connection.disconnect();
        }
        return response.toString();
    }

    /**
     * Checks the Have I Been Pwned API for the given SHA-1 prefix.
     *
     * @param shaPrefix The SHA-1 prefix to check
     * @return The response from the API
     */
    static String checkPwnedApi(String shaPrefix) throws IOException, URISyntaxException {
        HttpURLConnection connection = prepareConnection(shaPrefix);
        return readResponse(connection);
    }

    /**
     * Parses the response from the Have I Been Pwned API to find the number of times the given SHA-1 suffix has been leaked.
     *
     * @param shaSuffix The SHA-1 suffix to find in the response
     * @param response  The response from the API
     * @return The number of times the SHA-1 suffix has been leaked
     */
    private int parseResponse(String shaSuffix, String response) {
        String[] hashes = response.split(System.lineSeparator());
        for (String hash : hashes) {
            String[] parts = hash.split(":");
            if (parts[0].equals(shaSuffix)) {
                return Integer.parseInt(parts[1]);
            }
        }
        return 0;
    }

}



