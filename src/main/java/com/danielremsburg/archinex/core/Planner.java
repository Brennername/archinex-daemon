package com.danielremsburg.archinex.core;

import com.danielremsburg.archinex.cache.Cache;
import com.danielremsburg.archinex.config.ArchinexConfig;
import com.danielremsburg.archinex.journal.Journal;
import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.metadata.MetadataStore;
import com.danielremsburg.archinex.metadata.MetadataStoreException;
import com.danielremsburg.archinex.retention.RetentionPolicy;
import com.danielremsburg.archinex.storage.StorageSystem;
import com.danielremsburg.archinex.plan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class Planner {

    private static final Logger logger = LoggerFactory.getLogger(Planner.class);

    private final ArchinexConfig config;
    private final StorageSystem storageSystem;
    private final MetadataStore metadataStore;
    private final Journal journal;
    private final RetentionPolicy retentionPolicy;
    private final Cache cache;
    private final ExecutorService executorService;
    private final PlanFactory planFactory;
    private final PlanExecutor planExecutor;
    private final DecisionMaker decisionMaker;

    public Planner(ArchinexConfig config, StorageSystem storageSystem, MetadataStore metadataStore,
                   Journal journal, RetentionPolicy retentionPolicy, Cache cache, ExecutorService executorService) {
        this.config = config;
        this.storageSystem = storageSystem;
        this.metadataStore = metadataStore;
        this.journal = journal;
        this.retentionPolicy = retentionPolicy;
        this.cache = cache;
        this.executorService = executorService;

        this.planFactory = new PlanFactory(storageSystem);
        this.planExecutor = new PlanExecutor(executorService);
        this.decisionMaker = new DecisionMaker(planFactory, config);
    }

    public void start() {
        logger.info("Starting Planner...");
    }

    public void runPolicyEngine() {
        logger.info("Running Policy Engine...");
        try {
            metadataStore.getAllFiles().forEach(fileMetadata -> {
                if (retentionPolicy.shouldDelete(fileMetadata)) {
                    try {
                        storageSystem.delete(fileMetadata.getUuid());
                        metadataStore.delete(fileMetadata.getUuid());
                        journal.log("File deleted: " + fileMetadata.getUuid());
                        cache.remove(fileMetadata.getUuid());
                    } catch (Exception e) {
                        logger.error("Error deleting file: " + fileMetadata.getUuid(), e);
                        journal.log("Error deleting file: " + fileMetadata.getUuid() + ": " + e.getMessage());
                    }
                }
            });
        } catch (MetadataStoreException e) {
            logger.error("Error retrieving all files from metadata store: {}", e.getMessage(), e);
            journal.log("Error retrieving all files from metadata store: " + e.getMessage());
        }
    }

    public void storeFile(String path, byte[] data, Map<String, String> metadata) throws IOException {
        UUID uuid = java.util.UUID.randomUUID();
        FileMetadata fileMetadata = new FileMetadata(uuid, path, data.length);
        try {
            metadataStore.store(fileMetadata);
        } catch (MetadataStoreException e) {
            logger.error("Error storing metadata: {}", e.getMessage(), e);
            throw new IOException("Error storing metadata: " + e.getMessage(), e);
        }

        try {
            Plan plan = decisionMaker.choosePlan(fileMetadata);
            planExecutor.executePlan(plan, uuid, data, metadata);
            journal.log("File storage plan executed: " + path + " (UUID: " + uuid + ")");
            cache.put(uuid, data);
        } catch (RuntimeException e) {
            try {
                metadataStore.delete(uuid);
            } catch (MetadataStoreException ex) {
                logger.error("Error deleting metadata after storage failure: {}", ex.getMessage(), ex);
            }
            throw e;
        }
    }


    public byte[] retrieveFile(String uuid) throws IOException {
        byte[] cachedData = cache.get(UUID.fromString(uuid));
        if (cachedData != null) {
            return cachedData;
        }

        FileMetadata metadata;
        try {
            metadata = metadataStore.get(UUID.fromString(uuid));
        } catch (MetadataStoreException e) {
            logger.error("Error retrieving metadata: {}", e.getMessage(), e);
            throw new IOException("Error retrieving metadata: " + e.getMessage(), e);
        }

        if (metadata == null) {
            throw new IOException("File not found: " + uuid);
        }

        try {
            Plan plan = decisionMaker.choosePlan(metadata);
            AtomicReference<byte[]> retrievedDataHolder = new AtomicReference<>();
            plan.getActions().stream().filter(RetrieveAction.class::isInstance).findFirst().ifPresent(action -> {
                try {
                    ((RetrieveAction) action).retrieveAndProcess(UUID.fromString(uuid), retrievedDataHolder::set);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            byte[] retrievedData = retrievedDataHolder.get();

            if (retrievedData != null) {
                cache.put(UUID.fromString(uuid), retrievedData);
            }

            return retrievedData;
        } catch (Exception e) {
            throw e;
        }
    }
}