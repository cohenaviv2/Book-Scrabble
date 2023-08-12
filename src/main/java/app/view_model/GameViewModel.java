package app.view_model;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import app.model.GameModel;
import app.model.game.*;
import app.model.guest.GuestModel;
import app.model.host.HostModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;

public class GameViewModel extends Observable implements Observer {

    public GameModel gameModel;

    private ObservableValue<String> myNameView;
    private ObservableValue<String> myScoreView;
    private ObservableValue<String> myTurnView;
    private ObservableList<String> myWordsView;
    private ObservableList<String> myTilesView;
    private ObservableList<String> othersScoreView;

    private int firstRow;
    private int firstCol;
    private int lastRow;
    private int lastCol;
    private String wordDirection;
    private StringBuilder wordBuilder;
    private ObservableList<Button> buttonTiles;

    public GameViewModel() {
    }

    public void setGameMode(String MODE, int numOfPlayers) {
        if (MODE.equals("H")) {
            this.gameModel = HostModel.get();
            HostModel.get().addObserver(this);
            GameManager.get().setTotalPlayersCount(numOfPlayers);

        } else if (MODE.equals("G")) {
            this.gameModel = GuestModel.get();
            GuestModel.get().addObserver(this);
        } else {
            // PRINT DEBUG
            System.out.println("WRONG GAME MODE OPERATOR! USE H/G");
        }
    }

    public static ObservableList<String> getBookList() {
        List<String> sortedBookList = new ArrayList<>(GameManager.get().getFullBookList().keySet());
        Collections.sort(sortedBookList);
        return FXCollections.observableArrayList(sortedBookList);
    }

    public void connectMe(String name, String ip, int port) {
        try {
            gameModel.connectMe(name, ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void myBookChoice(String bookName) {
        gameModel.myBooksChoice(bookName);
    }

    public void ready() {
        gameModel.ready();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel) {
            // Update the ViewModel's state based on changes in the GameModel
            System.out.println("VIEW-MODEL : GOT UPDATE");

            buttonTiles = FXCollections.observableArrayList();
            wordBuilder = new StringBuilder();

            if (this.myNameView == null) {
                this.myNameView = new SimpleStringProperty(gameModel.getPlayerProperties().getMyName());
            }

            boolean myTurn = gameModel.isMyTurn();
            this.myTurnView = new SimpleStringProperty(String.valueOf(myTurn));
            if (myTurn) MessageReader.setMsg("It's your turn!");

            List<String> obsTiles = new ArrayList<>();
            for (Tile t : gameModel.getMyTiles()) {
                obsTiles.add(String.valueOf(t.getLetter()));
                buttonTiles.add(new Button(String.valueOf(t.getLetter())));
            }
            this.myTilesView = FXCollections.observableArrayList(obsTiles);

            List<String> obsWords = new ArrayList<>();
            for (Word w : gameModel.getMyWords()) {
                obsWords.add(w.toString());
            }
            this.myWordsView = FXCollections.observableArrayList(obsWords);

            List<String> obsOthers = new ArrayList<>();
            for (String n : gameModel.getOthersInfo().keySet()) {
                obsOthers.add(n + ":" + getPlayerProperties().getOtherPlayersInfo().get(n));
            }
            this.othersScoreView = FXCollections.observableArrayList(obsOthers);

            this.myScoreView = new SimpleStringProperty(String.valueOf(gameModel.getMyScore()));

            // for (int i = 0; i < Board.SIZE; i++) {
            //     for (int j = 0; j < Board.SIZE; j++) {
            //         if (getCurrentBoard()[i][j] == null)
            //             System.out.print("- ");
            //         else
            //             System.out.print(getCurrentBoard()[i][j].getLetter() + " ");
            //     }
            //     System.out.println();
            // }
            // System.out.println();

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

    public Tile[][] getCurrentBoard() {
        return gameModel.getCurrentBoard();
    }

    public void challenge() {
        gameModel.challenge();
    }

    public void skipTurn() {
        this.gameModel.skipTurn();
    }

    public void quitGame() {
        this.gameModel.quitGame();
    }

    public boolean isMyTurn() {
        return gameModel.isMyTurn();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    public ObservableList<Button> getButtonTiles() {
        return buttonTiles;
    }

    public String getWordDirection() {
        return wordDirection;
    }

    public void setWordDirection(String wordDirection) {
        this.wordDirection = wordDirection;
    }

    public String getWord() {
        return wordBuilder.toString();
    }

    public void addToWord(String tileValue) {
        wordBuilder.append(tileValue);
    }

    public void clearWord() {
        wordBuilder.setLength(0);
    }

    public void clearPlayerTiles() {
        buttonTiles.clear();
    }

    public void tryPlaceWord(String word, int firstRow, int firstCol, int lastRow, int lastCol) {
        // Handle the "Try Place Word" action with the collected data
        System.out.println("Word: " + word);
        System.out.println(firstRow + "," + firstCol);
        System.out.println(lastRow + "," + lastCol);

        if (firstRow > lastRow) {
            int tmp = firstRow;
            firstRow = lastRow;
            lastRow = tmp;
        }
        if (firstCol > lastCol) {
            int tmp = firstCol;
            firstCol = lastCol;
            lastCol = tmp;
        }
        boolean isVer = firstCol == lastCol ? true : false;
        int size = isVer ? lastRow - firstRow + 1 : lastCol - firstCol + 1;
        Tile[] tiles = new Tile[size];
        int j = 0;
        for (int i = 0; i < size; i++) {
            if (isVer) {
                if (getCurrentBoard()[firstRow + i][firstCol] != null) {
                    tiles[i] = null;
                } else {
                    for (Tile t : gameModel.getMyTiles()) {
                        if (t.getLetter() == word.toCharArray()[j]) {
                            tiles[i] = t;
                            j++;
                            break;
                        }
                    }
                }
            } else {
                if (getCurrentBoard()[firstRow][firstCol + i] != null) {
                    tiles[i] = null;
                } else {
                    for (Tile t : gameModel.getMyTiles()) {
                        if (t.getLetter() == word.toCharArray()[j]) {
                            tiles[i] = t;
                            j++;
                            break;
                        }
                    }
                }
            }
        }

        Word queryWord = new Word(tiles, firstRow, firstCol, isVer);
        System.out.println(queryWord);
        gameModel.tryPlaceWord(queryWord);

    }

    public void setFirstSelectedCellRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public void setFirstSelectedCellCol(int firstCol) {
        this.firstCol = firstCol;
    }

    public void setLastSelectedCellRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public void setLastSelectedCellCol(int lastCol) {
        this.lastCol = lastCol;
    }

    public void clearSelectedCells() {
        this.firstRow = 7;
        this.firstCol = 7;
        this.lastRow = 7;
        this.lastCol = 7;
    }

    public int getFirstSelectedCellRow() {
        return this.firstRow;
    }

    public int getFirstSelectedCellCol() {
        return this.firstCol;
    }

    public int getLastSelectedCellRow() {
        return this.lastRow;
    }

    public int getLastSelectedCellCol() {
        return lastCol;
    }

}
