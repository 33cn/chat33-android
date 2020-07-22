package com.fuzamei.common.utils;

/**
 * @author zhengjy
 * @since 2018/12/19
 * Description:
 */
public class LruCache<K, V> extends android.util.LruCache<K, V> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruCache(int maxSize) {
        super(maxSize);
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }
}
