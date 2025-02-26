package com.danielremsburg.archinex.storage;

import com.danielremsburg.archinex.config.ArchinexConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class LocalStorage implements Storage {

    private final Path storageDirectory;

    // Constructor now takes ArchinexConfig
    public LocalStorage(ArchinexConfig config) {
        // Extract storage path from the config
        String storagePath = config.getString("storage.local.path");
        this.storageDirectory = Paths.get(storagePath);

        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    @Override
    public void store(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());
        Files.write(filePath, data);
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());
        return Files.readAllBytes(filePath);
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        Path filePath = storageDirectory.resolve(uuid.toString());
        Files.delete(filePath);
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        Path archiveDir = storageDirectory.resolve("archive");
        if (!Files.exists(archiveDir)) {
            Files.createDirectories(archiveDir);
        }
        Path originalFile = storageDirectory.resolve(uuid.toString());
        Path archiveFile = archiveDir.resolve(uuid.toString());
        Files.move(originalFile, archiveFile);
    }
}
