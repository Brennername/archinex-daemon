package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class StoreAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(StoreAction.class);

    private final Storage storage;

    public StoreAction(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        try {
            storage.store(uuid, data, metadata);
            logger.info("StoreAction executed for UUID: {}", uuid);
        } catch (IOException e) {
            logger.error("Error storing data for UUID: {}", uuid, e);
            throw e;
        }
    }
}
