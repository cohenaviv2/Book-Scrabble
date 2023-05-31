package model.guest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class mainTestGuest {
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\n*** GUEST MODE ***");

        // Create Guest model:
        GuestModel gs = new GuestModel();

        // Wait...
        System.out.println();
        try {
            System.out.println("PRESS ENTER TO START ");
            String enter = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();

        // Set ip and port:
        String name = null;
        String ip = null;
        int port = 0;
        try {
            System.out.println("Enter your name: ");
            name = in.readLine();
            System.out.println();
            System.out.println("Enter ip: ");
            ip = in.readLine();
            System.out.println();
            System.out.println("Enter port: ");
            port = Integer.parseInt(in.readLine());
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Connect to the host server:
        gs.connectMe(name, ip, port);
        gs.pullTiles();

        // Wait...
        System.out.println();
        try {
            System.out.println("waiting... ");
            String ent = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();


        // Disconnect the host server and quit the game:
        gs.quitGame();
        System.out.println("done");
    }

}
