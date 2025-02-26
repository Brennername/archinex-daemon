package com.danielremsburg.archinex.storage;

import java.io.IOException;
import java.util.UUID;

public class CloudStorageWrapper implements StorageSystem {

    private final CloudStorage cloudStorage;

    public CloudStorageWrapper(CloudStorage cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    @Override
    public void store(UUID uuid, byte[] data) throws IOException {
        cloudStorage.store(uuid, data, null); // No metadata by default
    }

    @Override
    public byte[] retrieve(UUID uuid) throws IOException {
        return cloudStorage.retrieve(uuid);
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        cloudStorage.delete(uuid);
    }

    @Override
    public void archive(UUID uuid) throws IOException {
        cloudStorage.archive(uuid); // Call the archive method on your CloudStorage
    }
}