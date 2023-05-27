package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import model.logic.*;

public class GuestModel extends Observable implements GameModel {

    // Connectivity :
    private Socket hostSocket;
    private PrintWriter out;
    private Scanner in;

    // Profiles :
    private Player myPlayer;

    // Game :
    private Tile[][] myBoard;

    public GuestModel() {
        try {
            out = new PrintWriter(hostSocket.getOutputStream(), true);
            in = new Scanner(hostSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isValid(String answer, String indicator) {
        String[] params = answer.split(",");
        String myID = String.valueOf(getMyID());
        if (params.length != 3 || params[0] != myID || params[1] != indicator) {
            // PRINT DEBUG
            System.out.println("GUEST " + myPlayer.getName() + ": Protocol error");
            return false;
        }
        return true;
    }

    @Override
    public void connectMe(String myName, InetAddress ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        try {
            this.hostSocket = new Socket(ip, port);
            if (connect(myName)) { // Successfully completed
                this.myPlayer = new Player(myName, getMyID(), false); // Sets player with given ID from the host
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean connect(String name) {

        // Acknowledge test :
        out.println("0,connectMe," + name);
        String answer = in.nextLine();

        if (isValid(answer, "connectMe")) {
            String value = answer.split(",")[2];
            if (value == "true") {
                //PRINT DEBUG
                System.out.println("GUEST: Connections test passed successfully");
                return true;
            }
        }
        // PRINT DEBUG
        System.out.println("GUEST: Connections test failed - Unrecognized error");
        return false;

    }

    @Override
    public void tryPlaceWord(String word, int row, int col, boolean vertical) {
        /*
         * Informs the host that a query has been sent from this guest
         * host responds the score modifier
         */

        out.println(getMyID() + ",tryPlaceWord," + word + ":" + row + ":" + col + ":" + vertical);
        String answer = in.nextLine();

        if (isValid(answer, "tryPlaceWord")) {
            int modifier = Integer.parseInt(answer.split(",")[2]);
            switch (modifier) {

                case -1:
                    /* Not board legal */

                    // PRINT DEBUG
                    System.out.println("GUEST " + myPlayer.getName() + ": your word is not board legal");

                case 0:
                    /* Some word is not dictionary legal */

                    // PRINT DEBUG
                    System.out.println("GUEST " + myPlayer.getName() + ": some word placement is not dictionay legal ");
                default:

                    /*
                     * Placement earns points
                     * Update points, board, tiles
                     */

                    // this.myBoard = Board.getBoard().getTiles();
                    this.myPlayer.addPoints(modifier);
                    pullTiles(7 - myPlayer.getTiles().size());
                    setChanged();
                    notifyObservers();

            }
        }

    }

    @Override
    public void challenge(String word, int row, int col, boolean vertical) {
        /*
         * Informs the host that a challenge has been sent from this guest
         * host responds the challenge score modifier
         */

        out.println(getMyID() + ",challenge," + word + "," + row + "," + col + "," + vertical);
        String answer = in.nextLine();

        if (isValid(answer, "challenge")) {
            int score = Integer.parseInt(answer.split(",")[2]);

            if (score > 0) {

                /*
                 * Challenge was successful
                 * Update points, board, tiles
                 */

                this.myPlayer.addPoints(score);
                // this.myBoard = Board.getBoard().getTiles();
                pullTiles(7 - myPlayer.getTiles().size());
                setChanged();
                notifyObservers();

                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": The challenge was successful!");

            } else if (score < 0) {

                /*
                 * Challenge failed
                 * You lose points
                 */

                this.myPlayer.addPoints(score);
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": The challenge failed, you lose points");
            } else {

                /* Some error occurred */

                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": problem with challenge (maybe returned 0)");
            }
        }
    }

    @Override
    public void pullTiles(int count) {

        /* how we do it ??? */

    }

    @Override
    public void skipTurn() {
        /*
         * Informs the host that this guest want to skip turn
         * host responds true or false if operation was successful
         */

        out.println(getMyID() + ",skipTurn,true");
        String answer = in.nextLine();

        if (isValid(answer, "skipTurn")) {
            String value = answer.split(",")[2];
            if (value == "true") {
                this.myPlayer.setMyTurn(false);
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": Skip my turn");
            } else {
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": Some error occurred while skipTurn");
            }

        }
    }

    @Override
    public void myBookChoice(String bookName) {
        out.println(getMyID() + ",myBookChoice," + bookName);
        String answer = in.nextLine();

        if (isValid(answer, "myBookChoice")) {
            String value = answer.split(",")[2];
            if (value == "true") {
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": Selected book has been updated");
            } else {
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": Some error occurred while sending myBookChoice");
            }
        }
    }

    @Override
    public void quitGame() {
        /*
         * need to close all streams ....
         * close the communication with host ...
         */

    }

    @Override
    public String getMyName() {
        if (this.myPlayer != null) {
            return this.myPlayer.getName();
        } else
            return "null";
    }

    @Override
    public int getMyID() {
        if (this.myPlayer.getID() == 0) {

            // Send request for my ID :
            out.println("0,getMyID," + getMyName());
            String answer = in.nextLine();
            // Validation :
            if (isValid(answer, "getMyID")) {
                int id = Integer.parseInt(answer.split(",")[2]);
                return id;
            }
        }
        return this.myPlayer.getID();
    }

    @Override
    public int getMyScore() {

        // Send request for my ID :
        out.println(getMyID() + ",getMyScore,true");
        String answer = in.nextLine();

        // Validation :
        if (isValid(answer, "getMyScore")) {
            int score = Integer.parseInt(answer.split(",")[2]);
            return score;
        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myPlayer.getName() + ": problem with getMyScore");
            return -1;
        }
    }

    @Override
    public boolean isMyTurn() {
        // Send request for my ID :
        out.println(getMyID() + ",isMyTurn,true");
        String answer = in.nextLine();

        // Validation :
        if (isValid(answer, "isMyTurn")) {
            String bool = answer.split(",")[2];
            if (bool == "true") {
                this.myPlayer.setMyTurn(true);
                return true;
            } else {
                // PRINT DEBUG
                System.out.println("GUEST " + myPlayer.getName() + ": it is not my turn");
                return false;
            }

        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myPlayer.getName() + ": problem with isMyTurn");
            return false;
        }
    }

    @Override
    public Tile[][] getCurrentBoard() {

        /* how we do it ??? */

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentBoard'");
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        return this.myPlayer.getTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return this.myPlayer.getWords();
    }

}
