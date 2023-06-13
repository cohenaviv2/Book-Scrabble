package model;

import java.io.*;

import model.host.HostModel;
import model.server.BookScrabbleHandler;
import model.server.MyServer;
import model.server.MyServerParallel;

public class main_host {
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("*** HOST MODE ***\n");

        // Create and start the Game server on port 11224:
        MyServer gameServer = new MyServer(11224, new BookScrabbleHandler());
        //gameServer.start();

        // Create Host model:
        HostModel hostModel = HostModel.getHM(); // starts host server on port 8040

        // Set up name & Connect to the game server:
        String myName = null;
        try {
            System.out.println("Enter your name, and then press enter: ");
            myName = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        hostModel.connectMe(myName, "localhost", 11224); //to local game server

        // Wait...
        System.out.println();
        try {
            System.out.println("host is waiting... PRESS ENTER TO CONTINUE IN ANY STEP\n");
            String enter = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();

        // Close game server and Host quit game (close host server):
        //gameServer.close();
        hostModel.quitGame();
        System.out.println("done");
    }
}
