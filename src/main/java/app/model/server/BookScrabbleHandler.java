package app.model.server;

import java.io.*;
import java.util.Set;

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
        try {
            in = new BufferedReader(new InputStreamReader(inFromclient));
            out = new PrintWriter(outToClient, true);

            String query = in.readLine();

            if (query.equals("ack")) {
                out.println("connected");
                return;
            }
            if (query.equals("status")) {
                out.println(getStatus());
                return;
            }

            String[] userLine = query.split(",");
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
                System.out.println(userLine[0] + "," + userLine[1] + "," + userLine[2]);
                out.println("wrong operator");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Number of books: " + String.valueOf(DictionaryManager.get().size) + "\\n");
        int size = DictionaryManager.get().getDictionaries().size();
        if (size==0) return status.toString();
        Set<String> bookPaths = DictionaryManager.get().getDictionaries().keySet();
        for(String bp : bookPaths){
            status.append(bp + " :\\n");
            String exSize = String.valueOf(DictionaryManager.get().getDictionaries().get(bp).getExCacheSize());
            status.append("Exist words cache size: "+exSize+"\\n");
            String notExSize = String.valueOf(DictionaryManager.get().getDictionaries().get(bp).getNotExCacheSize());
            status.append("Doesnt exist words cache size: "+notExSize+"\\n");
        }

        return status.toString();
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
