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
            //
            String ack = "ack";
            String books = "server/books/Lord of the Flies.txt,server/books/Harray Potter.txt,server/books/All's Well That Ends Well.txt,server/books/Charlie and the Chocolate Factory.txt,server/books/Alice in Wonderland.txt,server/books/Dune - Frank Herbert.txt,server/books/Moby-Dick.txt,";
            String status = "status";
            Scanner input = new Scanner(System.in);
            System.out.println("1 - ack\n2 - status\n3 - query\n4 - challenge\n0 - Exit\n");
            String clInput;
            while (!(clInput = input.nextLine()).equals("0")) {
                Properties properties = loadProperties();
                String GAME_SERVER_IP = properties.getProperty("GAME_SERVER_IP");
                int GAME_SERVER_PORT = Integer.parseInt(properties.getProperty("GAME_SERVER_PORT"));
                Socket oracle_game_server = new Socket(GAME_SERVER_IP, GAME_SERVER_PORT);
                //
                PrintWriter out = new PrintWriter(oracle_game_server.getOutputStream(), true);
                Scanner in = new Scanner(oracle_game_server.getInputStream());
                switch (clInput) {
                    case "1": {
                        out.println(ack);
                        String ans = in.nextLine();
                        System.out.println("\n" + ans + "\n");
                        break;
                    }
                    case "2": {
                        out.println(status);
                        String ans = in.nextLine();
                        System.out.println("\nNumber of books: " + ans + "\n");
                        int size;
                        if ((size = Integer.parseInt(ans)) > 0) {
                            for (int i = 0; i < size; i++) {
                                ans = in.nextLine();
                                System.out.println(ans);
                            }
                        }
                        System.out.println();
                        break;
                    }
                    case "3": {
                        System.out.println("Enter word:");
                        String word = input.nextLine();
                        out.println(
                                "Q," + books + word);
                        String ans = in.nextLine();
                        System.out.println("\n" + ans + "\n");
                        break;
                    }
                    case "4": {
                        System.out.println("Enter word:");
                        String word = input.nextLine();
                        out.println(
                                "C," +books + word);
                        String ans = in.nextLine();
                        System.out.println("\n" + ans + "\n");
                        break;
                    }
                }
                oracle_game_server.close();
                System.out.println("1 - ack\n2 - status\n3 - query\n4 - challenge\n0 - Exit\n");
                
            }
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
