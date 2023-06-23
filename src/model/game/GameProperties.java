package model.game;

import java.util.*;

public class GameProperties {
    /* TODO: make class singleton */
    // Gameplay
    private final String myName;
    private int myScore;
    private boolean myTurn;
    private Tile[][] myBoard;
    private ArrayList<Tile> myHandTiled;
    private ArrayList<Word> myWords;
    private Map<String, Integer> playersScore;

    public GameProperties(String myName) {
        this.myName = myName;
        this.myScore = 0;
        this.myTurn = false;
        this.myBoard = new Tile[Board.SIZE][Board.SIZE];
        this.myHandTiled = new ArrayList<Tile>(7);
        this.myWords = new ArrayList<Word>();
        this.playersScore = new HashMap<>();
    }

    public void setMyScore(int score) {
        this.myScore = score;
    }

    public void addScore(int score) {
        this.myScore += score;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyBoard(Tile[][] myBoard) {
        this.myBoard = myBoard;
    }

    public void setMyTiles(ArrayList<Tile> tiles) {
        this.myHandTiled = tiles;
    }

    public void updatePlayersScore(String othersScore) {
        String[] scores = othersScore.split(":");
        for (String s : scores) {
            String[] params = s.split(",");
            this.playersScore.put(params[0], Integer.parseInt(params[1]));
        }
    }

    public String getMyName() {
        return myName;
    }

    public int getMyScore() {
        return myScore;
    }

    public Tile[][] getMyBoard() {
        return myBoard;
    }

    public ArrayList<Tile> getMyHandTiles() {
        return myHandTiled;
    }

    public ArrayList<Word> getMyWords() {
        return myWords;
    }

    public Map<String, Integer> getPlayersScore() {
        return playersScore;
    }

    public void setMyWords(ArrayList<Word> myWords) {
        this.myWords = myWords;
    }

    public void setPlayersScore(Map<String, Integer> playersScore) {
        this.playersScore = playersScore;
    }

    public String boardToString() {
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
        String info = "*************************************\n";
        info += "My name: " + myName + "\n";
        //info += "My id: " + myId + "\n";
        info += "My score: " + myScore + "\n";
        info += "My word :";
        if (myWords.size() != 0) {
            for (Word w : myWords) {
                info += wordToString(w) + ",";
            }
        } else {
            info += "0";
        }
        info += "\n*************************************\n";
        info += "My turn: " + isMyTurn() + "\n";
        info += "My Hand tiles: ";
        if (myHandTiled.size() != 0) {
            for (Tile t : myHandTiled) {
                info += t.getLetter() + ",";
            }
        } else {
            info += "0";
        }
        info += "\n*************************************\n";
        info += "Game Board: \n";
        info += boardToString();
        info += "\n*************************************\n";
        info += "Other players score: ";
        if (playersScore.size() != 0) {
            info += "\n";
            for (String name : playersScore.keySet()) {
                info += name + " - " + playersScore.get(name) + "\n";
            }
        } else {
            info += "0\n";
        }
        info += "*************************************\n";

        return info;
    }

}
