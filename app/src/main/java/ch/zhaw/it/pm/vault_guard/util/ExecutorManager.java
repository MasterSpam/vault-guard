package ch.zhaw.it.pm.vault_guard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The ExecutorManager class is responsible for managing the ExecutorServices.
 * It provides a method to register an ExecutorService and a method to shut down all registered ExecutorServices.
 */
public class ExecutorManager {
    private static final List<ExecutorService> executors = new ArrayList<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private ExecutorManager() {
        // Prevent instantiation
    }

    /**
     * Registers an ExecutorService.
     *
     * @param executor The ExecutorService to register.
     */
    public static synchronized void registerExecutor(ExecutorService executor) {
        executors.add(executor);
    }

    /**
     * Shuts down all registered ExecutorServices.
     */
    public static synchronized void shutdownAll() {
        for (ExecutorService executor : executors) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

