package app.view_model;

import java.io.IOException;
import java.util.*;

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
    private ObservableList<Button> buttonTiles;
    private ObservableList<String> othersInfoView;
    private ObservableValue<String> bagCountView;
    private ObservableList<String> gameBooksView;

    private int firstRow;
    private int firstCol;
    private int lastRow;
    private int lastCol;
    private int wordLength;
    private StringBuilder wordBuilder;

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

    public void connectMe(String name, String ip, int port) throws IOException {
        gameModel.connectMe(name, ip, port);
    }

    public void myBookChoice(String bookName) {
        gameModel.myBooksChoice(bookName);
    }

    public void ready() {
        gameModel.ready();
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

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel) {
            // Update the ViewModel's state based on changes in the GameModel
            System.out.println("VIEW-MODEL : GOT UPDATE");

            buttonTiles = FXCollections.observableArrayList();
            wordBuilder = new StringBuilder();

            // My name
            if (this.myNameView == null) {
                this.myNameView = new SimpleStringProperty(gameModel.getPlayerProperties().getMyName());
            }

            // My turn
            boolean myTurn = gameModel.isMyTurn();
            this.myTurnView = new SimpleStringProperty(String.valueOf(myTurn));
            if (myTurn)
                MessageReader.setMsg("It's your turn!");

            // My tiles
            List<String> obsTiles = new ArrayList<>();
            for (Tile t : gameModel.getMyTiles()) {
                obsTiles.add(String.valueOf(t.getLetter()));
                buttonTiles.add(new Button(String.valueOf(t.getLetter())));
            }
            this.myTilesView = FXCollections.observableArrayList(obsTiles);

            // My words
            List<String> obsWords = new ArrayList<>();
            for (Word w : gameModel.getMyWords()) {
                obsWords.add(w.toString());
            }
            this.myWordsView = FXCollections.observableArrayList(obsWords);

            // Other player's info
            List<String> obsOthers = new ArrayList<>();
            for (String n : gameModel.getOthersInfo().keySet()) {
                obsOthers.add(n + ":" + gameModel.getPlayerProperties().getOtherPlayersInfo().get(n));
            }
            this.othersInfoView = FXCollections.observableArrayList(obsOthers);

            this.myScoreView = new SimpleStringProperty(String.valueOf(gameModel.getMyScore()));

            setChanged();
            notifyObservers();

        }
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

    public ObservableList<String> getPlayerWordsProperty() {
        return myWordsView;
    }

    public ObservableList<String> getPlayerTilesProperty() {
        return myTilesView;
    }

    public ObservableList<String> getOthersInfoProperty() {
        return othersInfoView;
    }

    public ObservableValue<String> getBagCountProperty() {
        return bagCountView;
    }

    public ObservableList<String> getGameBooksProperty() {
        return gameBooksView;
    }

    public ObservableList<Button> getButtonTilesProperty() {
        return buttonTiles;
    }

    ////////////////////////////////// TRY PLACE WORD
    ////////////////////////////////// ///////////////////////////////////

    public String getWord() {
        return wordBuilder.toString();
    }

    public void addToWord(String tileValue) {
        wordBuilder.append(tileValue);
    }

    public void clearWord() {
        wordBuilder.setLength(0);
    }

    public void tryPlaceWord(String word) {
        // Handle the "Try Place Word" action with the collected data
        System.out.println("Word: " + word);
        System.out.println(firstRow + "," + firstCol);
        System.out.println(lastRow + "," + lastCol);

        // if (firstRow > lastRow) {
        // int tmp = firstRow;
        // firstRow = lastRow;
        // lastRow = tmp;
        // }
        // if (firstCol > lastCol) {
        // int tmp = firstCol;
        // firstCol = lastCol;
        // lastCol = tmp;
        // }

        boolean isVer = isWordVertical();
        int wordLen = getWordLength();
        int f_Row = getFirstSelectedCellRow();
        int f_Col = getFirstSelectedCellCol();

        Tile[] tiles = new Tile[wordLen];
        int j = 0;
        for (int i = 0; i < wordLen; i++) {
            if (isVer) {
                if (getCurrentBoard()[f_Row + i][f_Col] != null) {
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
                if (getCurrentBoard()[f_Row][f_Col + i] != null) {
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

        Word queryWord = new Word(tiles, f_Row, f_Col, isVer);
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
        return firstRow < lastRow ? firstRow : lastRow;
    }

    public int getFirstSelectedCellCol() {
        return firstCol < lastCol ? firstCol : lastCol;
    }

    public int getLastSelectedCellRow() {
        return firstRow > lastRow ? firstRow : lastRow;
    }

    public int getLastSelectedCellCol() {
        return firstCol > lastCol ? firstCol : lastCol;
    }

    public boolean isWordVertical() {
        return firstCol == lastCol ? true : false;
    }

    public int getWordLength() {
        return isWordVertical() ? (getLastSelectedCellRow() - getFirstSelectedCellRow() + 1)
                : (getLastSelectedCellCol() - getFirstSelectedCellCol() + 1);
    }

}
