package com.danielremsburg.archinex.storage;

import com.danielremsburg.archinex.config.ArchinexConfig;
import java.io.IOException;

public class StorageFactory {

    public static Storage createStorage(ArchinexConfig config) {
        String storageType = config.getString("storage.type");

        if ("local".equals(storageType)) {
            return new LocalStorage(config); // LocalStorage remains as before
        }

        String cloudRegion = config.getString("storage.cloud.region");
        if (cloudRegion != null && !cloudRegion.isEmpty()) {
            return createCloudStorage(config); // Create the CloudStorage
        }

        return new LocalStorage(config); // Default to LocalStorage if type is unknown
    }

    public static Storage createStorage() throws IOException {
        ArchinexConfig defaultConfig = getDefaultConfig();
        return createStorage(defaultConfig); // Use the default config
    }

    private static ArchinexConfig getDefaultConfig() throws IOException {
        return new ArchinexConfig(); // Default config, modify as needed
    }

    // This method abstracts the creation of the cloud storage type.
    private static CloudStorage createCloudStorage(ArchinexConfig config) {
        String cloudProvider = config.getString("storage.cloud.provider");

        // Currently, we only support S3 as a cloud provider, but you can expand this in the future.
        if ("s3".equalsIgnoreCase(cloudProvider)) {
            return new S3CloudStorage(config); // Instantiating the specific cloud storage
        }

        // You can add more cloud providers here (e.g., Google Cloud, Azure).
        throw new UnsupportedOperationException("Cloud provider not supported: " + cloudProvider);
    }
}
