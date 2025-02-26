package com.danielremsburg.archinex.storage;

import java.io.IOException;
import java.util.UUID;

public interface StorageSystem {

    void store(UUID uuid, byte[] data) throws IOException;

    byte[] retrieve(UUID uuid) throws IOException;

    void delete(UUID uuid) throws IOException;

    void archive(UUID uuid) throws IOException; // Added archive method
}