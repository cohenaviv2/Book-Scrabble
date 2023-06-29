package app.model.test;

import java.io.*;
import java.util.*;

import app.model.game.*;
import app.model.guest.GuestModel;

public class main_guest2 {

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
            try {
                gm.connectMe("Matan", "localhost", 8040);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            gm.myBookChoice("Moby-Dick");
            gm.ready();

        }

        String key;
        while (!(key = pressEnter("PRESS:\n1 - Try place word\n2 - Challange\n3 - Pass turn\n0 - Quit game", in)).equals("0")) {
            if (key.equals("1")) {
                System.out.println("tryPlaceWord"); //
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
                // List<Tile> tiles = new ArrayList<>();
                // for (Character c : line[0].toCharArray()) {
                //     if (c.equals('_')) tiles.add(null);
                //     for (Tile t : gm.getPlayerProperties().getMyHandTiles()) {
                //         if (t.getLetter() == c) {
                //             tiles.add(t);
                //         }
                //     }
                // }
                // Tile[] tt = new Tile[tiles.size()];
                // for (int i = 0; i < tt.length; i++) {
                //     tt[i] = tiles.get(i);
                // }

                Word word = new Word(tiles, row, col, isVer);

                gm.tryPlaceWord(word);

            } else if (key.equals("2")) {
                System.out.println("challange"); //
                gm.challenge();

            } else if (key.equals("3")) {
                System.out.println("skip"); //
                gm.skipTurn();
            } else {
                System.out.println("wrong key");
            }
        }
        // System.out.println("YOUR TURN - " + gm.getGameProperties().isMyTurn());
        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        // gm.skipTurn();

        // System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
        // }
        // if (pressEnter("PRESS ENTER TO SKIP TURN", in).equals("")) {

        // gm.skipTurn();

        // System.out.println("TURN - " + gm.getGameProperties().isMyTurn());
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
