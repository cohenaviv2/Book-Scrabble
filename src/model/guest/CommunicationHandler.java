package model.guest;

import java.io.*;
import model.server.ClientHandler;

public class CommunicationHandler implements ClientHandler {
    private final String ipString;
    private final int port;

    public CommunicationHandler(String ipString, int port) {
        this.ipString = ipString;
        this.port = port;
    }

    @Override
    public void handleClient(InputStream inputStream, OutputStream outputStream) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter out = new PrintWriter(outputStream, true)) {
            String serverMessage;

            while ((serverMessage = in.readLine()) != null) {
                // Print the server's message
                System.out.println("Server: " + serverMessage);

                // Check if it's the client's turn to play
                if (serverMessage.equals("Your turn to play. Please enter your move:")) {
                    // Read the client's move from the console
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                    String clientMove = consoleReader.readLine();

                    // Send the client's move to the server
                    out.println(clientMove);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // Cleanup or additional close logic if required
    }
}
