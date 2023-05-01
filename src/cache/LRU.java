package cache;

import java.util.Iterator;
import java.util.LinkedHashSet;

/*
 * Least Recently Used Cache implementation,
 * by LinkedHashSet
 * 
 * @author: Aviv Cohen
 *
 */

public class LRU implements CacheReplacementPolicy {
    private LinkedHashSet<String> lru;

    public LRU() {
        this.lru = new LinkedHashSet<>();
    }

    @Override
    public void add(String word) {

        /*
         * signifies that a query has been issued for this word
         * if the set already contains this word, just remove and add it back.
         * add/remove from hash set in O(1)
         */

        if (lru.contains(word)) {
            lru.remove(word);
            lru.add(word);
        } else {
            lru.add(word);
        }

    }

    @Override
    public String remove() {
        /*
         * returns the word that needs to be removed from the cache
         * which is the head of the linked set.
         * remove from hash set in O(1)
         */

        if (lru.isEmpty()) {
            return null;
        }

        Iterator<String> it = lru.iterator();
        String removeVal = it.next(); // head of the list - first in line
        lru.remove(it.next());
        return removeVal;
    }

}
