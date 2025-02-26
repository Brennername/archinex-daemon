package com.danielremsburg.archinex.core;

import com.danielremsburg.archinex.cache.Cache;
import com.danielremsburg.archinex.cache.MemoryCache;
import com.danielremsburg.archinex.cache.RedisCache;
import com.danielremsburg.archinex.config.ArchinexConfig;
import com.danielremsburg.archinex.journal.FileJournal;
import com.danielremsburg.archinex.journal.Journal;
import com.danielremsburg.archinex.journal.MemoryJournal;
import com.danielremsburg.archinex.metadata.MemoryMetadataStore;
import com.danielremsburg.archinex.metadata.MetadataStore;
import com.danielremsburg.archinex.metadata.PostgresMetadataStore;
import com.danielremsburg.archinex.retention.BasicRetentionPolicy;
import com.danielremsburg.archinex.retention.RetentionPolicy;
import com.danielremsburg.archinex.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ArchinexDaemon {

    private static final Logger logger = LoggerFactory.getLogger(ArchinexDaemon.class);

    private final ArchinexConfig config;
    private final StorageSystem storageSystem;
    private final MetadataStore metadataStore;
    private final Journal journal;
    private final RetentionPolicy retentionPolicy;
    private final Cache cache;
    private final Planner planner;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private ConfigurableApplicationContext context;

    public ArchinexDaemon() throws IOException {
        config = new ArchinexConfig();
        String storageType = config.getStorageType();
        CloudStorage cloudStorage = null; // Initialize cloudStorage

        if ("local".equalsIgnoreCase(storageType)) {
            storageSystem = new LocalStorage(config);
        } else if ("aws-s3".equalsIgnoreCase(storageType)) {
            cloudStorage = new S3CloudStorage(config);  // Updated to use S3CloudStorage
            storageSystem = new CloudStorageWrapper(cloudStorage);
        } else {
            throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        }

        // Metadata store configuration
        String metadataStoreType = config.getMetadataStoreType();
        if ("memory".equalsIgnoreCase(metadataStoreType)) {
            metadataStore = new MemoryMetadataStore();
        } else if ("postgres".equalsIgnoreCase(metadataStoreType)) {
            metadataStore = new PostgresMetadataStore(config);
        } else {
            throw new IllegalArgumentException("Unsupported metadata store type: " + metadataStoreType);
        }

        // Journal configuration
        String journalType = config.getJournalType();
        if ("memory".equalsIgnoreCase(journalType)) {
            journal = new MemoryJournal();
        } else if ("file".equalsIgnoreCase(journalType)) {
            journal = new FileJournal(config);
        } else {
            throw new IllegalArgumentException("Unsupported journal type: " + journalType);
        }

        // Retention policy configuration
        String retentionPolicyType = config.getRetentionPolicyType();
        if ("basic".equalsIgnoreCase(retentionPolicyType)) {
            retentionPolicy = new BasicRetentionPolicy(config);
        } else {
            throw new IllegalArgumentException("Unsupported retention policy type: " + retentionPolicyType);
        }

        // Cache configuration
        String cacheType = config.getCacheType();
        if ("memory".equalsIgnoreCase(cacheType)) {
            cache = new MemoryCache(config);
        } else if ("redis".equalsIgnoreCase(cacheType)) {
            cache = new RedisCache(config);
        } else {
            throw new IllegalArgumentException("Unsupported cache type: " + cacheType);
        }

        // Executor and Planner setup
        int plannerThreadPoolSize = config.getPlannerThreadPoolSize();
        executorService = Executors.newFixedThreadPool(plannerThreadPoolSize);
        planner = new Planner(config, storageSystem, metadataStore, journal, retentionPolicy, cache, executorService);

        scheduler = Executors.newScheduledThreadPool(2);
    }

    public void start() {
        logger.info("Starting Archinex Daemon...");

        planner.start();

        // Scheduling the policy engine to run at fixed intervals
        int policyEngineInterval = config.getPolicyEngineInterval();
        scheduler.scheduleAtFixedRate(planner::runPolicyEngine, 0, policyEngineInterval, TimeUnit.SECONDS);

        // Starting the API server
        int apiPort = config.getApiPort();
        String apiHost = config.getApiHost();

        String[] args = {};
        context = SpringApplication.run(ArchinexDaemon.class, args);

        logger.info("Archinex Daemon started on port {}...", apiPort);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("Archinex Daemon shutting down...");
            stop();
        }
    }

    public void stop() {
        executorService.shutdown();
        scheduler.shutdown();

        if (context != null) {
            SpringApplication.exit(context);
        }

        logger.info("Archinex Daemon stopped.");
    }

    public static void main(String[] args) throws IOException {
        ArchinexDaemon daemon = new ArchinexDaemon();
        daemon.start();
    }
}
