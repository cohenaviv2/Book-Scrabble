package app.model.server;

import java.util.*;

import app.model.game.*;
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
        //
        System.out.println(word);
        //
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

        //
        System.out.println(result);
        //
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
        //
        System.out.println(word);
        //
        boolean result = false;

        for (int i = 0; i < args.length - 1; i++) {
            String book = args[i];
            if (this.pool.get(book).challenge(word))
                result = true;
        }

        //
        System.out.println(result);
        //
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

    public static void main(String[] args) {
        DictionaryManager d = DictionaryManager.get();
        GameManager gm = GameManager.get();
        MyServer s = new MyServer(11224, new BookScrabbleHandler());
        s.start();
        try {
        gm.setGameServerSocket("localhost", 11224);
        gm.setTotalPlayersCount(1);
        int id = gm.connectGuestHandler("Aviv");
        gm.addBookHandler("Harray Potter.txt");
        gm.addBookHandler("alice_in_wonderland.txt");
        gm.addBookHandler("Frank Herbert - Dune.txt");
        gm.addBookHandler("mobydick.txt");
        gm.addBookHandler("pg10.txt");
        gm.addBookHandler("shakespeare.txt");
        gm.addBookHandler("The Matrix.txt");
        gm.setReady();
        
        Tile[] tiles = new Tile[6];
        tiles[0] = gm.getGameBag().getTile('S');
        tiles[1] = gm.getGameBag().getTile('I');
        tiles[2] = gm.getGameBag().getTile('H');
        tiles[3] = gm.getGameBag().getTile('A');
        tiles[4] = gm.getGameBag().getTile('Y');
        tiles[5] = gm.getGameBag().getTile('A');
        Word word = new Word(tiles, 7, 7, true);
            String w = ObjectSerializer.serializeObject(word);
            System.out.println(gm.processPlayerInstruction(id, "tryPlaceWord", w));;
            System.out.println(gm.processPlayerInstruction(id, "challenge", "true"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Dictionary dic = new Dictionary("resources/books/Harray Potter.txt");
        // System.out.println("bb
        // "+"PETUNIA".toLowerCase().equalsIgnoreCase("Petunia"));
        boolean res = d.query("resources/books/Harray Potter.txt", "resources/books/alice_in_wonderland.txt",
                "resources/books/Frank Herbert - Dune.txt", "resources/books/mobydick.txt", "resources/books/pg10.txt",
                "resources/books/shakespeare.txt", "resources/books/The Matrix.txt", "Sihaya");
        boolean challange = d.challenge("resources/books/Harray Potter.txt", "resources/books/alice_in_wonderland.txt",
                "resources/books/Frank Herbert - Dune.txt", "resources/books/mobydick.txt", "resources/books/pg10.txt",
                "resources/books/shakespeare.txt", "resources/books/The Matrix.txt", "Sihaya");
        System.out.println(res);
        System.out.println(challange);
    }
}
