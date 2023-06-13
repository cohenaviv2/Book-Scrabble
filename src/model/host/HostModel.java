package model.host;

import model.GameModel;
import model.game.*;
import model.server.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class HostModel extends Observable implements GameModel {

    private static final int HOST_PORT = 8040;

    private static HostModel hm = null; // Singleton

    private MyServerParallel hostServer; // my Host server - Will support connection of up to 3 guests
    private GameManager gameManager; // game manager contains all the game information including players

    private HostModel() {
        /* starts the host server on port 8040 */
        this.hostServer = new MyServerParallel(HOST_PORT, new GuestHandler2());
        this.hostServer.start();
        this.gameManager = GameManager.getGM();
    }

    public static HostModel getHM() {
        if (hm == null)
            hm = new HostModel();
        return hm;
    }

    @Override
    public void connectMe(String name, String ip, int port) {
        /*
         * Connects the host to the game server via socket
         * game server is local for now, hence the ip field should be "localhost".
         * game server is responsible for checking whether a word is dirctionary legal
         * or not
         * also sets the host player profile
         */

        if (ip.equals("localhost")) {
            // try {
            // this.gameServerSocket = new Socket(ip, port); // game server
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            if (connectionTest()) { // Successfully completed
                this.gameManager.createHostPlayer(name);
                // PRINT DEBUG
                System.out.println(gameManager.getHostPlayer());
                System.out.println("HOST: " + name + " is Connected to the game server!");
            }
        }
    }

    private boolean connectionTest() {
        /* for now returns true */
        return true;
    }

    @Override
    public void myBookChoice(String bookName) {
        /*
         * Adds this book to the book list of the game
         * each player chooses one book (Maximum 4)
         */
        this.gameManager.addBook(bookName);
    }

    @Override
    public void tryPlaceWord(Word myWord) {
        // /* TODO: create Word from this string and send it to Board.tryPlaceWord */
        // Tile[] tiles = createTiles(word);
        // Word queryWord = new Word(tiles, row, col, vertical);
        // int score = this.gameManager.getBoard().tryPlaceWord(queryWord);

        // if (score == -1) {

        // // PRINT DEBUG
        // System.out.println("HOST: your word is not board legal");

        // } else if (score == 0) {

        // /*
        // * Some word is not dictionary legal
        // * player can try to challenge or skip turn
        // * set the active word
        // */

        // // this.gameManager.setActiveWord(word);
        // this.gameManager.getHostPlayer().setActiveWord(true);

        // // PRINT DEBUG
        // System.out.println("HOST: some word that was made is not dictionary legal");

        // } else {
        // /*
        // * Add points,
        // * Add words,
        // * Pull tiles,
        // * Pull current board,
        // * Change turn
        // */
        // this.gameManager.getHostPlayer().addPoints(score);
        // this.gameManager.getHostPlayer().getMyWords().addAll(this.gameManager.getBoard().getCurrentWords());

        // pullTiles();
        // }

    }

    @Override
    public void challenge() {
        // /* TODO: create Word from this string and send it to Board.tryPlaceWord */
        // //Tile[] tiles = createTiles(word);
        // Word queryWord = new Word(tiles, row, col, vertical);
        // // local check
        // int score = this.gameManager.getBoard().tryPlaceWord(queryWord);

        // if (score < 0) {
        // /*
        // * The challenge failed, the player loses points
        // * TODO:
        // * Add points(negative value)
        // * Put back tiles
        // * Change turn
        // */

        // // PRINT DEBUG
        // System.out.println("HOST: challenge failed");
        // } else {
        // /*
        // * The challenge was successful, the player gets extra points
        // * TODO:
        // * Add points
        // * Add words
        // * Pull tiles
        // * Pull current board
        // * Change turn
        // */
        // this.gameManager.getHostPlayer().addPoints(score);
        // this.gameManager.getHostPlayer().getMyWords().addAll(this.gameManager.getBoard().getCurrentWords());

        // pullTiles();
        // }
    }

    @Override
    public void skipTurn() {
        this.gameManager.getHostPlayer().setMyTurn(false);
    }

    @Override
    public void quitGame() {
        this.hostServer.close();
        this.gameManager.close();
    }

    @Override
    public String getChanges() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChanges'");
    }

    @Override
    public String getMyName() {
        return gameManager.getHostPlayer().getName();
    }

    @Override
    public int getMyID() {
        return gameManager.getHostPlayer().getID();
    }

    @Override
    public int getMyScore() {
        return gameManager.getHostPlayer().getScore();
    }

    @Override
    public boolean isMyTurn() {
        return gameManager.getHostPlayer().isMyTurn();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        Character[][] charBoard = new Character[15][15];
        for (int i = 0; i < charBoard.length; i++) {
            for (int j = 0; j < charBoard.length; j++) {
                if (charBoard[i][j] == null) {
                    charBoard[i][j] = '-';
                } else {
                    //charBoard[i][j] = this.gameManager.getBoard().getTiles()[i][j].letter;
                }
            }
        }
        return null;
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        return gameManager.getHostPlayer().getMyHandTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return gameManager.getHostPlayer().getMyWords();
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

}
