package model;

import java.io.*;
import model.server.BookScrabbleHandler;
import model.server.MyServer;

public class mainTestHost {
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\n*** HOST MODE ***\n");

        // Create and start the Game server on port 11224:
        MyServer gs = new MyServer(11224, new BookScrabbleHandler());
        gs.start();

        // Create Host model:
        HostModel hm = HostModel.getHM(); // starts host server on port 8040

        // Set up name & Connect to the game server:
        String myName = null;
        try {
            System.out.println("Enter your name, and then press enter: ");
            myName = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        hm.connectMe(myName, "localhost", 11224); //to local game server

        // Wait...
        System.out.println();
        try {
            System.out.println("waiting... ");
            String enter = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();

        // Close game server and Host quit game (close host server):
        gs.close();
        hm.quitGame();
        System.out.println("done");
    }
}
