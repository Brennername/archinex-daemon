package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class StoreAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(StoreAction.class);

    private final StorageSystem storageSystem;

    public StoreAction(StorageSystem storageSystem) {
        this.storageSystem = storageSystem;
    }

    @Override
    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        try {
            storageSystem.store(uuid, data); // Store the data
            logger.info("StoreAction executed for UUID: {}", uuid);
        } catch (IOException e) {
            logger.error("Error storing data for UUID: {}", uuid, e);
            throw e; // Re-throw the exception to be handled by the planner
        }
    }
}