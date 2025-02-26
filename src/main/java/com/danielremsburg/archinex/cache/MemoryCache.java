package com.danielremsburg.archinex.cache;

import com.danielremsburg.archinex.config.ArchinexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCache.class);
    private final Map<UUID, byte[]> cache = new HashMap<>();
    private final int maxSize; // Maximum cache size in MB

    public MemoryCache(ArchinexConfig config) {
        this.maxSize = config.getIntPropertyOrDefault("cache.maxSize", 1024); // Default 1GB
        logger.info("MemoryCache initialized with max size: {} MB", maxSize);
    }

    @Override
    public byte[] get(UUID uuid) {
        byte[] data = cache.get(uuid);
        if (data != null) {
            logger.debug("Cache hit for UUID: {}", uuid);
        } else {
            logger.debug("Cache miss for UUID: {}", uuid);
        }
        return data;
    }

    @Override
    public void put(UUID uuid, byte[] data) {
        // Implement a cache eviction policy if the cache size exceeds maxSize
        cache.put(uuid, data);
        logger.debug("Added to cache: {}", uuid);
    }

    @Override
    public void remove(UUID uuid) {
        cache.remove(uuid);
        logger.debug("Removed from cache: {}", uuid);
    }
}