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
    private String name;
    private final int id;
    private final boolean isHost;
    private int score;
    private Map<Character, Tile> myTiles;
    private ArrayList<Word> myWords;
    private boolean myTurn;
    private int turnIndex;
    private boolean activeWord;

    public Player(String name, int id, boolean host) {
        this.isHost = host;
        this.name = name;
        this.id = id;
        this.score = 0;
        this.myTiles = new HashMap<>();
        this.myWords = new ArrayList<>();
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

    public Map<Character, Tile> getMyTiles() {
        return myTiles;
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

    public boolean isActiveWord() {
        return activeWord;
    }

    public void setActiveWord(boolean activeWord) {
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
        /* TODO: add more information */
        return name+", "+id+"\nis Host: " + isHost + "\nScore: " + score + "\n";
    }

}
