package model.test;

import java.io.IOException;

import model.game.*;
import model.server.*;

public class GameManagerTest {
    public static void main(String[] args) {

        
        // Create and start the Game server on port 11224:
        MyServerParallel gameServer = new MyServerParallel(11224, new BookScrabbleHandler());
        gameServer.start();

        String myName = "Aviv";
        GameManager g = GameManager.get();
        try {
            g.setTotalPlayersCount(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        g.setGameServerSocket("localhost", 11224);
        int myId = g.connectGuestHandler(myName);
        g.addBookHandler("Harray Potter.txt");
        g.setReady();
        // g.addBookHandler("Frank Herbert - Dune.txt");
        // g.addBookHandler("Harray Potter.txt");
        if (!g.isReadyToPlay()) {
            System.out.println("problem with ready to play");
        }
        Tile[] t = new Tile[4]; // McGonagall
        t[0] = g.getGameBag().getTile('A');
        t[1] = g.getGameBag().getTile('A');
        t[2] = g.getGameBag().getTile('A');
        t[3] = g.getGameBag().getTile('B');
        // t[4] = g.getGameBag().getTile('N');
        // t[5] = g.getGameBag().getTile('A');
        // t[6] = g.getGameBag().getTile('G');
        // t[7] = g.getGameBag().getTile('A');
        // t[8] = g.getGameBag().getTile('L');
        // t[9] = g.getGameBag().getTile('L');

        Word w = new Word(t, 7, 7, false);

        try {
            String ans = g.processPlayerInstruction(myId, "tryPlaceWord", ObjectSerializer.serializeObject(w));
            System.out.println(ans);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GameProperties p = new GameProperties(myName);
        // try {
        // p.setMyBoard((Tile[][]) ObjectSerializer
        // .deserializeObject(g.processPlayerInstruction(myId, "getCurrentBoard",
        // "true")));
        // } catch (ClassNotFoundException | IOException e) {
        // e.printStackTrace();
        // }

        gameServer.close();
        g.close();

    }
}
