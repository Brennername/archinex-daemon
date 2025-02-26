package com.danielremsburg.archinex.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ArchinexConfig {

    private static final Logger logger = LoggerFactory.getLogger(ArchinexConfig.class);

    private static final String ARCHINEX_HOME = System.getProperty("user.home") + "/.archinex";
    private static final String CONFIG_FILE = ARCHINEX_HOME + "/config/archinex.json";
    private static final String DEFAULT_CONFIG_FILE = "archinex_default.json"; // Path to default config in resources

    private final JsonObject config;

    public ArchinexConfig() throws IOException {
        this(CONFIG_FILE);
    }

    public ArchinexConfig(String configFile) throws IOException {
        createDirectoriesAndCopyDefaultConfig(); // Combined directory creation and default config copy
        Gson gson = new GsonBuilder().create();
        try (FileReader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            logger.error("Error reading configuration file: {}", configFile, e);
            throw e; // Re-throw after logging
        }
    }

    private void createDirectoriesAndCopyDefaultConfig() throws IOException {
        try {
            // Create directories
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "config"));
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "data", "metadata"));
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "data", "journal"));
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "data", "cache"));
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "data", "storage"));
            Files.createDirectories(Paths.get(ARCHINEX_HOME, "logs"));

            // Copy default config if it doesn't exist
            Path configFile = Paths.get(ARCHINEX_HOME, "config", "archinex.json");
            if (!Files.exists(configFile)) {
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                    if (inputStream == null) {
                        throw new IOException("Default config file not found in resources: " + DEFAULT_CONFIG_FILE);
                    }
                    Files.copy(inputStream, configFile, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Default config copied to: {}", configFile);
                }
            }

        } catch (IOException e) {
            logger.error("Error creating directories or copying default config: {}", e.getMessage(), e);
            throw e;
        }

    }

    public String getString(String path) {
        return getStringOrDefault(path, null);
    }

    public String getStringOrDefault(String path, String defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsString() : defaultValue;
    }

    public int getInt(String path) {
        return getIntOrDefault(path, 0);
    }

    public int getIntOrDefault(String path, int defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsInt() : defaultValue;
    }

    public boolean getBoolean(String path) {
        return getBooleanOrDefault(path, false);
    }

    public boolean getBooleanOrDefault(String path, boolean defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsBoolean() : defaultValue;
    }

    public long getLong(String path) {
        return getLongOrDefault(path, 0L);
    }

    public long getLongOrDefault(String path, long defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsLong() : defaultValue;
    }

    public double getDouble(String path) {
        return getDoubleOrDefault(path, 0.0);
    }

    public double getDoubleOrDefault(String path, double defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsDouble() : defaultValue;
    }

    private JsonElement getJsonElement(String path) {
        if (config == null) {
            return null; // Handle the case where the config is not loaded
        }
        String[] parts = path.split("\\.");
        JsonElement current = config;
        for (String part : parts) {
            if (current == null || !current.isJsonObject() || !current.getAsJsonObject().has(part)) {
                return null;
            }
            current = current.getAsJsonObject().get(part);
        }
        return current;
    }


    public String getStorageType() {
        return getStringOrDefault("storage.type", "local");
    }

    public String getLocalStoragePath() {
        return getStringOrDefault("storage.local.path", ARCHINEX_HOME + "/data/storage");
    }

    public String getMetadataStorePath() {
        return getStringOrDefault("metadata.store.path", ARCHINEX_HOME + "/data/metadata/metadata.db"); // Example DB path
    }

    public String getJournalFilePath() {
        return getStringOrDefault("journal.file.path", ARCHINEX_HOME + "/data/journal/journal.log");
    }

    public String getRetentionPolicyConfigPath() {
        return getStringOrDefault("retention.policy.config.path", ARCHINEX_HOME + "/config/retention_policy_config.json"); // Example
    }

    public String getCacheConfigPath() {
        return getStringOrDefault("cache.config.path", ARCHINEX_HOME + "/config/cache_config.json"); // Example
    }

    public String getCloudStorageProvider() {
        return getString("storage.cloud.provider");
    }

    public String getCloudStorageBucket() {
        return getString("storage.cloud.bucket");
    }

    public String getCloudStorageAccessKey() {
        return getString("storage.cloud.accessKey");
    }

    public String getCloudStorageSecretKey() {
        return getString("storage.cloud.secretKey");
    }

    public String getMetadataStoreType() {
        return getStringOrDefault("metadata.store.type", "memory");
    }

    public String getJournalType() {
        return getStringOrDefault("journal.type", "memory");
    }

    public String getRetentionPolicyType() {
        return getStringOrDefault("retention.policy.type", "basic");
    }

    public String getCacheType() {
        return getStringOrDefault("cache.type", "memory");
    }

    public int getMaxCacheSize() {
        return getIntOrDefault("cache.maxSize", 1024);
    }

    public int getPlannerThreadPoolSize() {
        return getIntOrDefault("planner.threadPoolSize", 4);
    }

    public int getPolicyEngineInterval() {
        return getIntOrDefault("policyEngine.interval", 3600);
    }

    public int getApiPort() {
        return getIntOrDefault("api.port", 9876);
    }
    public String getApiHost() {
        return getStringOrDefault("api.host", "localhost");
    }

    public long getLongPropertyOrDefault(String path, long defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsLong() : defaultValue;
    }

    public int getIntPropertyOrDefault(String path, int defaultValue) {
        JsonElement element = getJsonElement(path);
        return element != null && !element.isJsonNull() ? element.getAsInt() : defaultValue;
    }

}