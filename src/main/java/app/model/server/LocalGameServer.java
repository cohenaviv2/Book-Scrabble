package app.model.server;

import java.util.Scanner;

public class LocalGameServer {
    public static void main(String[] args) {
        System.out.println("########### GAME SERVER ###########\n");

        // Create and start the Game server on port 11224:
        MyServer gameServer = new MyServer(11224, new BookScrabbleHandler());
        gameServer.start();
        System.out.println("Game server is running On port 11224 in the background...\n\n PRESS 0 TO CLOSE THE GAME SERVER");
        Scanner s = new Scanner(System.in);
        if (s.nextLine().equals("0")){
            gameServer.close();
            s.close();
            System.out.println("Game server is closed");
        }
    }
}
