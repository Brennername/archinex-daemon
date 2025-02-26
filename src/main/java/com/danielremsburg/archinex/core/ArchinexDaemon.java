package com.danielremsburg.archinex.core;

import com.danielremsburg.archinex.config.ArchinexConfig;
import com.danielremsburg.archinex.storage.LocalStorage;
import com.danielremsburg.archinex.storage.Storage;
import com.danielremsburg.archinex.storage.S3CloudStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class ArchinexDaemon {

    private static final Logger logger = LoggerFactory.getLogger(ArchinexDaemon.class);

    private final Storage storage;
    private final Path directoryToMonitor;

    public ArchinexDaemon(ArchinexConfig config) {
        // Select storage type based on configuration
        String storageType = config.getString("storage.type");

        if ("local".equalsIgnoreCase(storageType)) {
            this.storage = new LocalStorage(config); // Initialize LocalStorage
            logger.info("Using local storage at: {}", config.getString("storage.local.directoryToMonitor"));
        } else if ("s3".equalsIgnoreCase(storageType)) {
            this.storage = new S3CloudStorage(config); // Initialize S3CloudStorage
            logger.info("Using S3 cloud storage.");
        } else {
            throw new IllegalArgumentException("Unknown storage type: " + storageType); // Handle invalid config
        }

        this.directoryToMonitor = Paths.get(config.getString("storage.local.directoryToMonitor"));
    }

    public void start() throws IOException, InterruptedException {
        logger.info("Archinex Daemon started.");

        // Initialize directory watcher
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directoryToMonitor.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take(); // Wait for an event
                } catch (InterruptedException e) {
                    logger.error("Watcher interrupted: {}", e.getMessage());
                    break;
                }

                // Process events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();

                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                        handleFileCreation(filePath);
                    }
                }

                // Reset the key
                boolean valid = key.reset();
                if (!valid) {
                    logger.error("Watch service key reset failed, terminating.");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error with file system watcher: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleFileCreation(Path filePath) {
        try {
            UUID fileUUID = UUID.randomUUID(); // Generate a new UUID for this file

            byte[] fileData = Files.readAllBytes(filePath);
            storage.store(fileUUID, fileData, null); // Use the generic storage interface to store the file

            logger.info("File {} stored with UUID: {}", filePath.getFileName(), fileUUID);

            // Optionally delete the local file after storing (if desired)
            if (storage instanceof LocalStorage) {
                Files.delete(filePath);
                logger.info("Local file {} deleted after storage.", filePath.getFileName());
            }

        } catch (IOException e) {
            logger.error("Error handling file {}: {}", filePath.getFileName(), e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try {
            // Load configuration
            ArchinexConfig config = new ArchinexConfig(); // Assuming constructor loads config from file or environment
            ArchinexDaemon daemon = new ArchinexDaemon(config);

            // Start the daemon
            daemon.start();
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting ArchinexDaemon: {}", e.getMessage(), e);
        }
    }
}
