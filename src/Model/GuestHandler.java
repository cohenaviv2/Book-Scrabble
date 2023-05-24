package model;

import java.io.*;

import model.server.ClientHandler;

public class GuestHandler implements ClientHandler {
    BufferedReader in;
    PrintWriter out;

    public void setClient(InputStream inFromclient, OutputStream outToClient) {
        
    }

    @Override
    public void handleClient(InputStream inFromclient, OutputStream outToClient) {
        /*
         * Reads a string from the guest until line-break character.
         * String starts with the Player ID
         * seperated by ","
         * The method the Player want to use
         * seperated by ","
         * The word for this query
         * 
         * e.g. - "1548, query(), Hello"
         * 
         * Host respondes a string starting with HOST
         * seperated by ","
         * The type of return data
         * seperated by ","
         * The data itself
         * 
         * e.g. - "HOST,int,12" , "HOST,boolean,true"
         * 
         */

        String[] userLine = null;
        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            userLine = in.readLine().split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String playerID = userLine[0];
        String func = userLine[1];
        String queryWord = userLine[2];
        String[] args = new String[userLine.length - 1];
        System.arraycopy(userLine, 1, args, 0, (userLine.length - 1));

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
