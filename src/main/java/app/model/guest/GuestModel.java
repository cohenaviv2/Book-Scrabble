package app.model.guest;

import java.io.IOException;
import java.util.*;

import app.model.GameModel;
import app.model.MethodInvoker;
import app.model.game.*;

public class GuestModel extends Observable implements GameModel {
    private static GuestModel gm = null; // Singleton
    private CommunicationHandler commHandler;
    private PlayerProperties playerProperties;

    public static GuestModel get() {
        if (gm == null)
            gm = new GuestModel();
        return gm;
    }

    @Override
    public PlayerProperties getPlayerProperties() {
        return PlayerProperties.get();
    }

    @Override
    public void connectMe(String name, String ip, int port) throws IOException {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        playerProperties = PlayerProperties.get();
        playerProperties.setMyName(name);
        commHandler = new CommunicationHandler(ip, port);
        commHandler.connectMe(name);

    }

    @Override
    public void myBooksChoice(String bookName) {
        try {
            commHandler.addMyBookChoice(bookName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ready() {
        try {
            commHandler.sendMessage(MethodInvoker.ready, "true");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void tryPlaceWord(Word myWord) {
        if (playerProperties.isMyTurn()) {
            try {
                String word;
                word = ObjectSerializer.serializeObject(myWord);
                commHandler.sendMessage(MethodInvoker.tryPlaceWord, word);
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void challenge() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage(MethodInvoker.challenge, "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void skipTurn() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage(MethodInvoker.skipTurn, "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void quitGame() {
        commHandler.sendMessage(MethodInvoker.quitGame, "true");
        commHandler.close();
    }

    @Override
    public Map<String, String> getOthersInfo() {
        return this.playerProperties.getOtherPlayersInfo();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        return this.playerProperties.getMyBoard();
    }

    @Override
    public int getMyScore() {
        return this.playerProperties.getMyScore();
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        return this.playerProperties.getMyHandTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        return this.playerProperties.getMyWords();
    }

    @Override
    public boolean isMyTurn() {
        return this.playerProperties.isMyTurn();
    }

    @Override
    public Set<String> getGameBooks() {
        return this.playerProperties.getGameBookList();
    }

    @Override
    public int getBagCount() {
        return this.playerProperties.getBagCount();
    }

    public void update() {
        setChanged();
        notifyObservers();
    }

}
