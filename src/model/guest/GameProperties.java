package model.guest;

import java.util.*;

import model.game.Tile;
import model.game.Word;

public class GameProperties {
    // Gameplay
    private final String myName;
    private int myId;
    private int myScore;
    private Tile[][] myBoard;
    private Tile[] myHandTiled;
    private ArrayList<Word> myWords;

    public GameProperties(String myName) {
        this.myName = myName;
        this.myScore = 0;
    }
    
    public void setMyId(int id){
        this.myId = id;
    }

    public void setMyScore(int myScore) {
        this.myScore = myScore;
    }

    public void setMyBoard(Tile[][] myBoard) {
        this.myBoard = myBoard;
    }

    public void addTiles(Tile[] tile, int count) {
    }


    
}
