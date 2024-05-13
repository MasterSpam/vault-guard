package ch.zhaw.it.pm.vault_guard.testdata;

import ch.zhaw.it.pm.vault_guard.controller.model.MainModelEntry;

public class MainModelTestData {
    public static MainModelEntry getMainModelEntryTestData00() {
        return new MainModelEntry("AtestPasswordTitel Nr.0", "testUsername Nr.0",
                "testWebseite Nr.0", "testEmail_0@gmail.com", "testOneTimePassword Nr.0", "testPassword Nr.0",
                "WEAK", false, false);
    }

    public static MainModelEntry getMainModelEntryTestData02() {
        return new MainModelEntry("BtestPasswordTitel Nr.2", "testUsername Nr.2",
                "testWebseite Nr.2", "testEmail_1@gmail.com", "testOneTimePassword Nr.2", "testPassword Nr.2",
                "WEAK", false, false);
    }

    public static MainModelEntry getMainModelEntryTestData01() {
        return new MainModelEntry("CtestPasswordTitel Nr.1", "testUsername Nr.1",
                "testWebseite Nr.1", "testEmail_2@gmail.com", "testOneTimePassword Nr.1", "testPassword Nr.1",
                "WEAK", false, false);
    }
}
