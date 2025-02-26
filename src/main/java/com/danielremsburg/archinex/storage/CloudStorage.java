package com.danielremsburg.archinex.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public interface CloudStorage {

    void store(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException; // Added metadata

    byte[] retrieve(UUID uuid) throws IOException;

    void delete(UUID uuid) throws IOException;

    void archive(UUID uuid) throws IOException; // Added archive method
}