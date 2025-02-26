package com.danielremsburg.archinex.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalStorageSystem implements StorageSystem {
    private static final Logger logger = LoggerFactory.getLogger(LocalStorageSystem.class);

    private final String storagePath;

    public LocalStorageSystem(String storagePath) {
        this.storagePath = storagePath;
        try {
            Files.createDirectories(Paths.get(storagePath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    @Override
    public void store(UUID uuid, byte[] data) throws IOException {
        Path filePath = getFilePath(uuid);
        try {
            Files.write(filePath, data);
            logger.info("File stored: {}", filePath);
        } catch (IOException e) {
            logger.error("Error storing file: {}", filePath, e);
            throw e;
        }
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        Path filePath = getFilePath(uuid);
        try {
            byte[] data = Files.readAllBytes(filePath);
            logger.info("File retrieved: {}", filePath);
            return data;
        } catch (IOException e) {
            logger.error("Error retrieving file: {}", filePath, e);
            throw e;
        }
    }


    @Override
    public void delete(UUID uuid) throws IOException {
        Path filePath = getFilePath(uuid);
        try {
            Files.deleteIfExists(filePath);
            logger.info("File deleted: {}", filePath);
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
            throw e;
        }
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        Path filePath = getFilePath(uuid);
        Path archiveDir = Paths.get(storagePath, "archive"); // Archive directory
        try {
            Files.createDirectories(archiveDir); // Create archive directory if it doesn't exist
            Path archivedFilePath = archiveDir.resolve(uuid.toString()); // Path in archive
            Files.move(filePath, archivedFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING); // Move to archive
            logger.info("File archived: {}", archivedFilePath);
        } catch (IOException e) {
            logger.error("Error archiving file: {}", filePath, e);
            throw e;
        }
    }

    private Path getFilePath(UUID uuid) {
        return Paths.get(storagePath, uuid.toString());
    }
}