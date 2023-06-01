package model.host;

import java.io.*;

import model.game.*;
import model.server.ClientHandler;

/*
 * The Guest handler used to communicate the host and the guests
 * Communication is done using strings
 * 
 * HOST:
 * receives a string from the guest
 * starting with the guests ID,
 * then the Model method to active,
 * and then the value (like query word)
 * All 3 parameters seperated by ","
 * 
 * e.g. - "0,connectMe,Moshe" , "0,getMyID,Moshe" , "259874,tryPlaceWord,Hello"
 * (ID is 0 for initialization)
 * 
 * GUEST:
 * gets a string from the host
 * starting with his ID,
 * then the Model methos that was activated
 * and then the returned value
 * All 3 parameters seperated by ","
 * 
 * e.g. - "0,connectMe,true" , "0,getMyID,256874" , "259874,tryPlaceWord,32"
 * 
 * @author: Aviv Cohen
 * 
 */

public class GuestHandler implements ClientHandler {
    private BufferedReader in;
    private PrintWriter out;

    private boolean idExist(String request) {
        /*
         * Checks if the guest reqest is valid
         * if ID is 0 the guest trying to connect and get his ID
         */

        String[] params = request.split(",");

        int id = Integer.parseInt(params[0]);

        if (id == 0) {
            return true;
        }

        if (params.length != 3 || HostModel.getHM().getGameManager().getGstsByID().get(id) == null
                || HostModel.getHM().getGameManager().getGstsByID().get(id).getID() != id) { // No player exist
            // PRINT DEBUG
            System.out.println("HOST : Protocol error - invalid/no player exist\n");
            return false;
        }
        return true;
    }

    @Override
    public void handleClient(InputStream inFromclient, OutputStream outToClient) {
        /*
         * handles a client
         * one conversation
         * one request & one response
         */

        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            String request = in.readLine();
            System.out.println("client request: " + request); // PRINT DRBUG

            if (idExist(request)) {
                String[] req = request.split(",");
                String guestID = req[0];
                String modifier = req[1];
                String guestValue = req[2];

                System.out.println("client modifier: " + modifier); // PRINT DRBUG

                switch (modifier) {
                    // All model cases
                    case "connectMe":
                        connectHandler(guestID, guestValue);
                        break;
                    case "myBookChoice":
                        addBookHandler(guestID, guestValue);
                        break;
                    case "tryPlaceWord":
                        queryHandler(guestID, guestValue);
                        break;
                    case "challenge":
                        challengeHandler(guestID, guestValue);
                        break;
                    case "pullTiles":
                        pullTilesHandler(guestID, guestValue);
                        break;
                    case "skipTurn":
                        skipTurnHandler(guestID, guestValue);
                        break;
                    case "quitGame":
                        quitHandler(guestID, guestValue);
                        break;
                    case "getMyID":
                        getIdHandler(guestID, guestValue);
                        break;
                    case "getMyScore":
                        scoreHandler(guestID, guestValue);
                        break;
                    case "isMyTurn":
                        myTurnHandler(guestID, guestValue);
                        break;
                    case "getCurrentBoard":
                        boardHandler(guestID, guestValue);
                        break;
                    case "getMyTiles":
                        tilesHandler(guestID, guestValue);
                        break;
                    case "getMyWords":
                        wordsHandler(guestID, guestValue);
                        break;
                    default:
                        break;
                }
            } else {
                // PRINT DEBUG
                System.out.println("HOST: wrong model operator\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connectHandler(String guestID, String guestName) {
        if (guestID.equals("0")) {
            HostModel.getHM().getGameManager().createGuest(guestName);
            out.println("0,connectMe,true");
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " is Connected!\n");
        } else {
            out.println("0,connectMe,false");
            // PRINT DEBUG
            System.out.println("HOST: failed to connect guest - " + guestName);
        }
    }

    private void addBookHandler(String guestID, String guestBook) {
        HostModel.getHM().getGameManager().addBook("/resources/books/...");
        out.println(guestID + ",myBookChoice,true");
        // PRINT DEBUG
        System.out.println("HOST: " + HostModel.getHM().getGameManager().getGstsByID().get(Integer.parseInt(guestID)).getName()
                + " chose the book - " + guestBook + "\n");
    }

    private void queryHandler(String guestID, String wordParams) {
        /* TODO: turn active word, add points and such... */
        String[] wordData = wordParams.split(":");
        String query = wordData[0];
        int row = Integer.parseInt(wordData[1]);
        int col = Integer.parseInt(wordData[2]);
        boolean vertical;
        if (wordData[3].equalsIgnoreCase("true")) {
            vertical = true;
        } else
            vertical = false;

        /********/
        Word word = new Word(null, row, col, vertical);

        /*** tryPlaceWord, get score, set score, send score, update all ... */
    }

    private void challengeHandler(String guestID, String challengeParams) {

    }

    private void pullTilesHandler(String guestID, String count) {
        out.println(guestID + ",pullTiles,1324");

    }

    private void skipTurnHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            Player guest = HostModel.getHM().getGameManager().getGstsByID().get(Integer.parseInt(guestID));
            guest.setMyTurn(false);
            out.println(guestID + ",skipTurn,true");

            // PRINT DEBUG
            System.out.println("HOST: " + guest.getName() + " skipped turn\n");
        } else {
            // PRINT DEBUG
            System.out.println("HOST: " + guestID + " Skip turn failed\n");
        }
    }

    private void quitHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            String guestName = HostModel.getHM().getGameManager().getGstsByID().get(Integer.parseInt(guestID)).getName();
            HostModel.getHM().getGameManager().getGstsByID().remove(Integer.parseInt(guestID));
            out.println(guestID + ",quitGame,true");

            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " just quit the game\n");
        } else {
            // PRINT DEBUG
            System.out.println("HOST: " + guestID + " error quiting the game \n");
        }
    }

    private void getIdHandler(String guestID, String guestName) {
        System.out.println("getIdHandler");
        if (guestID.equals("0")) {
            String id = String.valueOf(HostModel.getHM().getGameManager().getGstByName().get(guestName).getID());
            out.println("0,getMyID," + id);
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " requested is ID (" + id + ")\n");
        } else {
            // PRINT DEBUG
            System.out.println("HOST: failed to pass guest " + guestName + " his ID\n");
        }
    }

    private void scoreHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            Player guest = HostModel.getHM().getGameManager().getGstsByID().get(Integer.parseInt(guestID));
            if (guest != null) {
                String score = Integer.toString(guest.getScore());
                out.println(guestID + ",getMyScore," + score);
                // PRINT DEBUG
                System.out.println("HOST: " + guest.getName() + " got " + score + " points \n");
            } else {
                // PRINT DEBUG
                System.out.println("HOST: no such player exist \n");
            }
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in getting score \n");
        }
    }

    private void myTurnHandler(String guestID, String bool) {
        if (bool.equals("true")) {
            Player guest = HostModel.getHM().getGameManager().getGstsByID().get(Integer.parseInt(guestID));
            if (guest != null) {
                out.println(guestID + ",isMyTurn," + guest.isMyTurn());
                // PRINT DEBUG
                System.out.println("HOST: " + guest.getName() + " turn: " + guest.isMyTurn() + " \n");
            } else {
                // PRINT DEBUG
                System.out.println("HOST: no such player exist \n");
            }
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in is my turn \n");
        }
    }

    private void boardHandler(String guestID, String bool) {
        /*
         * 1234,getCurrentBoard,true
         * 1234,getCurrentBoard,-----XGHB:
         */
        if (bool.equals("true")) {
            out.println(guestID + ",getCurrentBoard," + boardToString(HostModel.getHM().getCurrentBoard()));
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in getting board \n");
        }
    }

    private String boardToString(Character[][] charBoard) {
        String board = "";
        for (int i = 0; i < charBoard.length; i++) {
            for (int j = 0; j < charBoard.length; j++) {
                if (charBoard[i][j] == '-') {
                    board += '-';
                } else {
                    board += charBoard[i][j];
                }
            }
        }
        return board;
    }

    private void tilesHandler(String guestID, String bool) {
    }

    private void wordsHandler(String guestID, String bool) {
    }

    @Override
    public void close() {
        /* responsible to close the streams for this handler */

        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
