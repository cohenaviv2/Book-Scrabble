package model.server.cache;

import java.util.HashSet;

/*
 * Cache manager implementation by HashSet
 * stores in memory the answers to the most common queries
 * search in O(1), its time and size is constant.
 * also contains a Cache Replacement Policy which removes a query when the cache is full
 * (etc. LRU, LFU)
 * 
 * @author: Aviv Cohen
 *
 */

public class CacheManager {
    private HashSet<String> cache;
    private int capacity;
    private CacheReplacementPolicy crp;

    public CacheManager(int size, CacheReplacementPolicy crp) {
        this.capacity = size;
        this.cache = new HashSet<>(this.capacity);
        this.crp = crp;
    }

    public boolean query(String word) {
        /* returns a boolean whether the word is in the cache or not */

        return cache.contains(word);
    }

    public void add(String word) {
        /*
         * This method updates the crp, add the word to the cache, and if its size
         * is large from the maximum capacity, removes from the cache the word
         * chosen by the crp.
         */

        this.crp.add(word);
        this.cache.add(word);

        if (cache.size() > capacity) {
            cache.remove(crp.remove());
        }
    }

}
