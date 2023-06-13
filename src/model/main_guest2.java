package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import model.guest.GuestModel;

public class main_guest2 {
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
        try {
            System.out.println("Enter your name: ");
            gs.setMyName(in.readLine());
            System.out.println();
            System.out.println("Enter ip: ");
            gs.setIpString(in.readLine());
            System.out.println();
            System.out.println("Enter port: ");
            gs.setPort(Integer.parseInt(in.readLine()));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("............");
        // Connect to the host server:
        gs.connectMe(gs.getMyName(), gs.getIpString(), gs.getPort());
        gs.getMyTiles();
        gs.quitGame();
        //gs.pullTiles();
        //gs.getCurrentBoard();

        // Wait...
        System.out.println();
        try {
            System.out.println("guest is waiting... PRESS ENTER TO CONTINUE ");
            String ent = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();

        // Disconnect the host server and quit the game:
        //gs.quitGame();
        System.out.println("done");
    }

}
