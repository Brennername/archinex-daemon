package com.danielremsburg.archinex.storage;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface Storage {

    void store(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException;

    byte[] retrieve(UUID uuid) throws IOException;

    void delete(UUID uuid) throws IOException;

    void archive(UUID uuid) throws IOException;
}
