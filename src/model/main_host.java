package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import model.game.Tile;
import model.game.Word;
import model.host.HostModel;
import model.server.*;

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
        MyServerParallel gameServer = new MyServerParallel(11224, new BookScrabbleHandler());
        gameServer.start();

        // Create Host model:
        HostModel hm = HostModel.get(); // starts host server on port 8040
        hm.setNumOfPlayers(3);

        // // Set up name & Connect to the game server:
        // String myName = null;
        // System.out.println("Enter your name:");
        // myName = in.nextLine();
        // System.out.println();

        // Set All
        if (pressEnter("PRESS ENTER TO CONNECT", in).equals("")) {

            hm.connectMe("Aviv", "localhost", 11224); // to local game server
            hm.myBookChoice("alice_in_wonderland.txt");
            hm.ready();
        }

        String key;
        while (!(key = pressEnter("", in)).equals("0")) {
            if (key.equals("1")) {
                String letters = pressEnter("ENTER WORD LETTERS: ", in);
                System.out.println(letters);
                List<Tile> tiles = new ArrayList<>();
                for (Character c : letters.toCharArray()) {
                    for (Tile t : hm.getGameProperties().getMyHandTiles()) {
                        if (t.getLetter() == c) {
                            tiles.add(t);
                        }
                    }
                }
                Tile[] tt = new Tile[tiles.size()];
                for (int i = 0; i < tt.length; i++) {
                    tt[i] = tiles.get(i);
                }

                Word word = new Word(tt, 7, 7, false);

                hm.tryPlaceWord(word);

            } else if (key.equals("2")) {
                hm.challenge();

            } else if (key.equals("3")) {
                hm.skipTurn();
            } else {
                System.out.println("wrong key");
            }
        }

        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        // hm.skipTurn();
        // }
        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        // hm.skipTurn();
        // }

        // Wait...
        if (pressEnter("PRESS ENTER TO QUIT GAME", in).equals("")) {

            // Close game server and Host quit game (close host server):
            hm.quitGame();
            gameServer.close();
            System.out.println();
            System.out.println("done");
        }

    }
}
