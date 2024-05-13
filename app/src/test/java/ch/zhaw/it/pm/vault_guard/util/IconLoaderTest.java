package ch.zhaw.it.pm.vault_guard.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IconLoaderTest {
    IconLoader iconLoader;

    @BeforeEach
    void setUp() {
        iconLoader = new IconLoader();
    }

    @Test
    void cleanupURLTest() {
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/"));
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/?query=1"));
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/#fragment"));
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/?query=1#fragment"));
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/#fragment?query=1"));
        assertEquals("https://www.google.com/favicon.ico", iconLoader.cleanupURL("https://www.google.com/favicon.ico?query=1#fragment?query=1"));
        assertEquals("https://google.com/favicon.ico", iconLoader.cleanupURL("google.com/?query=1#fragment?query=1"));
        assertEquals("https://google.com/favicon.ico", iconLoader.cleanupURL("google.com"));
        assertEquals("https://www.baeldung.com/favicon.ico", iconLoader.cleanupURL("https://www.baeldung.com/java-domain-name-from-url"));
    }

    @Test
    void cleanupURLTestWithWrongInput() {
        assertEquals("", iconLoader.cleanupURL(null));
        assertEquals("", iconLoader.cleanupURL(""));
        assertEquals("https://somethingbutnotanurl/favicon.ico", iconLoader.cleanupURL("somethingbutnotanurl"));
        assertEquals("https://example.com/favicon.ico", iconLoader.cleanupURL("https://username:password@example.com/"));
        assertEquals("https://example.com/favicon.ico", iconLoader.cleanupURL("https://example.com:8080/"));
    }

    @Test
    void isValidURLTestPositive() {
        assertTrue(iconLoader.isValidURL("https://www.google.com/favicon.ico"));
        assertTrue(iconLoader.isValidURL("https://www.google.com/?query=1"));
        assertTrue(iconLoader.isValidURL("https://www.google.com/#fragment"));
        assertTrue(iconLoader.isValidURL("https://www.google.com/?query=1#fragment"));
        assertTrue(iconLoader.isValidURL("https://www.google.com/#fragment?query=1"));
        assertTrue(iconLoader.isValidURL("https://www.google.com/favicon.ico?query=1#fragment?query=1"));
        assertTrue(iconLoader.isValidURL("https://google.com/?query=1#fragment?query=1"));
        assertTrue(iconLoader.isValidURL("https://www.baeldung.com/favicon.ico"));
    }

    @Test
    void isValidURLTestNegative() {
        assertFalse(iconLoader.isValidURL(null));
        assertFalse(iconLoader.isValidURL(""));
        assertFalse(iconLoader.isValidURL("google.com"));
        assertFalse(iconLoader.isValidURL("https://somethingbutnotanurl/favicon.ico"));
    }
}
