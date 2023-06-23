package model.game;

import java.util.*;

/*
 * Player profile
 * includes name, id, isHost, score
 * contains list of current tiles and all words created so far
 * also contains turn & active word indicators
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class Player {
    private final String name;
    private final int id;
    private final boolean isHost;
    private int score;
    private ArrayList<Tile> myHandTiles;
    private ArrayList<Word> myWords;
    private boolean myTurn;
    private int turnIndex;
    private boolean activeWord;

    public Player(String name, int id, boolean host) {
        this.isHost = host;
        this.name = name;
        this.id = id;
        this.score = 0;
        this.myHandTiles = new ArrayList<Tile>(7);
        this.myWords = new ArrayList<Word>();
        this.myTurn = false;
        this.turnIndex = 0;
        this.activeWord = false;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public ArrayList<Tile> getMyHandTiles() {
        return myHandTiles;
    }

    public ArrayList<Word> getMyWords() {
        return myWords;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void addPoints(int points) {
        this.score += points;
    }

    public void addWords(ArrayList<Word> turnWords) {
        this.myWords.addAll(turnWords);
    }
    
    public boolean isActiveWord() {
        return activeWord;
    }

    public void setIsActiveWord(boolean activeWord) {
        this.activeWord = activeWord;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public void setTurnIndex(int index) {
        this.turnIndex = index;
    }


    @Override
    public String toString() {
        return name + ", " + id + "\nis Host: " + isHost + "\nScore: " + score + "\n";
    }

}
