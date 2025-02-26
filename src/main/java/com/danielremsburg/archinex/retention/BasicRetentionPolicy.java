package com.danielremsburg.archinex.retention;

import com.danielremsburg.archinex.cache.Cache;
import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.metadata.MetadataStore;
import com.danielremsburg.archinex.metadata.MetadataStoreException;
import com.danielremsburg.archinex.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BasicRetentionPolicy implements RetentionPolicy {

    private static final Logger logger = LoggerFactory.getLogger(BasicRetentionPolicy.class);

    private final String name;
    private final String description;
    private final List<RetentionRule> rules;
    private final MetadataStore metadataStore;
    private final Cache cache;

    // Constructor adjusted: removed storageSystem and used StorageFactory directly in methods
    public BasicRetentionPolicy(String name, String description, List<RetentionRule> rules, MetadataStore metadataStore, Cache cache) {
        this.name = name;
        this.description = description;
        this.rules = rules;
        this.metadataStore = metadataStore;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void apply(FileMetadata metadata) throws MetadataStoreException, IOException {
        for (RetentionRule rule : rules) {
            if (rule.getType().equals("age")) {
                Instant cutOffDate = calculateCutOffDate(metadata.getCreationDate(), rule.getUnit(), rule.getValue());
                if (Instant.now().isAfter(cutOffDate)) {
                    RetentionAction action = rule.getAction();
                    switch (action) {
                        case ARCHIVE:
                            // Use StorageFactory to get the appropriate storage system and archive the file
                            StorageFactory.createStorage().archive(metadata.getUuid());
                            break;
                        case DELETE:
                        default:
                            // Use StorageFactory to get the appropriate storage system and delete the file
                            StorageFactory.createStorage().delete(metadata.getUuid());
                            break;
                    }
                    metadataStore.delete(metadata.getUuid()); // Remove metadata from store
                    cache.remove(metadata.getUuid()); // Remove file from cache
                    logger.info("File {} {}d", metadata.getUuid(), action.toString().toLowerCase());
                    break;
                }
            }
        }
    }

    private Instant calculateCutOffDate(Instant creationDate, String unit, int value) {
        switch (unit) {
            case "days":
                return creationDate.plus(value, ChronoUnit.DAYS);
            case "weeks":
                return creationDate.plus(value, ChronoUnit.WEEKS);
            case "months":
                return creationDate.plus(value, ChronoUnit.MONTHS);
            case "years":
                return creationDate.plus(value, ChronoUnit.YEARS);
            case "seconds": // For testing
                return creationDate.plus(value, ChronoUnit.SECONDS);
            default:
                throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }

    @Override
    public boolean shouldDelete(FileMetadata fileMetadata) {
        for (RetentionRule rule : rules) {
            if (rule.getType().equals("age")) {
                Instant cutOffDate = calculateCutOffDate(fileMetadata.getCreationDate(), rule.getUnit(), rule.getValue());
                if (Instant.now().isAfter(cutOffDate)) {
                    return true;
                }
            }
        }
        return false;
    }
}
