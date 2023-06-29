package app.model.server.cache;

import java.util.LinkedHashMap;

/*
 * Least Frequently Used Cache implementation,
 * by LinkedHashMap which stores the word as Key and its frequency counter as Value.
 * also contains a minKey variable for the removal function
 * 
 * @author: Aviv Cohen
 *
 */

public class LFU implements CacheReplacementPolicy {
    private LinkedHashMap<String, Integer> lfu;
    private String minKey;

    public LFU() {
        this.lfu = new LinkedHashMap<>();
    }

    @Override
    public void add(String word) {
        /*
         * signifies that a query has been issued for this word
         * if the map already contains this word, icrease its counter.
         * put in hash map in O(1)
         */
        if (lfu.containsKey(word)) {
            Integer freq = lfu.get(word);
            lfu.replace(word, freq, freq + 1);
        } else {
            lfu.put(word, 1);
        }
    }

    @Override
    public String remove() {
        /*
         * returns the word that has to be removed from the cache which has the lowest
         * counter.
         * in case of equality in the frequency, returns the word that entered
         * first(list)
         * scan and remove from hash map in O(n)
         */

        if (lfu.isEmpty()) {
            return null;
        }

        int minFreq = Integer.MAX_VALUE; // min val initialized to infinity for comparison

        for (String key : lfu.keySet()) {
            if (lfu.get(key) < minFreq) {
                minFreq = lfu.get(key);
                minKey = key;
            }
        }
        lfu.remove(minKey);
        return minKey;
    }

}
