package app.model.test;

import java.io.*;
import java.util.*;

import app.model.game.*;
import app.model.host.*;
import app.model.server.*;

public class main_host {

    public static String pressEnter(String info, Scanner in) {
        System.out.println();
        System.out.println(info + "\n");
        String ent = in.nextLine();
        return ent;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(new InputStreamReader(System.in));
        System.out.println("********* HOST MODE *********\n");

        // Create and start the Game server on port 11224:
        MyOldServer gameServer = new MyOldServer(11224, new BookScrabbleHandler());
        gameServer.start();

        // Create Host model:
        HostModel hm = HostModel.get(); // starts host server on port 8040
        hm.setNumOfPlayers(2);

        // // Set up name & Connect to the game server:
        // String myName = null;
        // System.out.println("Enter your name:");
        // myName = in.nextLine();
        // System.out.println();

        // Set All
        if (pressEnter("PRESS ENTER TO CONNECT", in).equals("")) {

            try {
                hm.connectMe("Aviv", "localhost", 11224);
                hm.myBooksChoice("Alice in Wonderland");
                hm.ready();
            } catch (Exception e) {
                e.printStackTrace();
            } // to local game server
        }

        String key;
        while (!(key = pressEnter("PRESS:\n1 - Try place word\n2 - Challange\n3 - Pass turn\n0 - Quit game", in))
                .equals("0")) {
            try {
                if (key.equals("1")) {
                    String[] line = pressEnter("ENTER WORD LETTERS AND ROW & COL: ", in).split(" ");
                    int row = Integer.parseInt(line[1]);
                    int col = Integer.parseInt(line[2]);
                    Boolean isVer = line[3].equals("t") ? true : false;
                    char[] c = line[0].toCharArray();
                    Tile[] tiles = new Tile[c.length];
                    for (int i = 0; i < c.length; i++) {
                        if (c[i] == '_')
                            tiles[i] = null;
                        for (Tile t : hm.getPlayerProperties().getMyHandTiles()) {
                            if (t.getLetter() == c[i]) {
                                tiles[i] = t;
                            }
                        }
                    }

                    Word word = new Word(tiles, row, col, isVer);

                    hm.tryPlaceWord(word);

                } else if (key.equals("2")) {
                    hm.challenge();

                } else if (key.equals("3")) {
                    hm.skipTurn();
                } else {
                    System.out.println("wrong key");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (pressEnter("PRESS ENTER TO QUIT GAME", in).equals("")) {

            // Close game server and Host quit game (close host server):
            hm.quitGame();
            gameServer.close();
            System.out.println();
            System.out.println("done");
        }

    }
}
