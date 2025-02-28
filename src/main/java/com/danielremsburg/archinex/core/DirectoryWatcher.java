package com.danielremsburg.archinex.core;

import com.danielremsburg.archinex.storage.LocalStorage;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class DirectoryWatcher {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DirectoryWatcher.class);
    private final Path directoryToMonitor;
    private final LocalStorage localStorage;
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 1000;  // Delay between retries

    public DirectoryWatcher(Path directoryToMonitor, LocalStorage localStorage) {
        this.directoryToMonitor = directoryToMonitor;
        this.localStorage = localStorage;

        // Ensure the directory exists
        if (!Files.exists(directoryToMonitor) || !Files.isDirectory(directoryToMonitor)) {
            throw new IllegalArgumentException("Directory to monitor does not exist or is not a directory: " + directoryToMonitor);
        }
        logger.debug("Directory to monitor exists: {}", directoryToMonitor);
    }

    public void startWatching() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the directory to monitor for file creation events
            directoryToMonitor.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("Started monitoring directory: {}", directoryToMonitor);

            // Infinite loop to keep the watch service running
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take(); // Block until an event is received
                    logger.debug("WatchService received a key: {}", key);
                } catch (InterruptedException e) {
                    logger.error("Watcher interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();  // Restore interrupt status
                    break; // Exit the loop if interrupted
                }

                // Process events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();

                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                        logger.debug("File creation event detected: {}", filePath);
                        handleFileCreation(filePath);
                    } else {
                        logger.warn("Unknown event kind: {}", kind);
                    }
                }

                // Reset the key to continue receiving events
                boolean valid = key.reset();
                if (!valid) {
                    logger.error("WatchKey no longer valid, terminating monitoring.");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error with file system watcher: {}", e.getMessage(), e);
        }
    }

    private void handleFileCreation(Path filePath) {
        try {
            logger.debug("Starting to handle file creation: {}", filePath);

            // Check if the file is fully created before processing
            if (isFileReady(filePath)) {
                // Generate a UUID for the file to ensure unique identification
                UUID fileUUID = UUID.randomUUID();
                logger.debug("Generated UUID for file: {}", fileUUID);

                byte[] fileData = Files.readAllBytes(filePath);
                logger.debug("Read file data, size: {} bytes", fileData.length);

                // Store the file in the configured local storage
                localStorage.store(fileUUID, fileData, null);
                logger.info("File stored with UUID: {}", fileUUID);

                // Optionally delete the local file after storing
                Files.delete(filePath);
                logger.info("Local file deleted after storage: {}", filePath);
            } else {
                logger.warn("File not ready for processing: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Error handling file {}: {}", filePath.getFileName(), e.getMessage(), e);
        }
    }

    private boolean isFileReady(Path filePath) {
        try {
            logger.error("entering isFileReady(): {}", filePath);

            // Retry logic to wait for the file to be fully written
            int retries = 0;
            while (retries < MAX_RETRIES) {
                if (!Files.exists(filePath)) {
                    logger.debug("File no longer exists: {}", filePath);
                    return false;
                }

                long sizeBefore = Files.size(filePath);
                logger.debug("Initial size of file {}: {} bytes", filePath, sizeBefore);

                // Wait briefly and then check again
                Thread.sleep(RETRY_DELAY_MS);

                if (!Files.exists(filePath)) {
                    logger.debug("File no longer exists after waiting: {}", filePath);
                    return false;
                }

                long sizeAfter = Files.size(filePath);
                logger.debug("Size of file {} after waiting: {} bytes", filePath, sizeAfter);

                // If file size is the same after the wait, it's likely finished being written
                if (sizeBefore == sizeAfter) {
                    return true;
                }

                retries++;
                logger.debug("Retrying file readiness check, attempt {}/{}", retries, MAX_RETRIES);
            }

            logger.warn("File {} was not ready after {} retries", filePath, MAX_RETRIES);
            return false;
        } catch (IOException | InterruptedException e) {
            logger.error("Error checking if file is ready: {}", e.getMessage(), e);
            return false;
        }
    }
}
