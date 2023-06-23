package model;

import java.io.*;
import java.util.*;

import model.game.*;
import model.guest.GuestModel;

public class main_guest {

    public static String pressEnter(String info, Scanner in) {
        System.out.println();
            System.out.println(info + "\n");
            String ent = in.nextLine();
            return ent;
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(new InputStreamReader(System.in));
        System.out.println("********* GUEST MODE *********\n");

        // Create Guest model:
        GuestModel gm = GuestModel.get();

        // // Set name, ip and port:
        // String name = null;
        //     System.out.println("Enter your name: ");
        //     name = in.nextLine();
        //     System.out.println();

        // Wait...
        String key;
        while (!(key = pressEnter("", in)).equals("0")) {
            if (key.equals("1")) {
                String letters = pressEnter("ENTER WORD LETTERS: ", in);
                System.out.println(letters);
                List<Tile> tiles = new ArrayList<>();
                for (Character c : letters.toCharArray()) {
                    for (Tile t : gm.getGameProperties().getMyHandTiles()) {
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

                gm.tryPlaceWord(word);

            } else if (key.equals("2")) {
                gm.challenge();

            } else if (key.equals("3")) {
                gm.skipTurn();
            } else {
                System.out.println("wrong key");
            }
        }

        // System.out.println("YOUR TURN - " + gm.getGameProperties().isMyTurn());
        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        //     gm.skipTurn();

        //     System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
        // }
        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        //     gm.skipTurn();

        //     System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
        // }


        // Wait...
        if (pressEnter("PRESS ENTER TO QUIT GAME", in).equals("")) {

            // Disconnect the host server and quit the game:
            gm.quitGame();
            System.out.println();
            System.out.println("done");
        }

        // Tile[] tt = new Tile[3];
        // tt[0] = HostModel.getHM().getGameManager().getGameBag().getTile('T');
        // tt[1] = HostModel.getHM().getGameManager().getGameBag().getTile('H');
        // tt[2] = HostModel.getHM().getGameManager().getGameBag().getTile('E');
        // Word w = new Word(tt, 7, 7, false);
        // gs.tryPlaceWord(w);
    }

}
