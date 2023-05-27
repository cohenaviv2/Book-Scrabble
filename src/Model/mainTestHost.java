package model;

import java.io.*;
import java.net.UnknownHostException;

import model.server.BookScrabbleHandler;
import model.server.MyServer;

public class mainTestHost {
    public static void main(String[] args) {

        // Create Game server:

        MyServer gs = new MyServer(11224, new BookScrabbleHandler());
        gs.start();

        // Create Host model:

       HostModel hm = HostModel.getHM(); // creates host server on port 10255

        // Connect to the game server:

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;
        try {
            System.out.println("Enter your name,then press enter: ");
            name = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // connect to the game server and starts the host server:
        hm.connectMe(name, null, 11224);
        
        System.out.println();
        try {
            System.out.println("wait ");
            name = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        System.out.println();

        gs.close();
        hm.quitGame();
        System.out.println("done");
    }
}
