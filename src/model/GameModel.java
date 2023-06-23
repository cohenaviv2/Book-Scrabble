package model;

import java.util.*;

import model.game.*;

/*
 * An abstract model of the Book Scrabble Game
 * 
 * @author: Aviv Cohen
 * 
 */

public interface GameModel{

    // SEND DATA
    void connectMe(String name, String ip, int port);

    void myBookChoice(String bookName);

    void ready();
    
    void tryPlaceWord(Word myWord);

    void challenge();

    void skipTurn();

    void quitGame();

    // GET DATA
    Tile[][] getCurrentBoard();
    
    ArrayList<Tile> getMyTiles();
    
    int getMyScore();
    
    ArrayList<Word> getMyWords();
    
    boolean isMyTurn();

    Map<String,Integer> getOthersScore();

}
