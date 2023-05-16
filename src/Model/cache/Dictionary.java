package Model.Cache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/*
 * represents a Book dictionary, responsible for the logic of word search in the dictionary.
 * The search is performed fisrt in two caches, one of the words that are found and the other of words that are not found.
 * then move to serach in the BloomFilter and finally performs an I/O based search.
 * 
 * @author: Aviv Cohen
 * 
 */

public class Dictionary {
    private CacheManager wordsExistCache;
    private CacheManager dontExistCache;
    private BloomFilter bloomFilter;
    private String[] fileList;

    public Dictionary(String... fileNames) {
        this.fileList = fileNames;
        this.wordsExistCache = new CacheManager(400, new LRU());
        this.dontExistCache = new CacheManager(100, new LFU());
        this.bloomFilter = new BloomFilter(256, "MD5", "SHA1");

        bloomFilterInit(fileNames);
    }

    public boolean query(String word) {
        /*
         * first search in the cache of existing words,
         * then in the cache of non-existing words,
         * finally search in the BloomFilter and updated the the matching cache.
         * 
         */
        if (this.wordsExistCache.query(word))
            return true;
        else if (this.dontExistCache.query(word))
            return false;
        else {
            if (this.bloomFilter.contains(word)) {
                this.wordsExistCache.add(word);
                return true;
            } else {
                this.dontExistCache.add(word);
                return false;
            }
        }
    }

    public boolean challenge(String word) {
        /*
         * The user chooses to challenge the dictionary, thinking that the dictionary
         * was wrong and the word is actually not found
         * then an I/O based search will be performed
         * game rules will penalize the challenger with drop points if he is
         * troubled the server for nothing, or will give him a bonus if he was right.
         */
        
        if (IOSearcher.search(word, this.fileList)) {
            this.wordsExistCache.add(word);
            return true;
        } else {
            this.dontExistCache.add(word);
            return false;
        }
    }

    public void bloomFilterInit(String... fileNames) {
        /* Scan and add all the words in to the bloomFilter */

        for (String book : fileNames) {
            try {
                Scanner myScanner = new Scanner(new BufferedReader(new FileReader(book)));
                while (myScanner.hasNext()) {
                    this.bloomFilter.add(myScanner.next());
                }
                myScanner.close();
            } catch (FileNotFoundException e) {
                System.out.println("Exception thrown : " + e);
            }
        }
    }
}
