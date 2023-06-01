package model.host;

import java.io.*;
import java.net.*;
import java.util.*;
import model.GameModel;
import model.game.*;
import model.server.*;

public class HostModel extends Observable implements GameModel {

    private static HostModel hm = null; // Singleton
    private Socket gameServer; // socket to the game server
    private MyServer hostServer; // my Host server - Will support connection of up to 3 guests
    private GameManager gameManager; // game manager contains all the game information including players

    private HostModel() {
        /* starts the host server on port 8040 */
        this.hostServer = new MyServer(8040, new GuestHandler());
        this.hostServer.start();
        this.gameManager = new GameManager();
    }

    public static HostModel getHM() {
        if (hm == null)
            hm = new HostModel();
        return hm;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public static int generateID() {
        /*
         * Generates a unique ID for each player
         * only host can create player profiles and ID's
         */
        UUID idOne = UUID.randomUUID();
        String str = "" + idOne;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str) / 1000;
    }

    @Override
    public void connectMe(String name, String ip, int port) {
        /*
         * Connects the host to the game server via socket
         * game server is local for now, hence the ip field should be "localhost".
         * game server is responsible for checking whether a word is dirctionary legal
         * or not
         * also sets the host player profile
         */

        if (ip.equals("localhost")) {
            try {
                this.gameServer = new Socket(ip, port); // game server
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connectionTest()) { // Successfully completed
                this.gameManager.createHost(name);
                // PRINT DEBUG
                System.out.println(gameManager.getGstByName().get(name));
                System.out.println("HOST: " + name + " is Connected to the game server!");
            }
        }
    }

    private boolean connectionTest() {
        /* for now returns true */
        return true;
    }

    @Override
    public void myBookChoice(String bookName) {
        /*
         * Adds this book to the book list of the game
         * each player chooses one book (Maximum 4)
         */
        this.gameManager.addBook(bookName);
    }

    @Override
    public void tryPlaceWord(String word, int row, int col, boolean vertical) {
        /* TODO: create Word from this string and send it to Board.tryPlaceWord */
        Tile[] tiles = createTiles(word);
        Word queryWord = new Word(tiles, row, col, vertical);
        int score = this.gameManager.getBoard().tryPlaceWord(queryWord);

        if (score == -1) {

            // PRINT DEBUG
            System.out.println("HOST: your word is not board legal");
            
        } else if (score == 0) {

            /* Some word is not dictionary legal
             * player can try to challenge or skip turn
             * set the active word
             */

            this.gameManager.setActiveWord(word);
            this.gameManager.getHostPlayer().setActiveWord(true);
            
            // PRINT DEBUG
            System.out.println("HOST: some word that was made is not dictionary legal");

        } else {
            /*
             * Add points,
             * Add words,
             * Pull tiles,
             * Pull current board,
             * Change turn
             */
            this.gameManager.getHostPlayer().addPoints(score);
            this.gameManager.getHostPlayer().getMyWords().addAll(this.gameManager.getBoard().getCurrentWords());

            pullTiles();
        }

    }

    @Override
    public void challenge(String word, int row, int col, boolean vertical) {
        /* TODO: create Word from this string and send it to Board.tryPlaceWord */
        Tile[] tiles = createTiles(word);
        Word queryWord = new Word(tiles, row, col, vertical);
        // local check
        int score = this.gameManager.getBoard().tryPlaceWord(queryWord);

        if (score < 0) {
            /*
             * The challenge failed, the player loses points
             * TODO:
             * Add points(negative value)
             * Put back tiles
             * Change turn
             */

            // PRINT DEBUG
            System.out.println("HOST: challenge failed");
        } else {
            /*
             * The challenge was successful, the player gets extra points
             * TODO:
             * Add points
             * Add words
             * Pull tiles
             * Pull current board
             * Change turn
             */
            this.gameManager.getHostPlayer().addPoints(score);
            this.gameManager.getHostPlayer().getMyWords().addAll(this.gameManager.getBoard().getCurrentWords());

            pullTiles();
        }
    }

    @Override
    public void pullTiles() {
        /* Completes the player's hand to 7 Tiles after a successful placement */
        while (gameManager.getHostPlayer().getMyTiles().size() < 7) {
            Tile tile = gameManager.getBag().getRand();
            gameManager.getHostPlayer().getMyTiles().put(tile.getLetter(), tile);
        }
    }

    @Override
    public void skipTurn() {
        this.gameManager.getHostPlayer().setMyTurn(false);
    }

    @Override
    public void quitGame() {
        try {
            this.gameServer.close();
            this.hostServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getMyName() {
        return gameManager.getHostPlayer().getName();
    }

    @Override
    public int getMyID() {
        return gameManager.getHostPlayer().getID();
    }

    @Override
    public int getMyScore() {
        return gameManager.getHostPlayer().getScore();
    }

    @Override
    public boolean isMyTurn() {
        return gameManager.getHostPlayer().isMyTurn();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        return gameManager.getBoard().getTiles();
    }

    @Override
    public Map<Character, Tile> getMyTiles() {
        return gameManager.getHostPlayer().getMyTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return gameManager.getHostPlayer().getMyWords();
    }

    private Tile[] createTiles(String word) {
        /* Turns a string into a Tile's array */
        Tile[] ts = new Tile[word.length()];
        int i = 0;
        for (char c : word.toCharArray()) {
            /* */
            ts[i] = this.gameManager.getHostPlayer().getMyTiles().get(c);
            i++;
        }
        return ts;
    }

    public boolean dictionaryLegal(Word word) {
        /* TODO: need to ask the game server */

        return true;
    }

}
