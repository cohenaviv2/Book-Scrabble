package view_model;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.GameModel;
import model.game.*;
import model.guest.GuestModel;
import model.host.HostModel;

public class GameViewModel extends Observable implements Observer {

    private GameModel gameModel;

    private Tile[][] currentBoard;
    private ArrayList<Tile> myTiles;
    private int myScore;
    private ArrayList<Word> myWords;
    private boolean myTurn;
    private Map<String, Integer> othersScore;

    private ObservableValue<String> myNameView;
    private ObservableValue<String> myScoreView;
    private ObservableValue<String> myTurnView;
    private ObservableList<String> myWordsView;
    private ObservableList<String> myTilesView;
    private ObservableList<String> othersScoreView;

    public GameViewModel() {
    }

    public void setGameMode(String MODE) {
        if (MODE.equals("H")) {
            this.gameModel = HostModel.get();
            HostModel.get().addObserver(this);

        } else if (MODE.equals("G")) {
            this.gameModel = GuestModel.get();
            GuestModel.get().addObserver(this);
        } else {
            // PRINT DEBUG
            System.out.println("WRONG GAME MODE OPERATOR! USE H/G");
        }
    }

    public void setNumOfPlayer(int numOfPlayers) {
        GameManager.get().setTotalPlayersCount(numOfPlayers);
    }

    public static ObservableList<String> getBookList() {
        return FXCollections.observableArrayList(GameManager.get().getFullBookList().keySet());
    }

    public Tile[][] getCurrentBoard() {
        return currentBoard;
    }

    public ArrayList<Tile> getMyTiles() {
        return myTiles;
    }

    public int getMyScore() {
        return myScore;
    }

    public ArrayList<Word> getMyWords() {
        return myWords;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public Map<String, Integer> getOthersScore() {
        return othersScore;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel) {
            // Update the ViewModel's state based on changes in the GameModel
            this.currentBoard = gameModel.getPlayerProperties().getMyBoard();
            this.myTiles = gameModel.getPlayerProperties().getMyHandTiles();
            this.myScore = gameModel.getPlayerProperties().getMyScore();
            this.myWords = gameModel.getPlayerProperties().getMyWords();
            this.myTurn = gameModel.getPlayerProperties().isMyTurn();
            this.othersScore = gameModel.getPlayerProperties().getPlayersScore();

            System.out.println(myTiles);
            System.out.println(myScore);
            System.out.println(myWords);
            System.out.println(myTurn);

       

            if (this.myNameView == null) {
                this.myNameView = new SimpleStringProperty(getPlayerProperties().getMyName());
            }

            this.myScoreView = new SimpleStringProperty(String.valueOf(myScore));

            this.myTurnView = new SimpleStringProperty(String.valueOf(getPlayerProperties().isMyTurn()));

            List<String> obsTiles = new ArrayList<>();
            for (Tile t : myTiles) {
                obsTiles.add(String.valueOf(t.getLetter()));
            }
            this.myTilesView = FXCollections.observableArrayList(obsTiles);

            List<String> obsWords = new ArrayList<>();
            for (Word w : getPlayerProperties().getMyWords()) {
                obsWords.add(w.toString());
            }
            this.myWordsView = FXCollections.observableArrayList(obsWords);

            List<String> obsOthers = new ArrayList<>();
            for (String n : getPlayerProperties().getPlayersScore().keySet()) {
                obsOthers.add(n + ": " + getPlayerProperties().getPlayersScore().get(n));
            }
            this.othersScoreView = FXCollections.observableArrayList(obsOthers);

            System.out.println("VIEW-MODEL : GOT UPDATE");

            setChanged();
            notifyObservers();
            // Notify the View layer of state changes for data binding
            // You can use a specific mechanism provided by your chosen UI framework (e.g.,
            // JavaFX)
            // to notify the bound properties or fields in the View layer about the updated
            // values.
            // For example, in JavaFX, you can use the Property API for data binding.
        }
    }

    public PlayerProperties getPlayerProperties() {
        return this.gameModel.getPlayerProperties();
    }

    public void connectMe(String name, String ip, int port) {
        try {
            gameModel.connectMe(name, ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void myBookChoice(String bookName) {
        gameModel.myBookChoice(bookName);
    }

    public void ready() {
        gameModel.ready();
    }

    public ObservableValue<String> getPlayerNameProperty() {
        return myNameView;
    }

    public ObservableValue<String> getPlayerScoreProperty() {
        return myScoreView;
    }

    public ObservableValue<String> getPlayerTurnProperty() {
        return myTurnView;
    }

    public ObservableList<String> getPlayerWords() {
        return myWordsView;
    }

    public ObservableList<String> getPlayerTiles() {
        return myTilesView;
    }

    public ObservableList<String> getOthersScores() {
        return othersScoreView;
    }
}
