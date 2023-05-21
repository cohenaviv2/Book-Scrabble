package model;

import java.util.*;
import model.logic.*;

/*
 * ...........
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class Player {
    private String name;
    private final int id;
    int score;
    private Tile[] tiles;
    private ArrayList<Word> words;
    private boolean myTurn;

    public Player(String name) {
        this.name = name;
        this. id = generateID();
        this.score = 0;
        this.tiles = new Tile[7];
        this.words = new ArrayList<>();
        this.myTurn = false;
    }


    private int generateID() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    public boolean isMyTurn() {
        return myTurn;
    }


    public void setScore(int score) {
        this.score = score;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    
}
