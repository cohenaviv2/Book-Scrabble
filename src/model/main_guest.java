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
        // System.out.println("Enter your name: ");
        // name = in.nextLine();
        // System.out.println();

        // Wait...
        if (pressEnter("PRESS ENTER TO CONNECT", in).equals("")) {

            // Connect to the host server:
            gm.connectMe("Moshe", "localhost", 8040);
            gm.myBookChoice("mobydick.txt");
            gm.ready();

        }

        String key;
        while (!(key = pressEnter("PRESS:\n1 - Try place word\n2 - Challange\n3 - Pass turn\n0 - Quit game", in)).equals("0")) {
            if (key.equals("1")) {
                String[] line = pressEnter("ENTER WORD LETTERS AND ROW & COL & isVertical: ", in).split(" ");
                int row = Integer.parseInt(line[1]);
                int col = Integer.parseInt(line[2]);
                Boolean isVer = line[3].equals("t") ? true : false;
                char[] c = line[0].toCharArray();
                Tile[] tiles = new Tile[c.length];
                for (int i=0;i<c.length;i++){
                    if (c[i] == '_') tiles[i] = null;
                    for(Tile t : gm.getPlayerProperties().getMyHandTiles()){
                        if (t.getLetter() == c[i]){
                            tiles[i] = t;
                        }
                    }
                }

                Word word = new Word(tiles, row, col, isVer);

                gm.tryPlaceWord(word);

            } else if (key.equals("2")) {
                gm.challenge();

            } else if (key.equals("3")) {
                gm.skipTurn();
            } else {
                System.out.println("wrong key");
            }
        }

        if (pressEnter("PRESS ENTER TO QUIT GAME", in).equals("")) {

            // Disconnect the host server and quit the game:
            gm.quitGame();
            System.out.println();
            System.out.println("done");
        }
    }

}
