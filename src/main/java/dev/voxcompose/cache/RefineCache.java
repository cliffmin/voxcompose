package dev.voxcompose.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache for refinement results. Uses a LinkedHashMap with access-order to implement LRU
 * eviction.
 */
public class RefineCache {
  private final int maxSize;
  private final long ttlMs;
  private final Map<String, CacheEntry> cache;

  public RefineCache(int maxSize, long ttlMs) {
    this.maxSize = maxSize;
    this.ttlMs = ttlMs;
    this.cache =
        new LinkedHashMap<String, CacheEntry>(maxSize + 1, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > maxSize;
          }
        };
  }

  /**
   * Generate a cache key from the input parameters. Uses SHA-256 hash of concatenated inputs for
   * consistent key generation.
   */
  public String generateKey(String model, String prompt, String systemPrompt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      String combined = model + "|" + prompt + "|" + systemPrompt;
      byte[] hash = digest.digest(combined.getBytes());

      // Convert to hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      // Fallback to simple hash
      return String.valueOf((model + prompt + systemPrompt).hashCode());
    }
  }

  /** Get a cached result if it exists and is not expired. */
  public synchronized String get(String key) {
    CacheEntry entry = cache.get(key);
    if (entry == null) {
      return null;
    }

    // Check if entry has expired
    if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
      cache.remove(key);
      return null;
    }

    return entry.value;
  }

  /** Put a result in the cache. */
  public synchronized void put(String key, String value) {
    cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
  }

  /** Clear all entries from the cache. */
  public synchronized void clear() {
    cache.clear();
  }

  /** Get current cache size. */
  public synchronized int size() {
    return cache.size();
  }

  /** Get cache statistics. */
  public synchronized CacheStats getStats() {
    int total = cache.size();
    int expired = 0;
    long now = System.currentTimeMillis();

    for (CacheEntry entry : cache.values()) {
      if (now - entry.timestamp > ttlMs) {
        expired++;
      }
    }

    return new CacheStats(total, total - expired, expired);
  }

  private static class CacheEntry {
    final String value;
    final long timestamp;

    CacheEntry(String value, long timestamp) {
      this.value = value;
      this.timestamp = timestamp;
    }
  }

  public static class CacheStats {
    public final int total;
    public final int valid;
    public final int expired;

    CacheStats(int total, int valid, int expired) {
      this.total = total;
      this.valid = valid;
      this.expired = expired;
    }
  }
}
