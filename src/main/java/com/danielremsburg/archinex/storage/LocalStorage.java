package com.danielremsburg.archinex.storage;

import com.danielremsburg.archinex.config.ArchinexConfig;
import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.metadata.MetadataStore;
import com.danielremsburg.archinex.metadata.MemoryMetadataStore;
import com.danielremsburg.archinex.metadata.PostgresMetadataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

public class LocalStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorage.class);

    private final MetadataStore metadataStore;
    private final Path storageDirectory;

    // Constructor accepting ArchinexConfig
    public LocalStorage(ArchinexConfig config) {
        // Get the metadata store type from config (defaults to "memory" if not found)
        String metadataStoreType = config.getString("storage.metadata.store");
        if (metadataStoreType == null || metadataStoreType.isEmpty()) {
            metadataStoreType = "memory"; // Default to memory store
        }

        // Initialize the metadataStore based on the config setting
        if ("postgres".equalsIgnoreCase(metadataStoreType)) {
            this.metadataStore = new PostgresMetadataStore(config);
        } else {
            this.metadataStore = new MemoryMetadataStore(); // Default to memory store
        }

        // Get the storage path from the config
        String storagePath = config.getString("storage.local.path");
        String expandedPath = storagePath.replace("~", System.getProperty("user.home"));
        this.storageDirectory = Paths.get(expandedPath);

        // Ensure the directory exists
        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory); 
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }

        logger.info("LocalStorage initialized with path: {}", storageDirectory);
    }

    // Store file with metadata
    public void store(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        // Save the file to the storage directory
        Path filePath = storageDirectory.resolve(uuid.toString());
        Files.write(filePath, data); // Save the file bytes

        // If metadata is provided, store it
        if (metadata != null) {
            String path = filePath.toString();
            long size = Files.size(filePath);
            FileMetadata fileMetadata = new FileMetadata(uuid, path, size);
            metadata.forEach((key, value) -> {
                if ("contentType".equals(key)) {
                    fileMetadata.setContentType(value);
                }
            });
            // Store metadata
            try {
                metadataStore.store(fileMetadata);
            } catch (Exception e) {
                logger.error("Error storing metadata for file {}: {}", uuid, e.getMessage());
                throw new IOException("Failed to store metadata", e);
            }
        }

        logger.info("File stored with UUID: {}", uuid);
    }

    // Retrieve file by UUID
    public byte[] retrieve(UUID uuid) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());

        // Check if file exists
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + uuid);
        }

        return Files.readAllBytes(filePath);
    }

    // Delete file and its metadata
    public void delete(UUID uuid) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());

        // Check if file exists before deleting
        if (Files.exists(filePath)) {
            Files.delete(filePath);  // Delete the file
            logger.info("File deleted: {}", uuid);
        } else {
            logger.warn("File not found for deletion: {}", uuid);
        }

        // Delete metadata
        try {
            metadataStore.delete(uuid);
            logger.info("Metadata deleted for UUID: {}", uuid);
        } catch (Exception e) {
            logger.error("Error deleting metadata for UUID {}: {}", uuid, e.getMessage());
            throw new IOException("Failed to delete metadata", e);
        }
    }

    // Archive (move the file to archive location)
    public void archive(UUID uuid) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());

        if (Files.exists(filePath)) {
            // Archive logic (moving the file to an archive directory)
            Path archiveDirectory = storageDirectory.resolve("archive");
            Files.createDirectories(archiveDirectory);
            Path archivePath = archiveDirectory.resolve(filePath.getFileName());

            Files.move(filePath, archivePath, StandardCopyOption.REPLACE_EXISTING); // Move the file
            logger.info("File archived: {}", uuid);
        } else {
            logger.warn("File not found for archiving: {}", uuid);
        }
    }

    // Additional method to get metadata by UUID (useful for checking metadata)
    public FileMetadata getMetadata(UUID uuid) throws IOException {
        try {
            return metadataStore.get(uuid);
        } catch (Exception e) {
            logger.error("Error retrieving metadata for UUID {}: {}", uuid, e.getMessage());
            throw new IOException("Failed to retrieve metadata", e);
        }
    }

}
