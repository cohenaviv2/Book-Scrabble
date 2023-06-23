package model.guest;

import java.io.IOException;
import java.util.*;
import model.GameModel;
import model.game.*;

public class GuestModel extends Observable implements GameModel{

    private static GuestModel gm = null; // Singleton
    private CommunicationHandler commHandler;
    private PlayerProperties playerProperties;

    private GuestModel() {
    }

    public static GuestModel get() {
        if (gm == null)
            gm = new GuestModel();
        return gm;
    }

    public PlayerProperties getPlayerProperties() {
        return this.playerProperties;
    }

    @Override
    public void connectMe(String name, String ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        playerProperties = new PlayerProperties(name);
        commHandler = new CommunicationHandler(ip, port);
        commHandler.connectMe(name);

    }

    @Override
    public void myBookChoice(String bookName) {
        commHandler.addMyBookChoice(bookName);
    }

    @Override
    public void ready() {
        commHandler.sendMessage("ready", "true");
    }

    @Override
    public void tryPlaceWord(Word myWord) {
        if (playerProperties.isMyTurn()) {
            try {
                String word = ObjectSerializer.serializeObject(myWord);
                commHandler.sendMessage("tryPlaceWord", word);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void challenge() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage("challenge", "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void skipTurn() {
        if (playerProperties.isMyTurn()) {
            commHandler.sendMessage("skipTurn", "true");
        } else
            System.out.println("its not your turn");

    }

    @Override
    public void quitGame() {
        commHandler.sendMessage("quitGame", "true");
        commHandler.close();
    }

    @Override
    public Map<String, Integer> getOthersScore() {
        commHandler.sendMessage("getOthersScore", "true");
        return playerProperties.getPlayersScore();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        commHandler.sendMessage("getCurrentBoard", "true");
        return playerProperties.getMyBoard();
    }

    @Override
    public int getMyScore() {
        commHandler.sendMessage("getMyScore", "true");
        return playerProperties.getMyScore();
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        commHandler.sendMessage("getMyTiles", "true");
        return playerProperties.getMyHandTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        commHandler.sendMessage("getMyWords", "true");
        return playerProperties.getMyWords();
    }

    @Override
    public boolean isMyTurn() {
        commHandler.sendMessage("isMyTurn", "true");
        return playerProperties.isMyTurn();
    }

    public void updateAllStates() {
        this.playerProperties.setMyBoard(getCurrentBoard());
        this.playerProperties.setMyTiles(getMyTiles());
        this.playerProperties.setMyScore(getMyScore());
        this.playerProperties.setMyWords(getMyWords());
        this.playerProperties.setMyTurn(isMyTurn());
        this.playerProperties.setPlayersScore(getOthersScore());
    }


}
