package app.model.guest;

import java.io.IOException;
import java.util.*;

import app.model.GameModel;
import app.model.GetMethod;
import app.model.game.*;

public class GuestModel extends Observable implements GameModel, Observer {
    private static GuestModel gm = null; // Singleton
    private CommunicationHandler commHandler;
    private PlayerProperties playerProperties;
    private boolean isConnected = false;

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
    public void connectMe(String name, String ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        playerProperties = PlayerProperties.get();
        playerProperties.setMyName(name);
        try {
            commHandler = new CommunicationHandler(ip, port);
            this.commHandler.addObserver(this);
            commHandler.connectMe(name);
            setIsConnected(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            commHandler.sendMessage(GetMethod.ready, "true");
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
                commHandler.sendMessage(GetMethod.tryPlaceWord, word);
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void challenge() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage(GetMethod.challenge, "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void skipTurn() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage(GetMethod.skipTurn, "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void quitGame() {
        if (isConnected) {
            commHandler.sendMessage(GetMethod.quitGame, "true");
        }
        if (commHandler != null){
            commHandler.close();
            // System.out.println("Communication Handler is closed.");
        }
        // System.out.println("Guest Model: you quit the game.");
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

    @Override
    public boolean isConnected() {
        return this.isConnected;
    }

    // public void update() {
    // setChanged();
    // notifyObservers();
    // }

    @Override
    public void update(Observable o, Object arg) {
        if (o == commHandler) {
            setChanged();
            notifyObservers(arg);
            // // System.out.println("\n\n\n\n\n"+(String)arg+"\n\n\n\n\n");
        }
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

}
