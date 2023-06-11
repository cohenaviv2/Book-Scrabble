package model.game;

import java.io.*;
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

    private static GameManager gm = null; // Singleton

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
        File booksDirectory = new File("resources/books");
        File[] txtFiles = booksDirectory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (txtFiles != null) {
            for (File file : txtFiles) {
                String fileName = file.getName();
                String filePath = file.getPath();
                fullBookList.put(fileName, filePath);
            }
        }
        this.gameBooks = new StringBuilder();
        this.turnIndex = -1;
        this.guestsByID = new HashMap<>();
        this.guestsByName = new HashMap<>();
        this.activeWord = null;
    }

    public static GameManager getGM() {
        if (gm == null)
            gm = new GameManager();
        return gm;
    }

    public static int generateID() {
        /*
         * Generates a unique ID for each player
         * only Game Manager can create player profiles and ID's
         */
        UUID idOne = UUID.randomUUID();
        String str = "" + idOne;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str) / 1000;
    }

    public void createHostPlayer(String hostName) {
        int hostID = generateID();
        this.hostPlayer = new Player(hostName, hostID, true);
    }

    public void createGuestPlayer(String guestName) {
        int guestID = generateID();
        Player guest = new Player(guestName, guestID, false);
        this.guestsByID.put(guestID, guest);
        this.guestsByName.put(guestName, guest);
        while (guest.getMyTiles().size() < 7) {
            Tile t = bag.getRand();
            guest.getMyTiles().put(t.getLetter(), t);
        }
    }

    public void addBook(String bookName) {
        this.gameBooks.append(bookName);
    }

    public int connectMe(String firstLine) {
        String[] params = firstLine.split(",");
        if (params[0].equals("0") && params[1].equals("connectMe")) {
            String guestName = params[2];
            createGuestPlayer(guestName);
            // PRINT DEBUG
            System.out.println("GAME MANAGER: guest " + guestName + " profile was created!");
            return getGstByName(guestName).getID();
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: faild to connect guest");
            return 0;
        }
    }

    private boolean isIdExist(int id) {
        return this.guestsByID.get(id) != null || this.hostPlayer.getID() == id;
    }

    public String getBookByName(String book) {
        return this.fullBookList.get(book);
    }

    private void setActiveWord(String activeWord) {
        this.activeWord = activeWord;
    }

    private void setTurnIndex(int turnIndex) {
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

    public Player getGstsByID(int id) {
        return guestsByID.get(id);
    }

    public Player getGstByName(String name) {
        return guestsByName.get(name);
    }

    public String processGuestInstruction(int guestId, String modifier, String value) {
        if (isIdExist(guestId)) {
            // All model cases that indicates some guest instructions which not refers to a
            // game move
            switch (modifier) {
                case "myBookChoice":
                    return addBookHandler(value);
                case "getMyName":
                    return getNameHandler(guestId, value);
                case "getMyID":
                    return getIdHandler(guestId, value);
                case "getMyScore":
                    return scoreHandler(guestId, value);
                case "isMyTurn":
                    return myTurnHandler(guestId, value);
                case "getCurrentBoard":
                    return boardHandler(value);
                case "getMyTiles":
                    return tilesHandler(guestId, value);
                case "getMyWords":
                    return wordsHandler(guestId, value);
                default:
                    // PRINT DEBUG
                    System.out.println("GAME MANAGER: wrong instructions operator - " + modifier);
                    return null;
            }
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: id do not exist");
            return null;
        }
    }

    private String wordsHandler(int guestId, String value) {
        if (value.equals("true")) {
            String words = "";
            if (gameBoard.getCurrentWords().size() == 0)
                return "false";
            for (Word w : gameBoard.getCurrentWords()) {
                words += w.toString() + ":";
            }
            return words;
        } else
            return "false";
    }

    private String tilesHandler(int guestId, String value) {
        if (value.equals("true")) {
            String tiles = "";
            for (char c : getGstsByID(guestId).getMyTiles().keySet()) {
                tiles += c + ":";
            }
            return tiles;
        } else
            return "false";
    }

    private String boardHandler(String value) { // 1234.getCurrentBoard,true //1234,getCurrentBoard,Stringborard
        if (value.equals("true")) {
            return gameBoard.toString();
        } else
            return "false";
    }

    private String myTurnHandler(int guestId, String value) {
        if (value.equals("true") && getGstsByID(guestId).isMyTurn())
            return "true";
        else
            return "false";
    }

    private String scoreHandler(int guestId, String value) {
        if (value.equals("true")) {
            return String.valueOf(getGstsByID(guestId).getScore());
        } else
            return "false";
    }

    private String getIdHandler(int guestId, String value) {
        if (value.equals("true")) {
            return String.valueOf(getGstsByID(guestId).getID());
        } else
            return "false";
    }

    public void quitGameHandler(String quitGameID) {
        String[] params = quitGameID.split(",");
        int id = Integer.parseInt(params[0]);
        String quitMod = params[1];
        String bool = params[2];
        if (isIdExist(id) && quitMod.equals("quitGame") && bool.equals("true")) {
            Player p = this.guestsByID.get(id);
            // put back player tiles
            for (Tile t : p.getMyTiles().values()) {
                this.bag.put(t);
            }
            this.guestsByID.remove(id);
            this.guestsByName.remove(p.getName());
        }
    }

    private String addBookHandler(String value) {
        String bookPath;
        if ((bookPath = fullBookList.get(value)) != null) {
            addBook(bookPath);
            return "true";
        }
        return "false";
    }

    private String getNameHandler(int guestId, String value) {
        if (value.equals("true")) {
            return getGstsByID(guestId).getName();
        } else
            return "false";
    }

    public String processGuestMove(String moveModifier, String moveValue) {

        switch (moveModifier) {
            // All model cases that indicates a possible guest game move
            case "tryPlaceWord":
                return queryHandler(moveValue);
            case "challenge":
                return challengeHandler(moveValue);
            case "pullTiles":
                return pullTilesHandler(moveValue);
            case "skipTurn":
                return skipTurnHandler(moveValue);
            default:
                // PRINT DEBUG
                System.out.println("GAME MANAGER: wrong game move operator");
                return null;
        }
    }

    private String skipTurnHandler(String moveValue) {
        return null;
    }

    private String pullTilesHandler(String moveValue) {
        return null;
    }

    private String challengeHandler(String moveValue) {
        /*
         * TODO:
         * if Active word is NOT activated - can not try challange this word.
         * need to ask the game sever to challenge this word -
         * if server return true - need to go board.tryPlaceWord() - if all words is
         * dictionary legal need to updated all stated and add some extra points, if not dic legal - skip turn
         * if server return false - need to drop players points and update all states
         * Any way need to turn off Active word.
         * 
         */
        return null;
    }

    private String queryHandler(String moveValue) {
        /*
         * TODO:
         * if Active word is activated - can not try place this word.
         * need to check if the guest has all the currect tiles and make the string word
         * to a Word object
         * go to board.tryPlaceWord() - if not board legal(try again), else set Active
         * word
         * if word was dictionary legal : updated score, updated words, pull tiles,
         * change turn, turn off active word,
         * notifyAll (turn index, get currectBoard, players points and such...)
         * if the word wasnt dictionary legal : need to inform the player that some word
         * that was created is not dictionary legal and the player can try to challenge
         * or skip turn.
         */
        return null;
    }

    public static void main(String[] args) {
        GameManager g = new GameManager();
        System.out.println("0");
    }
}
