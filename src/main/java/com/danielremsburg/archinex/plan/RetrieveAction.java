package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RetrieveAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveAction.class);

    private final StorageSystem storageSystem;

    public RetrieveAction(StorageSystem storageSystem) {
        this.storageSystem = storageSystem;
    }

    @Override
    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        retrieveAndProcess(uuid, (retrievedData) -> {
            if (retrievedData != null) {
                logger.info("RetrieveAction executed successfully for UUID: {}", uuid);
            }
        });
    }

    public void retrieveAndProcess(UUID uuid, Consumer<byte[]> dataConsumer) throws IOException {
        try {
            byte[] retrievedData = storageSystem.retrieve(uuid);
            if (retrievedData == null) {
                throw new IOException("Data not found for UUID: " + uuid);
            }
            dataConsumer.accept(retrievedData); // Pass the data to the consumer
        } catch (IOException e) {
            logger.error("Error retrieving data for UUID: {}", uuid, e);
            throw e;
        }
    }
}