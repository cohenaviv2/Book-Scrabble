package model.server;

import java.io.*;

/*
 * The Book scrabble handler is used to communicate with the game server
 * the game server is responsible for checking if the word is dictionary legal
 * Server reads a string from the client until line-break character,
 * string starts with "Q," for query or "C," for challenge.
 * the rest of the string (seperated by ",")
 * indicate the names of the books, except for the last word that indicates the
 * query itself.
 * Using DictionaryManager will return the answer as a string "true" or
 * "false" followed by a line break character.
 * The conversation with the client will end after one query.
 * 
 * @author: Aviv Cohen
 * 
 */

public class BookScrabbleHandler implements ClientHandler {
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void handleClient(InputStream inFromclient, OutputStream outToClient) {

        String[] userLine = null;
        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);
            userLine = in.readLine().split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String operator = userLine[0];
        String[] books = new String[userLine.length - 1];
        System.arraycopy(userLine, 1, books, 0, (userLine.length - 1));

        DictionaryManager dm = DictionaryManager.get();

        if (operator.equals("Q")) {
            if (dm.query(books)) {
                out.println("true");
            } else {
                out.println("false");
            }
        } else if (operator.equals("C")) {
            if (dm.challenge(books)) {
                out.println("true");
            } else {
                out.println("false");
            }
        } else {
            System.out.println(userLine[0]+","+userLine[1]+","+userLine[2]);
            out.println("wrong operator");
        }

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
