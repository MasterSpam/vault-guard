package ch.zhaw.it.pm.vault_guard.service.pwgenerator;

import ch.zhaw.it.pm.vault_guard.util.ExecutorManager;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The TOTPGenerator class is responsible for generating a Time-based One-Time Password (TOTP).
 * It uses the GoogleAuthenticator library to generate the TOTP.
 * The class provides a method to start the TOTP generation and a method to stop the TOTP generation.
 * The class uses the PropertyChangeSupport to inform the listeners about the TOTP.
 */
public class TOTPGenerator {


    private ScheduledExecutorService scheduler;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Starts the TOTP generation in a separate thread.
     *
     * @param secretKey The secret key used to generate the TOTP.
     */
    public synchronized void runTOTPGenerator(String secretKey) {
        GoogleAuthenticator gAuth;

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            ExecutorManager.registerExecutor(scheduler);
        }

        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000)
                .setCodeDigits(6)
                .build();

        gAuth = new GoogleAuthenticator(config);
        Runnable totpTask = () -> {
            int totp = gAuth.getTotpPassword(secretKey);
            long timeRemaining = (30000 - (System.currentTimeMillis() % 30000)) / 1000;
            informListeners(totp + " (" + timeRemaining + " seconds)");
        };

        scheduler.scheduleAtFixedRate(totpTask, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops the TOTP generation.
     */
    private synchronized void stopGenerator() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Adds a PropertyChangeListener to the TOTPGenerator.
     *
     * @param listener The PropertyChangeListener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (support.getPropertyChangeListeners().length != 0) {
            support.removePropertyChangeListener(support.getPropertyChangeListeners()[0]);
            stopGenerator();
        }
        support.addPropertyChangeListener(listener);
    }

    /**
     * Informs the listeners about the TOTP.
     *
     * @param totp The TOTP to inform the listeners about.
     */
    private void informListeners(String totp) {
        support.firePropertyChange("TOTP", "", totp);
    }
}
