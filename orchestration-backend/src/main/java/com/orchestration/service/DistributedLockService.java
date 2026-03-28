package com.orchestration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-based distributed lock using Lettuce client.
 * Ensures that in a dual-instance deployment, only one instance processes
 * a given trigger event or exclusive task.
 */
@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "orchestration:lock:";

    private final StringRedisTemplate redisTemplate;
    private final String instanceId = UUID.randomUUID().toString();
    private final ThreadLocal<String> currentLockValue = new ThreadLocal<>();

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Try to acquire a distributed lock.
     *
     * @param lockKey    unique lock identifier
     * @param leaseTimeMs lock expiration time in milliseconds
     * @return true if lock acquired
     */
    public boolean tryLock(String lockKey, long leaseTimeMs) {
        String key = LOCK_PREFIX + lockKey;
        String value = instanceId + ":" + Thread.currentThread().getId() + ":" + System.nanoTime();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofMillis(leaseTimeMs));

        if (Boolean.TRUE.equals(acquired)) {
            currentLockValue.set(value);
            log.debug("[Lock] Acquired lock: {}", lockKey);
            return true;
        }

        log.debug("[Lock] Failed to acquire lock: {}", lockKey);
        return false;
    }

    /**
     * Release the lock (only if we own it).
     */
    public void unlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String expectedValue = currentLockValue.get();

        if (expectedValue == null) return;

        String currentValue = redisTemplate.opsForValue().get(key);
        if (expectedValue.equals(currentValue)) {
            redisTemplate.delete(key);
            log.debug("[Lock] Released lock: {}", lockKey);
        }
        currentLockValue.remove();
    }

    /**
     * Check if a lock is currently held.
     */
    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
