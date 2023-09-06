package app.view_model;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import app.model.GameModel;
import app.model.game.PlayerProperties;
import app.model.game.Tile;
import app.model.game.Word;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class ViewModel extends Observable implements Observer {
    // Observable properties for UI updates
    private StringProperty playerNameProperty;
    private BooleanProperty connectedProperty;
    private ObjectProperty<PlayerProperties> playerPropertiesProperty;
    private ObjectProperty<Tile[][]> currentBoardProperty;
    private ListProperty<Tile> myTilesProperty;
    private IntegerProperty myScoreProperty;
    private ListProperty<Word> myWordsProperty;
    private BooleanProperty myTurnProperty;
    private MapProperty<String, String> othersInfoProperty;
    private SetProperty<String> gameBooksProperty;
    private IntegerProperty bagCountProperty;

    private GameModel gameModel;

    public ViewModel(GameModel gameModel) {
        this.gameModel = gameModel;

        // Initialize observable properties
        playerNameProperty = new SimpleStringProperty();
        connectedProperty = new SimpleBooleanProperty();
        playerPropertiesProperty = new SimpleObjectProperty<>();
        currentBoardProperty = new SimpleObjectProperty<>();
        myTilesProperty = new SimpleListProperty<>();
        myScoreProperty = new SimpleIntegerProperty();
        myWordsProperty = new SimpleListProperty<>();
        myTurnProperty = new SimpleBooleanProperty();
        othersInfoProperty = new SimpleMapProperty<>(FXCollections.observableHashMap());
        gameBooksProperty = new SimpleSetProperty<>(FXCollections.observableSet());
        bagCountProperty = new SimpleIntegerProperty();

        // Register observers to listen for updates from the game model
        // gameModel.addObserver((observable, arg) -> {
        //     // Update observable properties based on the model's state
        //     playerNameProperty.set(gameModel.getPlayerProperties().getName());
        //     connectedProperty.set(gameModel.isConnected());
        //     playerPropertiesProperty.set(gameModel.getPlayerProperties());
        //     currentBoardProperty.set(gameModel.getCurrentBoard());
        //     myTilesProperty.set(FXCollections.observableList(gameModel.getMyTiles()));
        //     myScoreProperty.set(gameModel.getMyScore());
        //     myWordsProperty.set(FXCollections.observableList(gameModel.getMyWords()));
        //     myTurnProperty.set(gameModel.isMyTurn());
        //     othersInfoProperty.set(FXCollections.observableMap(gameModel.getOthersInfo()));
        //     gameBooksProperty.set(FXCollections.observableSet(gameModel.getGameBooks()));
        //     bagCountProperty.set(gameModel.getBagCount());
        // });
    }

    // Implement methods to interact with the GameModel
    public void connect(String name, String ip, int port) {
        gameModel.connectMe(name, ip, port);
    }

    public void chooseBooks(List<String> bookList) {
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

    // Getters for observable properties
    public StringProperty playerNameProperty() {
        return playerNameProperty;
    }

    public BooleanProperty connectedProperty() {
        return connectedProperty;
    }

    public ObjectProperty<PlayerProperties> playerPropertiesProperty() {
        return playerPropertiesProperty;
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    // Add getters for other properties as needed...
}

