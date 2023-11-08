package app.model.server;

import java.util.*;

import app.model.server.cache.Dictionary;

/*
 * The dictionary manager creates new dictionaries only as needed
 * and provides the answers of the relevant dictionaries.
 * each book gets its own exclusive Dictionary.
 * contains a Pool(HashMap) of dictionaries
 * 
 * @author: Aviv Cohen
 * 
 */

public class DictionaryManager {
    public static DictionaryManager dictManInstance = null; // Singleton
    private Map<String, Dictionary> pool;
    int size;

    public DictionaryManager() {
        this.pool = new HashMap<>();
        this.size = 0;
    }

    public boolean query(String... args) {
        /*
         * By given a list of books (args), checks for each book if its in the pool
         * if not, create new Dictionary for this book.
         * Each query, which is the last word in arg [n-1],
         * will be searched in each book separately to update the cache of each relevant
         * dictionary (saves time for future queries).
         */

        String word = args[args.length - 1].toLowerCase();
        boolean result = false;

        for (int i = 0; i < args.length - 1; i++) {
            String book = args[i];
            if (this.pool.get(book) == null) {
                Dictionary dict = new Dictionary(book);
                this.pool.put(book, dict);
                this.size++;
                if (this.pool.get(book).query(word))
                    result = true;
            }
            if (this.pool.get(book).query(word))
                result = true;
        }

        return result;
    }

    public boolean challenge(String... args) {
        /*
         * The Player chooses to challenge the game dictionary
         * thinking that the dictionary was wrong and the word exist in one of the
         * books.
         * This will perform an I/O search in the books.
         */

        String word = args[args.length - 1].toLowerCase();
        boolean result = false;

        for (int i = 0; i < args.length - 1; i++) {
            String book = args[i];
            if (this.pool.get(book).challenge(word))
                result = true;
        }

        return result;
    }

    public int getSize() {
        return size;
    }

    public static DictionaryManager get() {
        /* Singletone */

        if (dictManInstance == null)
            dictManInstance = new DictionaryManager();
        return dictManInstance;
    }

    public Map<String, Dictionary> getDictionaries() {
        return new HashMap<>(pool);
    }

}
