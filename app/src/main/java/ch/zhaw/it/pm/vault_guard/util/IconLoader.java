package ch.zhaw.it.pm.vault_guard.util;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The IconLoader class is used to load the icon of a password entry. From the website or from the resources' folder.
 */
public class IconLoader {

    private static final String ICONS_PATH = "src/main/resources/ch/zhaw/it/pm/vault_guard/images/icons/";
    private static final Log log = LogFactory.getLog(IconLoader.class);

    /**
     * Loads the image of the password entry.
     *
     * @param entry The password entry
     */
    public void loadIconFromWebsite(MainModelEntry entry) {
        String website = cleanupURL(entry.getWebsite());
        if (!website.isEmpty() && isValidURL(website)) {
            String pathname = getIconPath(website);
            File f = new File(pathname);
            if (f.exists() && !f.isDirectory()) {
                entry.setIcon(f);
            } else if (isWebsiteReachable()) {
                Optional<BufferedImage> optionalIcon = loadIcon(website);
                if (optionalIcon.isPresent()) {
                    BufferedImage icon = optionalIcon.get();
                    storeIcon(icon, pathname);
                    entry.readIconFromFile();
                } else {
                    loadDefaultIcon(entry);
                }
            } else {
                loadDefaultIcon(entry);
            }
        } else {
            loadDefaultIcon(entry);
        }
    }

    /**
     * Checks if the website is reachable.
     *
     * @return True if the website is reachable, false otherwise
     */
    private boolean isWebsiteReachable() {
        try {
            URI uri = new URI("https://www.google.com");
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
     * Loads the default icon.
     *
     * @param entry The password entry
     */
    private void loadDefaultIcon(MainModelEntry entry) {
        entry.setIcon(new File(ICONS_PATH + "entry_default_icon.png"));
    }

    /**
     * Returns the pathname of the icon.
     *
     * @param website The website of the icon
     * @return The pathname of the icon
     */
    private String getIconPath(String website) {
        return ICONS_PATH + website.replace("https://", "").replace("http://", "").replace("/", "").replace("www.", "").replace("favicon.ico", "") + ".png";
    }

    /**
     * Stores the icon in the resources' folder.
     *
     * @param icon     The icon to be stored
     * @param pathname The pathname of the icon
     */
    private void storeIcon(BufferedImage icon, String pathname) {
        try {
            File file = new File(pathname);
            ImageIO.write(icon, "png", file);
        } catch (IOException e) {
            log.error("Error while storing the icon: " + e.getMessage());
        }
    }

    /**
     * Checks if the given URL is valid.
     *
     * @param url The URL to be checked
     * @return True if the URL is valid, false otherwise
     */
    boolean isValidURL(String url) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(url);
    }

    /**
     * Cleans up the URL.
     * If the URL does not start with "http://" or "https://", it adds "https://" to the URL.
     *
     * @param url The URL to be cleaned up
     * @return The cleaned up URL
     */
    public String cleanupURL(String url) {
        if (url != null && !url.isEmpty()) {
            url = replaceUmlaute(url);
            try {
                URI uri = new URI(url);
                if (uri.getScheme() == null) {
                    url = "https://" + url;
                    uri = new URI(url);
                }
                url = (uri.getHost() != null) ? uri.getHost() : url;
                url = "https://" + url + "/favicon.ico";
            } catch (URISyntaxException e) {
                log.error("Error while cleaning up the URL: " + e.getMessage());
            }
            return url;
        } else {
            return "";
        }
    }

    /**
     * Replaces the umlauts in the URL.
     *
     * @param url The URL to be cleaned up
     * @return The cleaned up URL
     */
    private String replaceUmlaute(String url) {
        Map<Character, String> replacements = new HashMap<>();
        replacements.put('ü', "ue");
        replacements.put('ö', "oe");
        replacements.put('ä', "ae");
        replacements.put('Ü', "Ue");
        replacements.put('Ö', "Oe");
        replacements.put('Ä', "Ae");
        replacements.put('ß', "ss");
        StringBuilder sb = new StringBuilder();
        for (char c : url.toCharArray()) {
            sb.append(replacements.getOrDefault(c, String.valueOf(c)));
        }
        return sb.toString();
    }


    /**
     * Loads the icon from the given URL.
     *
     * @param url The URL of the icon
     * @return The loaded icon
     */
    private Optional<BufferedImage> loadIcon(String url) {
        BufferedImage icon = null;
        try {
            URI uri = new URI(url);
            URL fullUrl = uri.toURL();
            icon = ICODecoder.read(fullUrl.openStream()).getFirst();
        } catch (Exception e) {
            log.error("Error while loading the icon: " + e.getMessage());
        }
        return Optional.ofNullable(icon);
    }
}
