package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import model.guest.GuestModel;

public class main_guest2 {

    public static String pressEnter(String info, BufferedReader in) {
        System.out.println();
        try {
            System.out.println(info + "\n");
            String ent = in.readLine();
            return ent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("*** GUEST MODE ***\n");

        // Create Guest model:
        GuestModel gm = GuestModel.get();

        // Set name, ip and port:
        String name = null;
        try {
            System.out.println("Enter your name: ");
            name = in.readLine();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wait...
        if (pressEnter("PRESS 1 TO CONNECT", in).equals("1")) {

            // Connect to the host server:
            gm.connectMe(name, "localhost", 8040);

        }

        if (pressEnter("PRESS 1 TO SET YOUR BOOK", in).equals("1")) {

            gm.myBookChoice("Harray Potter.txt");
            gm.ready();

        }
        System.out.println("YOUR TURN - " + gm.getGameProperties().isMyTurn());

        if (pressEnter("PRESS 1 TO SKIP TURN", in).equals("1")) {

            gm.skipTurn();

            System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
        }
        if (pressEnter("PRESS 1 TO SKIP TURN", in).equals("1")) {

            gm.skipTurn();
            
            System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
        }

        // Wait...
        if (pressEnter("PRESS 1 TO QUIT GAME", in).equals("1")) {

            // Disconnect the host server and quit the game:
            gm.quitGame();
            System.out.println();
            System.out.println("done");
        }
    }

}
