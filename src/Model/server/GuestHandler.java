package model.server;

import java.io.*;

public class GuestHandler implements ClientHandler {
    BufferedReader in;
    PrintWriter out;

    public void setClient(InputStream inFromclient, OutputStream outToClient) {

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
