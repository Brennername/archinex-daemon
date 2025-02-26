package com.danielremsburg.archinex.storage;

import com.danielremsburg.archinex.config.ArchinexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; // Import for copy option
import java.util.UUID;

public class LocalStorage implements StorageSystem {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorage.class);

    private final Path storageRoot;
    private final Path archiveRoot; // Path for archived files

    public LocalStorage(ArchinexConfig config) {
        String storagePath = config.getLocalStoragePath();
        if (storagePath == null || storagePath.isEmpty()) {
            throw new IllegalArgumentException("Local storage path is not configured.");
        }
        storageRoot = Paths.get(storagePath);

        String archivePath = config.getStringOrDefault("storage.local.archivePath", storagePath + "/archive"); // Get archive path from config, default to storagePath/archive
        archiveRoot = Paths.get(archivePath);


        try {
            Files.createDirectories(storageRoot); // Create storage directory if it doesn't exist
            Files.createDirectories(archiveRoot); // Create archive directory if it doesn't exist
        } catch (IOException e) {
            throw new RuntimeException("Failed to create local storage directory or archive directory: " + e.getMessage(), e);
        }
    }

    @Override
    public void store(UUID uuid, byte[] data) throws IOException {
        Path filePath = storageRoot.resolve(uuid.toString());
        Files.write(filePath, data);
        logger.info("File stored locally: {}", filePath);
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        Path filePath = storageRoot.resolve(uuid.toString());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        Path filePath = storageRoot.resolve(uuid.toString());
        Files.deleteIfExists(filePath);
        logger.info("File deleted locally: {}", filePath);
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        Path sourcePath = storageRoot.resolve(uuid.toString());
        Path destinationPath = archiveRoot.resolve(uuid.toString());

        try {
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File archived locally: {} -> {}", sourcePath, destinationPath);
        } catch (IOException e) {
            logger.error("Error archiving file: {}", sourcePath, e);
            throw e;
        }

    }
}