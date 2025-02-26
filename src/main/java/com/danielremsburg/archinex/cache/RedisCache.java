package com.danielremsburg.archinex.cache;

import com.danielremsburg.archinex.config.ArchinexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

public class RedisCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final JedisPool jedisPool;

    public RedisCache(ArchinexConfig config) {
        String host = config.getStringOrDefault("redis.host", "localhost");
        int port = config.getIntOrDefault("redis.port", 6379);
        String password = config.getString("redis.password");

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        if (password != null && !password.isEmpty()) {
            jedisPool = new JedisPool(poolConfig, host, port, 10000, password);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, 10000);
        }

        logger.info("RedisCache initialized. Host: {}, Port: {}", host, port);
    }

    @Override
    public byte[] get(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] data = jedis.get(uuid.toString().getBytes());
            if (data != null) {
                logger.debug("Cache hit for UUID: {}", uuid);
            } else {
                logger.debug("Cache miss for UUID: {}", uuid);
            }
            return data;
        } catch (Exception e) {
            logger.error("Error getting from Redis: {}", e.getMessage(), e);
            throw new CacheException("Error getting from Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public void put(UUID uuid, byte[] data) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(uuid.toString().getBytes(), data);
            logger.debug("Added to cache: {}", uuid);
        } catch (Exception e) {
            logger.error("Error putting into Redis: {}", e.getMessage(), e);
            throw new CacheException("Error putting into Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(uuid.toString().getBytes());
            logger.debug("Removed from cache: {}", uuid);
        } catch (Exception e) {
            logger.error("Error removing from Redis: {}", e.getMessage(), e);
            throw new CacheException("Error removing from Redis: " + e.getMessage(), e);
        }
    }
}