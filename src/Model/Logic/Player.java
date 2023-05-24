package model.logic;

import java.util.*;

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
    private ArrayList<Tile> myTiles;
    private ArrayList<Word> myWords;
    private boolean myTurn;

    public Player(String name) {
        this.name = name;
        this. id = generateID();
        this.score = 0;
        this.myTiles = new ArrayList<>();
        this.myWords = new ArrayList<>();
        this.myTurn = false;
    }


    private int generateID() {
        UUID idOne = UUID.randomUUID();
        String str=""+idOne;        
        int uid=str.hashCode();
        String filterStr=""+uid;
        str=filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
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

    public ArrayList<Tile> getTiles() {
        return myTiles;
    }

    public ArrayList<Word> getWords() {
        return myWords;
    }

    public boolean isMyTurn() {
        return myTurn;
    }


    public void setScore(int score) {
        this.score = score;
    }

    public void setTiles(ArrayList<Tile> tiles) {
        this.myTiles = tiles;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

 public static void main(String[] args) {
    Player a = new Player("aviv");
    Player b = new Player("jacob");
    Player c = new Player("tomer");

    System.out.println(a.getId());
    System.out.println(b.getId());
    System.out.println(c.getId());

 }   
}
