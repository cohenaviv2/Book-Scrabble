package model;

import java.net.InetAddress;
import java.util.ArrayList;
import model.logic.*;

public interface GameModel {

    // Send data :
    void connectMe(String name,InetAddress ip, int port);
    void myBookChoice(String bookName);
    void query(String word);
    void challenge(String word);
    void pullTiles();
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
