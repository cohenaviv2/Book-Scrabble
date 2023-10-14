package app.view_model;

import app.model.GameModel;
import app.model.game.*;
import app.model.guest.GuestModel;
import app.model.host.HostModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

public class ViewModel extends Observable implements Observer {
    GameModel gameModel;

    private StringProperty myNameProperty;
    private ObjectProperty<Tile[][]> currentBoardProperty;
    private ListProperty<Tile> myTilesProperty;
    private ListProperty<Word> myWordsProperty;
    private IntegerProperty myScoreProperty;
    private BooleanProperty myTurnProperty;
    private MapProperty<String, String> othersInfoProperty;
    private SetProperty<String> gameBooksProperty;
    private IntegerProperty bagCountProperty;

    private BooleanProperty connectedProperty;

    public ViewModel(boolean isHost) {
        if (isHost) {
            this.gameModel = HostModel.get();
            HostModel.get().addObserver(this);
        } else {
            this.gameModel = GuestModel.get();
            GuestModel.get().addObserver(this);
        }
        myNameProperty = new SimpleStringProperty();
        connectedProperty = new SimpleBooleanProperty();
        currentBoardProperty = new SimpleObjectProperty<>();
        myTilesProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        myScoreProperty = new SimpleIntegerProperty();
        myWordsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        myTurnProperty = new SimpleBooleanProperty();
        othersInfoProperty = new SimpleMapProperty<>(FXCollections.observableHashMap());
        gameBooksProperty = new SimpleSetProperty<>(FXCollections.observableSet());
        bagCountProperty = new SimpleIntegerProperty();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel && arg instanceof String) {

            String message = (String) arg;

            System.out.println(message);

            myNameProperty.set(gameModel.getPlayerProperties().getMyName());
            currentBoardProperty.set(gameModel.getCurrentBoard());
            myTilesProperty.clear();
            myTilesProperty.setAll(gameModel.getMyTiles());
            myScoreProperty.set(gameModel.getMyScore());
            myWordsProperty.setAll(gameModel.getMyWords());
            myTurnProperty.set(gameModel.isMyTurn());
            othersInfoProperty.clear();
            othersInfoProperty.putAll(gameModel.getOthersInfo());
            gameBooksProperty.clear();
            gameBooksProperty.addAll(gameModel.getGameBooks());
            bagCountProperty.set(gameModel.getBagCount());

        }
    }

    public StringProperty getMyNameProperty() {
        return myNameProperty;
    }

    public BooleanProperty getConnectedProperty() {
        return connectedProperty;
    }

    public ObjectProperty<Tile[][]> getCurrentBoardProperty() {
        return currentBoardProperty;
    }

    public ListProperty<Tile> getMyTilesProperty() {
        return myTilesProperty;
    }

    public IntegerProperty getMyScoreProperty() {
        return myScoreProperty;
    }

    public ListProperty<Word> getMyWordsProperty() {
        return myWordsProperty;
    }

    public BooleanProperty getMyTurnProperty() {
        return myTurnProperty;
    }

    public MapProperty<String, String> getOthersInfoProperty() {
        return othersInfoProperty;
    }

    public SetProperty<String> getGameBooksProperty() {
        return gameBooksProperty;
    }

    public IntegerProperty getBagCountProperty() {
        return bagCountProperty;
    }

    // Methods to interact with the GameModel
    public void connectMe(String name, String ip, int port) {
        gameModel.connectMe(name, ip, port);
    }

    public void setTotalPlayersCount(int numOfPlayers) {
        HostModel.get().setNumOfPlayers(numOfPlayers);
    }

    public boolean isConnected() {
        return this.gameModel.isConnected();
    }

    public void myBooksChoice(List<String> bookList) {
        gameModel.myBooksChoice(bookList);
    }

    public void ready() {
        gameModel.ready();
    }

    public void tryPlaceWord(Word myWord) {
        gameModel.tryPlaceWord(myWord);
    }

    public void challenge() {
        gameModel.challenge();
    }

    public void skipTurn() {
        gameModel.skipTurn();
    }

    public void sendTo(String name, String message) {
        gameModel.sendTo(name, message);
    }

    public void sendToAll(String message) {
        gameModel.sendToAll(message);
    }

    public void quitGame() {
        gameModel.quitGame();
    }

    public boolean isGameServerConnect() {
        return HostModel.get().isGameServerConnect();
    }

    public boolean isValidPort(String portText) {
        try {
            int portNumber = Integer.parseInt(portText);
            return portNumber >= 0 && portNumber <= 65535 && portText.length() <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getPublicIpAddress() {
        String publicIp = "";
        URL connection;
        try {
            connection = new URL("http://checkip.amazonaws.com/");
            URLConnection con = connection.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            publicIp = reader.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return publicIp;
    }
}
