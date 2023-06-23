package model.guest;

import java.io.IOException;
import java.util.*;
import model.GameModel;
import model.game.*;

public class GuestModel implements GameModel {

    private static GuestModel gm = null; // Singleton
    private CommunicationHandler commHandler;
    private GameProperties gameProperties;

    private GuestModel() {
    }

    public static GuestModel get() {
        if (gm == null)
            gm = new GuestModel();
        return gm;
    }

    public GameProperties getGameProperties() {
        return this.gameProperties;
    }

    @Override
    public void connectMe(String name, String ip, int port) {
        /*
         * Connects to the host server via socket
         * sets the guest player profile
         * (Gets unique ID from the host)
         */
        gameProperties = new GameProperties(name);
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
        try {
            String word = ObjectSerializer.serializeObject(myWord);
            commHandler.sendMessage("tryPlaceWord", word);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void challenge() {
        commHandler.sendMessage("challenge", "true");

    }

    @Override
    public void skipTurn() {
        if (gameProperties.isMyTurn()) {
            commHandler.sendMessage("skipTurn", "true");
        }
    }

    @Override
    public void quitGame() {
        commHandler.sendMessage("quitGame", "true");
        commHandler.close();
    }

    @Override
    public Map<String, Integer> getOthersScore() {
        commHandler.sendMessage("getOthersScore", "true");
        return gameProperties.getPlayersScore();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        commHandler.sendMessage("getCurrentBoard", "true");
        return gameProperties.getMyBoard();
    }

    @Override
    public int getMyScore() {
        commHandler.sendMessage("getMyScore", "true");
        return gameProperties.getMyScore();
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        commHandler.sendMessage("getMyTiles", "true");
        return gameProperties.getMyHandTiles();
    }

    @Override
    public ArrayList<Word> getMyWords() {
        commHandler.sendMessage("getMyWords", "true");
        return gameProperties.getMyWords();
    }

    @Override
    public boolean isMyTurn() {
        commHandler.sendMessage("isMyTurn", "true");
        return gameProperties.isMyTurn();
    }

    public void updateAllStates() {
        this.gameProperties.setMyBoard(getCurrentBoard());
        this.gameProperties.setMyTiles(getMyTiles());
        this.gameProperties.setMyScore(getMyScore());
        this.gameProperties.setMyWords(getMyWords());
        this.gameProperties.setMyTurn(isMyTurn());
        this.gameProperties.setPlayersScore(getOthersScore());
        // try {
        //     Thread.sleep(5000);
        // } catch (InterruptedException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }

    // @Override
    // public void update(Observable o, Object arg) {
    // if (o == commHandler) {
    // System.out.println("OBSERVER");
    // // if (((String) arg).equals("myTurn")) {
    // // gameProperties.setMyTurn(true);
    // // }
    // // if (((String) arg).equals("updateAll")) {
    // // updateAllStates();
    // // }
    // updateAllStates();
    // }
    // }

}
