package model.game;

import java.util.*;

/*
 * A Player profile
 * includes name, ID, isHost, score
 * also contains list of current tiles and currnet words
 * and boolean my Turn
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class Player {
    private String name;
    private final int id;
    private final boolean isHost;
    private int score;
    private Map<Character,Tile> myTiles;
    private ArrayList<Word> myWords;
    private boolean myTurn;
    private int index;

    public Player(String name,int id, boolean host) {
        this.isHost = host;
        this.name = name;
        this. id = id;
        this.score = 0;
        this.myTiles = new HashMap<>();
        this.myWords = new ArrayList<>();
        this.myTurn = false;
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

    public Map<Character,Tile> getMyTiles() {
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

    public void setScore(int score) {
        this.score = score;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    @Override
    public String toString() {
        return "Player name: "+name+"\nId: "+id+"\nIs host: "+isHost+"\nScore: "+score+"\n";
    }

    /* LOCAL TEST */
 public static void main(String[] args) {
    Player a = new Player("aviv",1234,true);
    Player b = new Player("jacob",5678,false);
    Player c = new Player("tomer",9513,false);

    System.out.println(a);
    System.out.println(b);
    System.out.println(c);

 }   
}
