package model;
import java.util.*;
import model.game.*;

public interface GameModel {

    // Send data :
    void connectMe(String name,String ip, int port);
    void myBookChoice(String bookName);
    void tryPlaceWord(Word myWord);
    void challenge();
    void skipTurn();
    void quitGame();
    
    // Get data :
    String getChanges();
    Tile[][] getCurrentBoard();
    int getMyScore();
    ArrayList<Tile> getMyTiles();
    ArrayList<Word> getMyWords();
    String getMyName();
    int getMyID();
    boolean isMyTurn();

}
