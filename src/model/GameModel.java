package model;

import java.util.*;

import model.game.*;

public interface GameModel {

    // Send data :
    void connectMe(String name,String ip, int port);
    void myBookChoice(String bookName);
    void tryPlaceWord(String word,int row,int col, boolean vertical);
    void challenge(String word,int row,int col, boolean vertical);
    void pullTiles();
    void skipTurn();
    void quitGame();

    // Get data :
    String getMyName();
    int getMyID();
    int getMyScore();
    boolean isMyTurn();
    Character[][] getCurrentBoard();
    Map<Character,Tile> getMyTiles();
    ArrayList<Word> getMyWords();

}
