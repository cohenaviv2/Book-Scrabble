package model.guest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import model.GameModel;
import model.game.*;

public class GuestModel extends Observable implements GameModel {

    // Connectivity :
    private Socket hostSocket;
    private PrintWriter out;
    private Scanner in;
    String ipString;
    int port;
    // Profile :
    String myName;
    int myId;
    int myScore;
    // Game :
    private Character[][] myBoard;
    private Character[] myTiles;

    public GuestModel() {
        this.myBoard = new Character[15][15];
        this.myTiles = new Character[7];
        this.myScore = 0;
    }

    private Character[][] fillBoard(String board) {
        /*
         * 
         * "1234,getBoard,-----X---G---GHJ:----GBH---"
         * 
         */
        return null;

    }

    private void openConversation() throws IOException {
        this.hostSocket = new Socket(this.ipString, this.port);
        this.out = new PrintWriter(hostSocket.getOutputStream(), true);
        this.in = new Scanner(hostSocket.getInputStream());

    }

    private void closeConversation() throws IOException {
        this.hostSocket.close();
        this.in.close();
        this.out.close();

    }

    private boolean isMyRequest(String answer, String indicator) {
        /*
         * Checks if the host respond intended for this ID
         * and has the same method indicator
         */

        String[] params = answer.split(",");
        int id = Integer.parseInt(params[0]);
        if (params.length != 3 || id != this.myId || !params[1].equalsIgnoreCase(indicator)) {
            // PRINT DEBUG
            System.out.println("GUEST " + myName + ": Protocol error\n");
            return false;
        }
        return true;
    }

    @Override
    public void connectMe(String myName, String ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */

        this.myName = myName;
        this.ipString = ip;
        this.port = port;

        if (connectTest()) { // Successfully completed
            try {
                openConversation();
                this.myId = getMyID();
                closeConversation();
                if (this.myId != 0) {
                    // PRINT DEBUG
                    System.out.println("GUSET " + myName + ": my Player profile is set up\n");
                } else {
                    // PRINT DEBUG
                    System.out.println("GUSET " + myName + ": error getting my id\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean connectTest() {

        try {
            openConversation();
            out.println("0,connectMe," + this.myName);
            String[] answer = in.nextLine().split(",");

            if (answer[1].equals("connectMe") && answer[2].equals("true")) {
                // PRINT DEBUG
                System.out.println("GUEST: Connections test passed successfully\n");
                closeConversation();
                return true;
            }
            closeConversation();
            // PRINT DEBUG
            System.out.println("GUEST: Connections test failed - Unrecognized error");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void myBookChoice(String bookName) {
        try {
            openConversation();
            out.println(getMyID() + ",myBookChoice," + bookName);
            String answer = in.nextLine();
            closeConversation();
            
            if (isMyRequest(answer, "myBookChoice")) {
                String value = answer.split(",")[2];
                if (value.equals("true")) {
                    // PRINT DEBUG
                    System.out.println("GUEST " + myName + ": Selected book has been updated");
                } else {
                    // PRINT DEBUG
                    System.out.println("GUEST " + myName + ": Some error occurred while sending myBookChoice");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tryPlaceWord(String word, int row, int col, boolean vertical) {
        /*
        * Informs the host that a query has been sent from this guest
        * host responds the score modifier
        */
        try {
            openConversation();
            out.println(getMyID() + ",tryPlaceWord," + word + ":" + row + ":" + col + ":" + vertical);
            String answer = in.nextLine();
            closeConversation();
            
            if (isMyRequest(answer, "tryPlaceWord")) {
                int scoreModifier = Integer.parseInt(answer.split(",")[2]);
    
                switch (scoreModifier) {
    
                    case -1:
                        /* Not board legal */
    
                        // PRINT DEBUG
                        System.out.println("GUEST " + myName + ": your word is not board legal\n");
    
                    case 0:
                        /* Some word is not dictionary legal */
    
                        // PRINT DEBUG
                        System.out
                                .println("GUEST " + myName + ": some word placement is not dictionay legal\n");
                    default:
    
                        /*
                         * Placement earns points
                         * Update points, board, tiles
                         */
    
                        // this.myBoard = Board.getBoard().getTiles();
                        this.myScore += scoreModifier; // Add points
                        // pullTiles(7 - myPlayer.getTiles().size()); // Pull tiles
    
                        // PRINT DEBUG
                        System.out.println("GUEST " + myName + ": You earned " + scoreModifier + "points\n");
                        // -----------//
                        setChanged();
                        notifyObservers();
    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void challenge(String word, int row, int col, boolean vertical) {
        /*
         * Informs the host that a challenge has been sent from this guest
         * host responds the challenge score modifier
         */
        
        try {
            openConversation();
            out.println(getMyID() + ",challenge," + word + ":" + row + ":" + col + ":" + vertical);
            String answer = in.nextLine();
            closeConversation();
    
            if (isMyRequest(answer, "challenge")) {
                int score = Integer.parseInt(answer.split(",")[2]);
    
                if (score > 0) {
    
                    /*
                     * Challenge was successful
                     * Update points, board, tiles
                     */
    
                    this.myScore += score;
                    // this.myBoard = Board.getBoard().getTiles();
                    // pullTiles(7 - myPlayer.getTiles().size());
    
                    // -----------//
                    setChanged();
                    notifyObservers();
    
                    // PRINT DEBUG
                    System.out.println("GUEST " + myName + ": The challenge was successful! You eraned " + score
                            + " points\n");
    
                } else if (score < 0) {
    
                    /*
                     * Challenge failed
                     * You lose points
                     */
    
                     this.myScore += score;
                    // PRINT DEBUG
                    System.out.println(
                            "GUEST " + myName + ": The challenge failed, you lost " + score + " points\n");
                } else {
    
                    /* Some error occurred */
    
                    // PRINT DEBUG
                    System.out.println("GUEST " + myName + ": problem with challenge (maybe returned 0)\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pullTiles() {

        /* how we do it ??? */

        /* TEST */
        try {
            openConversation();
            out.println("0,pullTiles,true");
            String answer = in.nextLine();
            // PRINT DEBUG
            System.out.println(answer);
            closeConversation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void skipTurn() {
        /*
         * Informs the host that this guest want to skip turn
         * host responds true or false if operation was successful
         */

        out.println(getMyID() + ",skipTurn,true");
        String answer = in.nextLine();

        if (isMyRequest(answer, "skipTurn")) {
            String value = answer.split(",")[2];
            if (value.equalsIgnoreCase("true")) {
                //this.myPlayer.setMyTurn(false);
                // PRINT DEBUG
                System.out.println("GUEST " + myName + ": Skip my turn\n");
            } else {
                // PRINT DEBUG
                System.out.println("GUEST " + myName + ": Some error occurred while skipTurn\n");
            }

        }
    }

    @Override
    public void quitGame() {
        /*
         * need to close all streams ....
         * close the communication with host ...
         */
        out.println(getMyID() + ",quitGame,true");
        String answer = in.nextLine();

        if (isMyRequest(answer, "quitGame")) {
            String value = answer.split(",")[2];
            if (value.equalsIgnoreCase("true")) {
                /********** */
            }
        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myName + ": error while quit the game");
        }

    }

    @Override
    public String getMyName() {
        if (this.myName != null) {
            return this.myName;
        } else
            return "myName is null";
    }

    @Override
    public int getMyID() {
        if (this.myId == 0) {

            // Send request for my ID :
            out.println("0,getMyID," + myName);
            String[] answer = in.nextLine().split(",");
            // Validation :
            if (answer[1].equalsIgnoreCase("getMyID")) {
                int id = Integer.parseInt(answer[2]);
                return id;
            }
        }
        return this.myId;
    }

    @Override
    public int getMyScore() {

        // Send request for my ID :
        out.println(getMyID() + ",getMyScore,true");
        String answer = in.nextLine();

        // Validation :
        if (isMyRequest(answer, "getMyScore")) {
            int score = Integer.parseInt(answer.split(",")[2]);
            return score;
        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myName + ": problem with getMyScore\n");
            return 0;
        }
    }

    @Override
    public boolean isMyTurn() {
        // Send request for my ID :
        out.println(getMyID() + ",isMyTurn,true");
        String answer = in.nextLine();

        // Validation :
        if (isMyRequest(answer, "isMyTurn")) {
            String bool = answer.split(",")[2];
            if (bool.equalsIgnoreCase("true")) {
                // PRINT DEBUG
                System.out.println("GUEST " + myName + ": it is My turn now!\n");
                //this.myPlayer.setMyTurn(true);
                return true;
            } else {
                // PRINT DEBUG
                System.out.println("GUEST " + myName + ": it is not my turn\n");
                return false;
            }

        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myName + ": problem with isMyTurn\n");
            return false;
        }
    }

    @Override
    public Character[][] getCurrentBoard() {
        try {
            openConversation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* how we do it ??? */
        // Send request for my ID :
        out.println(getMyID() + ",getCurrentBoard,true");
        String answer = in.nextLine();

        // Validation :
        if (isMyRequest(answer, "getCurrentBoard")) {
            String[] params = answer.split(",");
            myBoard = stringToBoard(params[2]);
            printBoard();
        } else {
            // PRINT DEBUG
            System.out.println("GUEST " + myName + ": problem with getMyScore\n");
        }
        try {
            closeConversation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return myBoard;
    }

    public Character[][] stringToBoard(String board) {
        Character[][] localBoard = new Character[15][15];
        for (int i = 0; i < localBoard.length; i++)
            for (int j = 0; j < localBoard.length; j++)
                localBoard[i][j] = board.charAt(15 * i + j);
        return localBoard;
    }

    public void printBoard() {
        /* prints the current board */
        for (int i = 0; i < myBoard.length; i++) {
            for (int j = 0; j < myBoard.length; j++) {
                if (myBoard[i][j] == '-')
                    System.out.print("- ");
                else
                    System.out.print(myBoard[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    @Override
    public Map<Character, Tile> getMyTiles() {
        /* TODO */
        return null;
    }

    @Override
    public ArrayList<Word> getMyWords() {
        /* TODO */
        return null;
    }

}
