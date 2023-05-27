package model;

import java.io.IOException;
import java.net.*;
import java.util.*;
import model.logic.*;
import model.logic.Tile.Bag;
import model.server.*;

public class HostModel extends Observable implements GameModel {

    private static HostModel hm = null; // Singleton

    // Connectivity :
    private Socket gameServer;
    private MyServer hostServer; // Will support connection of up to 3 guests

    // Profiles :
    private Player hostPlayer;
    private Map<Integer, Player> playersByID;
    private Map<String, Player> playersByName;

    // Game :
    Board gameBoard;
    Bag gameBag;
    StringBuilder bookList;

    public HostModel() {
        this.hostServer = new MyServer(8040, new GuestHandler());
        this.gameBoard = Board.getBoard();
        this.gameBag = Tile.Bag.getBag();
        playersByID = new HashMap<>();
        playersByName = new HashMap<>();
    }

    public static HostModel getHostModel() {
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
    public void connectMe(String name, InetAddress ip, int port) {
        /*
         * Connects the host to the game server via socket
         * game server is local for now, hence the ip field should be null.
         * Also sets the host player profile
         */

        if (ip == null) {
            try {
                this.gameServer = new Socket("localhost", port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connectionTest()) { // Successfully completed
                this.hostPlayer = new Player(name, generateID(), true); // Sets host to true
            }
        }
    }

    private boolean connectionTest() {
        return true;
    }

    @Override
    public void query(String word) {

    }

    @Override
    public void challenge(String word) {

    }

    @Override
    public void pullTiles() {

    }

    @Override
    public void skipTurn() {
        this.hostPlayer.setMyTurn(false);
    }

    @Override
    public void myBookChoice(String bookName) {
        this.bookList.append("/resources/books/" + bookName + ",");
    }

    @Override
    public void quitGame() {

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
    public ArrayList<Tile> getMyTiles() {
        return this.hostPlayer.getTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return this.hostPlayer.getWords();
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
