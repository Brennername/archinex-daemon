package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RetrieveAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveAction.class);

    private final Storage storage;

    public RetrieveAction(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        try {
            byte[] retrievedData = storage.retrieve(uuid);
            logger.info("RetrieveAction executed for UUID: {}", uuid);
            // Here, we're directly using the retrievedData to do something
            processRetrievedData(retrievedData);
        } catch (IOException e) {
            logger.error("Error retrieving data for UUID: {}", uuid, e);
            throw e;
        }
    }

    /**
     * A method to retrieve the file and pass it to the provided consumer.
     * 
     * @param uuid the UUID of the file to retrieve.
     * @param dataConsumer the consumer to process the retrieved data.
     * @throws IOException if there is an error during retrieval.
     */
    public void retrieveAndProcess(UUID uuid, Consumer<byte[]> dataConsumer) throws IOException {
        try {
            byte[] retrievedData = storage.retrieve(uuid);
            logger.info("File retrieved for UUID: {}", uuid);
            dataConsumer.accept(retrievedData); // Process the data through the consumer
        } catch (IOException e) {
            logger.error("Error retrieving data for UUID: {}", uuid, e);
            throw e;
        }
    }

    /**
     * Process the retrieved data as part of the execute method. This can be customized further.
     * Default just log the retrieved data length.
     */
    public void processRetrievedData(byte[] retrievedData) {
        if (retrievedData != null) {
            logger.info("Data retrieved successfully, length: {}", retrievedData.length);
            // Default processing - log and move on
            logger.info("Default processing: Data length logged.");
        } else {
            logger.error("Failed to retrieve data: No data found.");
        }
    }


     // Method to inverts control (allowing client-provided processing)
     public void processRetrievedData(byte[] retrievedData, Consumer<byte[]> clientProcessor) {
        if (retrievedData != null) {
            logger.info("Data retrieved successfully, length: {}", retrievedData.length);

            // Default processing (your existing logic)
            logger.info("Default processing: Data length logged.");

            // Client-provided processing (if any)
            if (clientProcessor != null) {
                clientProcessor.accept(retrievedData);
            }
        } else {
            logger.error("Failed to retrieve data: No data found.");
        }
    }

}
