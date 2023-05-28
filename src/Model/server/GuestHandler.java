package model.server;

import java.io.*;
import model.HostModel;
import model.logic.*;

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

        if (id == 0)
            return true;

        if (params.length != 3 || HostModel.getHM().getPlayersByID().get(id) == null
                || HostModel.getHM().getPlayersByID().get(id).getID() != id) { // No player exist
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
         * one request & answer
         */

        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            String request = in.readLine();

            if (idExist(request)) {
                String[] req = request.split(",");
                String guestID = req[0];
                String modifier = req[1];
                String guestValue = req[2];

                switch (modifier) {

                    // All model cases
                    case "connectMe":
                        connectHandler(guestID, guestValue);
                    case "myBookChoice":
                        addBookHandler(guestID, guestValue);
                    case "tryPlaceWord":
                        queryHandler(guestID, guestValue);
                    case "challenge":
                        challengeHandler(guestID, guestValue);
                    case "pullTiles":
                        pullTilesHandler(guestID, guestValue);
                    case "skipTurn":
                        skipTurnHandler(guestID, guestValue);
                    case "quitGame":
                        quitHandler(guestID, guestValue);
                    case "getMyName":
                        getNameHandler(guestID, guestValue);
                    case "getMyID":
                        getIdHandler(guestID, guestValue);
                    case "getMyScore":
                        scoreHandler(guestID, guestValue);
                    case "isMyTurn":
                        myTurnHandler(guestID, guestValue);
                    case "getCurrentBoard":
                        boardHandler(guestID, guestValue);
                    case "getMyTiles":
                        tilesHandler(guestID, guestValue);
                    case "getMyWords":
                        wordsHandler(guestID, guestValue);
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
        if (guestID.equalsIgnoreCase("0")) {
            HostModel.getHM().getPlayersByName().put(guestName, new Player(guestName, HostModel.generateID(), false));
            out.println("0,connectMe,true");
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " is Connected!\n");
        } else {
            // PRINT DEBUG
            System.out.println("HOST: failed to connect guest - " + guestName);
        }
    }

    private void addBookHandler(String guestID, String guestBook) {
    }

    private void queryHandler(String guestID, String wordParams) {
        String[] wordData = wordParams.split(":");
        String query = wordData[0];
        int row = Integer.parseInt(wordData[1]);
        int col = Integer.parseInt(wordData[2]);
        boolean vertical;
        if (wordData[3].equalsIgnoreCase("true")) {
            vertical = true;
        }
        else vertical = false;

        /********/
        Word word = new Word(null, row, col, vertical);
    }

    private void challengeHandler(String guestID, String challengeParams) {
    }

    private void pullTilesHandler(String guestID, String count) {
    }

    private void skipTurnHandler(String guestID, String bool) {
    }

    private void quitHandler(String guestID, String bool) {
    }

    private void getNameHandler(String guestID, String bool) {
    }

    private void getIdHandler(String guestID, String guestName) {
        if (guestID.equalsIgnoreCase("0")) {
            String id = String.valueOf(HostModel.getHM().getPlayersByName().get(guestName).getID());
            out.println("0,getMyID," + id);
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " requested is ID (" + id + ")\n");
        } else {
            // PRINT DEBUG
            System.out.println("HOST: failed to pass guest " + guestName + " his ID\n");
        }
    }

    private void scoreHandler(String guestID, String bool) {
    }

    private void myTurnHandler(String guestID, String bool) {
    }

    private void boardHandler(String guestID, String bool) {
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
