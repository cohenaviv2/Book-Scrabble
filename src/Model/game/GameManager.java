package model.game;

import java.util.*;
import model.game.Tile.Bag;
import model.host.*;

/*
 * The Game Manager maintains the game board, the bag of tiles, the books
 * and the player profile of each participant.
 * Contains a list of books chosen for this game where each player chooses one book (max 4)
 * Also contains turn and "Active word" indicators
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class GameManager {

    // Game:
    private Board gameBoard;
    private Bag bag;
    private Map<String, String> fullBookList;
    private StringBuilder gameBooks;
    private int turnIndex;
    private String activeWord;

    // Participants:
    private Player hostPlayer;
    private Map<Integer, Player> guestsByID;
    private Map<String, Player> guestsByName;

    public GameManager() {
        this.gameBoard = Board.getBoard();
        this.bag = Tile.Bag.getBag();
        /** TODO: initial game Books */
        this.fullBookList = new HashMap<>();
        // .....
        this.gameBooks = new StringBuilder();
        this.turnIndex = -1;
        this.guestsByID = new HashMap<>();
        this.guestsByName = new HashMap<>();
        this.activeWord = null;
    }

    public void createHost(String hostName) {
        int hostID = HostModel.generateID();
        this.hostPlayer = new Player(hostName, hostID, true);

    }

    public void createGuest(String guestName) {
        int guestID = HostModel.generateID();
        Player guest = new Player(guestName, guestID, false);
        this.guestsByID.put(guestID, guest);
        this.guestsByName.put(guestName, guest);
    }

    public void addBook(String bookName) {
        this.gameBooks.append(bookName);
    }

    public String getBookByName(String book) {
        return this.fullBookList.get(book);
    }

    public void setActiveWord(String activeWord) {
        this.activeWord = activeWord;
    }

    public void setTurnIndex(int turnIndex) {
        this.turnIndex = turnIndex;
    }

    public String getActiveWord() {
        return activeWord;
    }

    public Board getBoard() {
        return gameBoard;
    }

    public Bag getBag() {
        return bag;
    }

    public String getGameBooks() {
        return gameBooks.toString();
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }

    public Map<Integer, Player> getGstsByID() {
        return guestsByID;
    }

    public Map<String, Player> getGstByName() {
        return guestsByName;
    }

}
