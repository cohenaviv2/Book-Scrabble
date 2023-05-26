package model.server;

import java.io.*;

import model.HostModel;

public class GuestHandler implements ClientHandler {
    HostModel hm = HostModel.getHostModel();
    BufferedReader in;
    PrintWriter out;

    private boolean isValid(String request) {
        /*
         * Checks if the guest reqest is valid
         */

        String[] params = request.split(",");

        if (params.length != 3) {
            // PRINT DEBUG
            System.out.println("HOST : Protocol error - invalid");
            return false;
        }

        int id = Integer.parseInt(params[0]);
        String modifier = params[1];

        if (id == 0 && modifier == "connectMe") { // id == 0 for initializaition
            return true;
        }

        if (HostModel.getHostModel().getPlayersByID().get(id) == null) { // No player exist
            // PRINT DEBUG
            System.out.println("HOST : Protocol error - No player exist");
            return false;
        }

        if (HostModel.getHostModel().getPlayersByID().get(id).getID() != id) { // No ID
            // PRINT DEBUG
            System.out.println("HOST : Protocol error - No matching ID");
            return false;
        }

        return true;
    }

    @Override
    public void handleClient(InputStream inFromclient, OutputStream outToClient) {
        /*
         * HOST RECEIVES:
         * a string from the guest
         * string starts with the geust ID,
         * then the Model method,
         * and then the Value (like word).
         * 
         * (ID is 0 for initializaition)
         * e.g. - "0,connectMe,true" , "0,getMyID,true" , "2146376,query,Hello"
         * 
         * HOST RESPONDS :
         * a string to the guest
         * string starts with the geust ID,
         * then the Model method,
         * and then the return value.
         * 
         * e.g. - "0,connectMe,true" , "0,getMyID,2146376" , "2146376,query,32"
         * 
         */

        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            String request = in.readLine();

            if (isValid(request)) {
                String modifier = request.split(",")[1];
                String value = request.split(",")[2];

                switch (modifier) {

                    // All model cases
                    case "connectMe":
                        connectHandler();
                    case "myBookChoice":
                        addBookHandler();
                    case "query":
                        queryHandler();
                    case "challenge":
                        challengeHandler();
                    case "pullTiles":
                        pullTilesHandler();
                    case "skipTurn":
                        skipTurnHandler();
                    case "quitGame":
                        quitHandler();
                    case "getMyName":
                        getNameHandler();
                    case "getMyID":
                        getIdHandler();
                    case "getMyScore":
                        scoreHandler();
                    case "isMyTurn":
                        myTurnHandler();
                    case "getCurrentBoard":
                        boardHandler();
                    case "getMyTiles":
                        tilesHandler();
                    case "getMyWords":
                        wordsHandler();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connectHandler() {
    }

    private void addBookHandler() {
    }

    private void queryHandler() {
    }

    private void challengeHandler() {
    }

    private void pullTilesHandler() {
    }

    private void skipTurnHandler() {
    }

    private void quitHandler() {
    }

    private void getNameHandler() {
    }

    private void getIdHandler() {
    }

    private void scoreHandler() {
    }

    private void myTurnHandler() {
    }

    private void boardHandler() {
    }

    private void tilesHandler() {
    }

    private void wordsHandler() {
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
