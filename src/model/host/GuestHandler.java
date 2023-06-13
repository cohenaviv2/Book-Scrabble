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

        if (params.length != 3 || HostModel.getHM().getGameManager().getPlayerByID(id) == null
                || HostModel.getHM().getGameManager().getPlayerByID(id).getID() != id) { // No player exist
            // PRINT DEBUG
            System.out.println("HOST : Protocol error - invalid/no player exist\n");
            return false;
        }
        return true;
    }

    // @Override
    // public void handleClient(InputStream inFromclient, OutputStream outToClient)
    // {
    // try {
    // in = new BufferedReader(new InputStreamReader(inFromclient));
    // out = new PrintWriter(outToClient, true);

    // // Perform initial communication with the client, if needed
    // out.println("Welcome to the game!");

    // String clientMessage;
    // while ((clientMessage = in.readLine()) != null) {

    // System.out.println("client request: " + clientMessage); // PRINT DRBUG

    // // if (!myTurn){
    // // //drop the message
    // // }

    // // if (quitGame){
    // // break;
    // // }

    // if (idExist(clientMessage)) {
    // // Parse the client message
    // String[] params = clientMessage.split(",");
    // String guestID = params[0];
    // String modifier = params[1];
    // String value = params[2];

    // // Process the client's instruction and invoke the method
    // String returnValue = processClientInstruction(guestID, modifier, value);

    // // Send the response to the client
    // String response = guestID + "," + modifier + "," + returnValue;
    // out.println(response);
    // }
    // }

    // // Closing the reader and writer
    // in.close();
    // out.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }

    // }

    @Override
    public void handleClient(InputStream inFromclient, OutputStream outToClient) {
        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            out.println("Hello from server");
            // Closing the reader and writer
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Example method for processing client instructions and invoking methods
    private String processClientInstruction(String guestID, String modifier, String guestValue) {
        // Handle the client instruction and invoke the corresponding method
        // You can implement your game logic here

        // Example: Echo the value back to the client
        System.out.println("client modifier: " + modifier); // PRINT DRBUG

        switch (modifier) {
            // All model cases
            case "connectMe":
                return connectHandler(guestID, guestValue);
            case "myBookChoice":
                return addBookHandler(guestID, guestValue);
            case "tryPlaceWord":
                return queryHandler(guestID, guestValue);
            case "challenge":
                return challengeHandler(guestID, guestValue);
            case "pullTiles":
                return pullTilesHandler(guestID, guestValue);
            case "skipTurn":
                return skipTurnHandler(guestID, guestValue);
            case "quitGame":
                return quitHandler(guestID, guestValue);
            case "getMyID":
                return getIdHandler(guestID, guestValue);
            case "getMyScore":
                return scoreHandler(guestID, guestValue);
            case "isMyTurn":
                return myTurnHandler(guestID, guestValue);
            case "getCurrentBoard":
                return boardHandler(guestID, guestValue);
            case "getMyTiles":
                return tilesHandler(guestID, guestValue);
            case "getMyWords":
                return wordsHandler(guestID, guestValue);
            default:
                // PRINT DEBUG
                System.out.println("HOST: wrong model operator\n");
                return null;
        }
    }

    private String connectHandler(String guestID, String guestName) {
        if (guestID.equals("0")) {
            // HostModel.getHM().getGameManager().createGuestPlayer(guestName);
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " is Connected!\n");
            return "true";
        } else {
            // PRINT DEBUG
            System.out.println("HOST: failed to connect guest - " + guestName);
            return "false";
        }
    }

    private String addBookHandler(String guestID, String guestBook) {
        HostModel.getHM().getGameManager().addBook("/resources/books/...");
        // PRINT DEBUG
        System.out.println(
                "HOST: " + HostModel.getHM().getGameManager().getPlayerByID(Integer.parseInt(guestID)).getName()
                        + " chose the book - " + guestBook + "\n");
        return "true";
    }

    private String queryHandler(String guestID, String wordParams) {
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

        return null;
    }

    private String challengeHandler(String guestID, String challengeParams) {
        return null;
    }

    private String pullTilesHandler(String guestID, String count) {
        return "true";
    }

    private String skipTurnHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            Player guest = HostModel.getHM().getGameManager().getPlayerByID(Integer.parseInt(guestID));
            guest.setMyTurn(false);
            // PRINT DEBUG
            System.out.println("HOST: " + guest.getName() + " skipped turn\n");
            return guestID + ",skipTurn,true";
        } else {
            // PRINT DEBUG
            System.out.println("HOST: " + guestID + " Skip turn failed\n");
            return guestID + ",skipTurn,false";
        }
    }

    private String quitHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            String guestName = HostModel.getHM().getGameManager().getPlayerByID(Integer.parseInt(guestID)).getName();
            // HostModel.getHM().getGameManager().getGstsByID().remove(Integer.parseInt(guestID));
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " just quit the game\n");
            return guestID + ",quitGame,true";
        } else {
            // PRINT DEBUG
            System.out.println("HOST: " + guestID + " error quiting the game \n");
            return guestID + ",quitGame,false";
        }
    }

    private String getIdHandler(String guestID, String guestName) {
        System.out.println("getIdHandler");
        if (guestID.equals("0")) {
            String id = String.valueOf(HostModel.getHM().getGameManager().getPlayerByName(guestName).getID());
            // PRINT DEBUG
            System.out.println("HOST: " + guestName + " requested is ID (" + id + ")\n");
            return "0,getMyID," + id;
        } else {
            // PRINT DEBUG
            System.out.println("HOST: failed to pass guest " + guestName + " his ID\n");
            return "0,getMyID,0";
        }
    }

    private String scoreHandler(String guestID, String bool) {
        if (bool.equals("true")) {

            Player guest = HostModel.getHM().getGameManager().getPlayerByID(Integer.parseInt(guestID));
            if (guest != null) {
                String score = Integer.toString(guest.getScore());
                // PRINT DEBUG
                System.out.println("HOST: " + guest.getName() + " got " + score + " points \n");
                return guestID + ",getMyScore," + score;
            } else {
                // PRINT DEBUG
                System.out.println("HOST: no such player exist \n");
                return null;
            }
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in getting score \n");
            return null;
        }
    }

    private String myTurnHandler(String guestID, String bool) {
        if (bool.equals("true")) {
            Player guest = HostModel.getHM().getGameManager().getPlayerByID(Integer.parseInt(guestID));
            if (guest != null) {
                // PRINT DEBUG
                System.out.println("HOST: " + guest.getName() + " turn: " + guest.isMyTurn() + " \n");
                return guestID + ",isMyTurn," + guest.isMyTurn();
            } else {
                // PRINT DEBUG
                System.out.println("HOST: no such player exist \n");
                return null;
            }
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in is my turn \n");
            return null;
        }
    }

    private String boardHandler(String guestID, String bool) {
        /*
         * 1234,getCurrentBoard,true
         * 1234,getCurrentBoard,-----XGHB:
         */
        if (bool.equals("true")) {
            return guestID + ",getCurrentBoard," + boardToString(HostModel.getHM().getCurrentBoard());
        } else {
            // PRINT DEBUG
            System.out.println("HOST: error in getting board \n");
            return null;
        }
    }

    private String boardToString(Tile[][] charBoard) {
        String board = "";
        for (int i = 0; i < charBoard.length; i++) {
            for (int j = 0; j < charBoard.length; j++) {
                if (charBoard[i][j].getLetter() == '-') {
                    board += '-';
                } else {
                    board += charBoard[i][j];
                }
            }
        }
        return board;
    }

    private String tilesHandler(String guestID, String bool) {
        return null;
    }

    private String wordsHandler(String guestID, String bool) {
        return null;
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
