package app.model.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class RunGameServer {

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("env.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    Properties properties = loadProperties();
    // public static final String IP = "129.159.145.21";
    // public static final int PORT = 11224;

    public static void main(String[] args) {
        try {
            Properties properties = loadProperties();
            String GAME_SERVER_IP = properties.getProperty("GAME_SERVER_IP");
            int GAME_SERVER_PORT = Integer.parseInt(properties.getProperty("GAME_SERVER_PORT"));
            Socket oracle_game_server = new Socket(GAME_SERVER_IP , GAME_SERVER_PORT);
            //
            System.out.println("connect to oracle_game_server: " + oracle_game_server.isConnected());
            //
            PrintWriter out = new PrintWriter(oracle_game_server.getOutputStream(),true);
            Scanner in = new Scanner(oracle_game_server.getInputStream());
            //
            String req = "C,"+"server/books/Harray Potter.txt,server/books/Alice in Wonderland.txt,"+"SEND";
            out.println(req);
            String ans = in.nextLine();
            System.err.println(ans);
            //
            oracle_game_server.close();

        } catch (IOException e) {
        }

        // System.out.println("########### GAME SERVER ###########\n");

        // // Create and start the Game server on port 11224:
        // MyServer gameServer = new MyServer(PORT, new BookScrabbleHandler());
        // gameServer.start();
        // System.out.println(
        // "Game server is running On port 11224 in the background...\n\n PRESS 0 TO
        // CLOSE THE GAME SERVER");
        // Scanner s = new Scanner(System.in);
        // if (s.nextLine().equals("0")) {
        // gameServer.close();
        // s.close();
        // System.out.println("Game server is closed");
        // }
    }
}
