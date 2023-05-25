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

    @Override
    public void connectMe(String name, InetAddress ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        try {
            this.hostSocket = new Socket(ip, port);
            if (connectionTest()) { // Successfully completed
                this.myPlayer = new Player(name, getMyID(), false); // Sets player with given ID from the host
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean connectionTest() {

        // Acknowledge test :
        out.println("0,connectMe,true");
        String answer = in.nextLine();

        if (isValid(answer, "connectMe")) {
            String value = answer.split(",")[2];
            if (value == "true") {
                System.out.println("GUEST: Connections test passed successfully");
                return true;
            }
        }
        // PRINT DEBUG
        System.out.println("GUEST: Connections test failed - Unrecognized error");
        return false;

    }

    @Override
    public void query(String word) {
        /*
         * Informs the host that a query has been sent from this guest
         * host responds the score modifier
         */

        out.println(getMyID() + ",query," + word);
        String answer = in.nextLine();

        if (isValid(answer, "query")) {
            int modifier = Integer.parseInt(answer.split(",")[2]);
            switch (modifier) {

                case -1:
                    // Not board legal

                case 0:
                    // Some word is not dictionary legal

                default:
                    // Placement earns points
                    // Update points, board, tiles
                    this.myBoard = Board.getBoard().getTiles();
                    this.myPlayer.addPoints(modifier);
                    pullTiles();
                    setChanged();
                    notifyObservers();

            }
        }

    }

    @Override
    public void challenge(String word) {
        /*
         * Informs the host that a challenge has been sent from this guest
         * host responds the challenge score modifier
         */

        out.println(getMyID() + ",challenge," + word);
        String answer = in.nextLine();

        if (isValid(answer, "challenge")) {
            int score = Integer.parseInt(answer.split(",")[2]);

            if (score > 0) {

                // Challenge was successful
                // Update points, board, tiles

            } else if (score < 0) {

                // Challenge failed

            } else {

                // Some error occurred

            }
        }
    }

    @Override
    public void pullTiles() {
       
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
    public void myBookChoice(String bookName) {
        out.println(getMyID()+",myBookChoice,"+bookName);
        String answer = in.nextLine();

        if(isValid(answer, "myBookChoice")){
            String value = answer.split(",")[2];
            if (value == "true"){
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
        return this.myPlayer.getName();
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyScore'");
    }

    @Override
    public boolean isMyTurn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isMyTurn'");
    }

    @Override
    public Tile[][] getCurrentBoard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentBoard'");
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyTiles'");
    }

    @Override
    public ArrayList<Word> getMyWords() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyWords'");
    }

}
