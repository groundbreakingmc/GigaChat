package groundbreaking.gigachat.utils.map;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.concurrent.TimeUnit;

public final class ExpiringMap<K, V> {

    private final Cache<K, V> cache;

    public ExpiringMap(final long duration, final TimeUnit unit) {
        this.cache = CaffeineFactory.newBuilder()
                .expireAfterWrite(duration, unit)
                .build();
    }

    public void put(final K key, final V value) {
        this.cache.put(key, value);
    }

    public V get(final K key) {
        return this.cache.getIfPresent(key);
    }

    public boolean containsKey(final K key) {
        return this.cache.getIfPresent(key) != null;
    }

    public void remove(final K key) {
        this.cache.invalidate(key);
    }

    public long size() {
        return this.cache.estimatedSize();
    }

    public void clear() {
        this.cache.invalidateAll();
    }
}
