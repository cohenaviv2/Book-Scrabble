package app.model;

import java.io.*;
import java.util.*;

import app.model.game.*;

/*
 * An abstract model of the Book Scrabble Game
 * 
 * @author: Aviv Cohen
 * 
 */

public interface GameModel {

    // SEND DATA
    void connectMe(String name, String ip, int port);

    void myBooksChoice(String bookList); /* TODO: Change to List/Set */

    void ready();

    void tryPlaceWord(Word myWord);

    void challenge();

    void skipTurn();

    void quitGame();

    // GET DATA
    boolean isConnected();

    PlayerProperties getPlayerProperties();

    Tile[][] getCurrentBoard();

    ArrayList<Tile> getMyTiles();

    int getMyScore();

    ArrayList<Word> getMyWords();

    boolean isMyTurn();

    Map<String, String> getOthersInfo();

    Set<String> getGameBooks();

    int getBagCount();

    static Map<String, String> getFullBookList() {
        // initial all game Books from directory
        Map<String, String> fullBookList = new HashMap<>(); // book name to path
        File booksDirectory = new File("src\\main\\resources\\books");
        File[] txtFiles = booksDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles != null) {
            for (File file : txtFiles) {
                String fileName = file.getName().replaceAll(".txt", "");
                String gameServerPath = "server/books/" + fileName + ".txt"; // GAME SERVER PATH
                fullBookList.put(fileName, gameServerPath);
            }
        }
        return fullBookList;
    }

}
