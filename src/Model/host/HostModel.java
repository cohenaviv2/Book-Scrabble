package model.host;

import java.io.*;
import java.net.*;
import java.util.*;

import model.GameModel;
import model.game.*;
import model.game.Tile.Bag;
import model.server.MyServer;

public class HostModel extends Observable implements GameModel {

    private static HostModel hm = null; // Singleton

    // Connectivity :
    private Socket gameServer; // socket to the game server
    private MyServer hostServer; // my Host server - Will support connection of up to 3 guests

    // Profiles :
    private Player hostPlayer;
    private Map<Integer, Player> playersByID;
    private Map<String, Player> playersByName;

    // Game :
    Board gameBoard;
    Bag gameBag;
    StringBuilder bookList;

    private HostModel() {
        /* starts the host server on port 8040 */
        this.hostServer = new MyServer(8040, new GuestHandler());
        this.hostServer.start();
        System.out.println();
        this.gameBoard = Board.getBoard();
        this.gameBag = Tile.Bag.getBag();
        playersByID = new HashMap<>();
        playersByName = new HashMap<>();
    }

    public static HostModel getHM() {
        if (hm == null)
            hm = new HostModel();
        return hm;
    }

    public static int generateID() {
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
         * Connect the host to the game server via socket
         * game server is local for now, hence the ip field should be "localhost".
         * also set the host player profile
         */

        if (ip.equalsIgnoreCase("localhost")) {
            try {
                this.gameServer = new Socket(ip, port); // game server
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connectionTest()) { // Successfully completed
                this.hostPlayer = new Player(name, generateID(), true); // Sets host to true
                // PRINT DEBUG
                System.out.println(hostPlayer);
                System.out.println("HOST: " + name + " is Connected to the game server!");
            }
        }
    }

    private boolean connectionTest() {
        return true;
    }

    @Override
    public void myBookChoice(String bookName) {
        this.bookList.append("/resources/books/" + bookName + ",");
    }

    @Override
    public void tryPlaceWord(String word, int row, int col, boolean vertical) {
        Tile[] wordTiles = createTiles(word);
        Word queryWord = new Word(wordTiles, row, col, vertical);
        int score = gameBoard.tryPlaceWord(queryWord);

        if (score == -1) {
            // PRINT DEBUG
            System.out.println("HOST: your word is not board legal");
        } else if (score == 0) {
            // PRINT DEBUG
            System.out.println("HOST: some word that was made is not dictionary legal");
        } else {
            this.hostPlayer.addPoints(score);
            this.hostPlayer.getMyWords().addAll(gameBoard.getCurrentWords());
            pullTiles();
        }

    }

    @Override
    public void challenge(String word, int row, int col, boolean vertical) {

    }

    @Override
    public void pullTiles() {

        while (hostPlayer.getMyTiles().size() < 7) {
            Tile tile = gameBag.getRand();
            hostPlayer.getMyTiles().put(tile.getLetter(), tile);
        }
    }

    @Override
    public void skipTurn() {
        this.hostPlayer.setMyTurn(false);
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

    private Tile[] createTiles(String word) {
        Tile[] ts = new Tile[word.length()];
        int i = 0;
        for (char c : word.toCharArray()) {
            ts[i] = hostPlayer.getMyTiles().get(c);
            i++;
        }
        return ts;
    }

    public boolean dictionaryLegal(Word word) {
        /* TODO: need to ask the game server */

        return true;
    }

    @Override
    public String getMyName() {
        return this.hostPlayer.getName();
    }

    @Override
    public int getMyID() {
        return this.hostPlayer.getID();
    }

    @Override
    public int getMyScore() {
        return this.hostPlayer.getScore();
    }

    @Override
    public boolean isMyTurn() {
        return this.hostPlayer.isMyTurn();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        return null;
    }

    @Override
    public Map<Character, Tile> getMyTiles() {
        return this.hostPlayer.getMyTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return this.hostPlayer.getMyWords();
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }

    public Map<Integer, Player> getPlayersByID() {
        return playersByID;
    }

    public Map<String, Player> getPlayersByName() {
        return playersByName;
    }

    public Board getGameBoard() {
        return gameBoard;
    }

    public Bag getGameBag() {
        return gameBag;
    }

    public StringBuilder getBookList() {
        return bookList;
    }

}
