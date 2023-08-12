package app.model;

import java.io.IOException;
import java.util.*;

import app.model.game.*;

/*
 * An abstract model of the Book Scrabble Game
 * 
 * @author: Aviv Cohen
 * 
 */

public interface GameModel{

    // SEND DATA
    void connectMe(String name, String ip, int port) throws IOException;

    void myBooksChoice(String bookName);

    void ready();
    
    void tryPlaceWord(Word myWord);

    void challenge();

    void skipTurn();

    void quitGame();

    // GET DATA
    PlayerProperties getPlayerProperties();

    Tile[][] getCurrentBoard();
    
    ArrayList<Tile> getMyTiles();
    
    int getMyScore();
    
    ArrayList<Word> getMyWords();
    
    boolean isMyTurn();

    Map<String,String> getOthersInfo();

}
