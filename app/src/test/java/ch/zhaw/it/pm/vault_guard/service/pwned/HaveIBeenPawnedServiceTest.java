package ch.zhaw.it.pm.vault_guard.service.pwned;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HaveIBeenPawnedServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"password", "", " ", "\n", "This String is longer than 40 characters and should be hashed correctly!"})
    void testSHA1Hashing(String password) throws Exception {
        String sha1Password = HaveIBeenPawnedService.sha1Hex(password);
        assertEquals(40, sha1Password.length());
    }

    @Test
    void testCheckBadPassword() {
        HaveIBeenPawnedService service = new HaveIBeenPawnedService();
        int count = service.checkPassword("password");
        assert(count > 0);
    }


    @Test
    void testCheckStrongPassword() {
        HaveIBeenPawnedService service = new HaveIBeenPawnedService();
        int count = service.checkPassword("aBü&wuz29_!gt2vjv$izg765vf7t7z67t/T|g&6");
        assertEquals(0, count);
    }


}
