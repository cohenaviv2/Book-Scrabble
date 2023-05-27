package model;

import java.net.InetAddress;
import java.util.ArrayList;
import model.logic.*;

public interface GameModel {

    // Send data :
    void connectMe(String name,InetAddress ip, int port);
    void myBookChoice(String bookName);
    void tryPlaceWord(String word,int row,int col, boolean vertical);
    void challenge(String word,int row,int col, boolean vertical);
    void pullTiles(int count);
    void skipTurn();
    void quitGame();

    // Get data :
    String getMyName();
    int getMyID();
    int getMyScore();
    boolean isMyTurn();
    Tile[][] getCurrentBoard();
    ArrayList<Tile> getMyTiles();
    ArrayList<Word> getMyWords();

}
