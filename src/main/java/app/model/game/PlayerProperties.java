package app.model.game;

import java.util.*;

/*
 * Represents all of the player's proerties as it will appear on the view side
 * Each player, whether host or guest, has game properties
 * 
 * @author: Aviv Cohen
 * 
 */

public class PlayerProperties {
    private static PlayerProperties instance = null;
    private String myName;
    private int myScore;
    private boolean myTurn;
    private Tile[][] myBoard;
    private ArrayList<Tile> myHandTiles;
    private ArrayList<Word> myWords;
    private Map<String, String> otherPlayersInfo;
    private Set<String> gameBookList;
    private int bagCount;

    public static PlayerProperties get() {
        if (instance == null) {
            instance = new PlayerProperties();
        }
        return instance;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public void setMyScore(int score) {
        this.myScore = score;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public void setMyBoard(Tile[][] myBoard) {
        this.myBoard = myBoard;
    }

    public void setMyTiles(ArrayList<Tile> tiles) {
        this.myHandTiles = tiles;
    }

    public void setMyWords(ArrayList<Word> myWords) {
        this.myWords = myWords;
    }

    public void setPlayersInfo(Map<String, String> playersScore) {
        this.otherPlayersInfo = playersScore;
    }

    public void setGameBookList(Set<String> gameBookList) {
        this.gameBookList = gameBookList;
    }

    public void setBagCount(int bagCnt) {
        this.bagCount = bagCnt;
    }

    public String getMyName() {
        return myName;
    }

    public int getMyScore() {
        return myScore;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public Tile[][] getMyBoard() {
        return myBoard;
    }

    public ArrayList<Tile> getMyHandTiles() {
        return myHandTiles;
    }

    public ArrayList<Word> getMyWords() {
        return myWords;
    }

    public Map<String, String> getOtherPlayersInfo() {
        return otherPlayersInfo;
    }

    public Set<String> getGameBookList() {
        return gameBookList;
    }

    public int getBagCount() {
        return bagCount;
    }

    private String boardToString() {
        String board = "";
        for (int i = 0; i < this.myBoard.length; i++) {
            for (int j = 0; j < this.myBoard.length; j++) {
                if (this.myBoard[i][j] == null)
                    board += "- ";
                else
                    board += this.myBoard[i][j].getLetter() + " ";
            }
            board += "\n";
        }
        board += "\n";
        return board;
    }

    public String wordToString(Word word) {
        String w = "";
        for (Tile t : word.getTiles()) {
            w += t.getLetter();
        }
        return w;
    }

    @Override
    public String toString() {
        String info = "**********PLAYER PROPERTIES***********\n";
        info += "My name: " + myName + "\n";
        // info += "My id: " + myId + "\n";
        info += "My score: " + myScore + "\n";
        info += "My word :";
        if (myWords.size() != 0) {
            for (Word w : myWords) {
                info += wordToString(w) + ",";
            }
        } else {
            info += "0";
        }
        info += "\n__________________________________\n";
        info += "My turn: " + isMyTurn() + "\n";
        info += "My Hand tiles: ";
        if (myHandTiles.size() != 0) {
            for (Tile t : myHandTiles) {
                info += t.getLetter() + ",";
            }
        } else {
            info += "0";
        }
        info += "\n__________________________________\n";
        info += "Game Board: \n";
        info += boardToString();
        info += "\n__________________________________\n";
        info += "Other players score: ";
        if (otherPlayersInfo.size() != 0) {
            info += "\n";
            for (String name : otherPlayersInfo.keySet()) {
                info += name + " - " + otherPlayersInfo.get(name) + "\n";
            }
        } else {
            info += "0\n";
        }
        info += "__________________________________\n";
        info+="Bag Count: "+getBagCount()+"\n";
        info+="Books: \n";
        for(String b : getGameBookList()){
            info+=b+"\n";
        }

        return info;
    }

}
