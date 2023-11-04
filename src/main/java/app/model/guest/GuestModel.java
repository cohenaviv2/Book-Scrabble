package app.model.guest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import app.model.GameModel;
import app.model.GetMethod;
import app.model.game.ObjectSerializer;
import app.model.game.PlayerProperties;
import app.model.game.Tile;
import app.model.game.Word;

public class GuestModel extends Observable implements GameModel, Observer {
    private static GuestModel gm = null; // Singleton
    private CommunicationHandler commHandler;
    private PlayerProperties playerProperties;
    private boolean isConnected = false;

    private GuestModel() {
    }

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
            // System.out.println("guest is connected");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void myBooksChoice(List<String> bookName) {
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
        if (commHandler != null) {
            commHandler.close();
            System.out.println("\nCommunication Handler is closed.");
        }
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

    @Override
    public void update(Observable o, Object arg) {
        if (o == commHandler) {
            setChanged();
            notifyObservers(arg);
        }
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void sendTo(String name, String message) {
        commHandler.sendMessage(GetMethod.sendTo, name + ":" + message + ":" + playerProperties.getMyName());
    }

    @Override
    public void sendToAll(String message) {
        commHandler.sendMessage(GetMethod.sendToAll, "All" + ":" + message + ":" + playerProperties.getMyName());
    }

}
