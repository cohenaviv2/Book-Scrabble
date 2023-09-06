package app.view_model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import app.model.GameModel;
import app.model.GetMethod;
import app.model.game.*;
import app.model.guest.GuestModel;
import app.model.host.HostModel;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;

public class GameViewModel extends Observable implements Observer {

    public GameModel gameModel;

    private ObservableValue<String> myNameView;
    private final IntegerProperty scoreProp = new SimpleIntegerProperty(0);
    private ObservableValue<String> myScoreView;
    private ObservableValue<String> myTurnView;
    private ObservableList<String> myWordsView;
    private ObservableList<String> myTilesView;
    private ObservableList<Button> buttonTiles;
    private ObservableList<String> othersInfoView;
    private ObservableValue<String> bagCountView;
    private ObservableList<String> gameBooksView;

    private String update;
    private boolean isUpdate;
    private boolean isGameEnd;
    private String finalScores;

    private int firstRow;
    private int firstCol;
    private int lastRow;
    private int lastCol;
    private StringBuilder wordBuilder;

    public void setGameMode(String MODE) {
        if (MODE.equals("H")) {
            this.gameModel = HostModel.get();
            HostModel.get().addObserver(this);

        } else if (MODE.equals("G")) {
            this.gameModel = GuestModel.get();
            GuestModel.get().addObserver(this);
        } else {
            // PRINT DEBUG
            // System.out.println("WRONG GAME MODE OPERATOR! USE H/G");
        }
    }

    public boolean isValidPort(String portText) {
        try {
            int portNumber = Integer.parseInt(portText);
            return portNumber >= 0 && portNumber <= 65535 && portText.length() <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getLocalIpAddress() {
        String localIp = "";
        URL connection;
        try {
            connection = new URL("http://checkip.amazonaws.com/");
            URLConnection con = connection.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            localIp = reader.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return localIp;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    private void setUpdate(String msg) {
        this.isUpdate = true;
        this.update = msg;
    }

    public String getUpdate() {
        isUpdate = false;
        return update;
    }

    public boolean isGameEnd() {
        return isGameEnd;
    }

    public void setTotalPlayersCount(int numOfPlayers) {
        HostModel.get().setNumOfPlayers(numOfPlayers);
    }

    public static ObservableList<String> getBookList() {
        List<String> bookList = new ArrayList<>(GameModel.getFullBookList().keySet());
        Collections.shuffle(bookList); // Shuffle the list randomly
        return FXCollections.observableArrayList(bookList);
    }

    public void connectMe(String name, String ip, int port) {
        gameModel.connectMe(name, ip, port);
    }

    public void myBookChoice(List<String> myBooks) {
        gameModel.myBooksChoice(myBooks);
    }

    public void ready() {
        gameModel.ready();
    }

    public void sendTo(String name, String message) {
        gameModel.sendTo(name, message);
    }

    public void sendToAll(String message) {
        System.out.println("game model - " + message);
        gameModel.sendToAll(message);
    }

    public Tile[][] getCurrentBoard() {
        return gameModel.getPlayerProperties().getMyBoard();
    }

    public String tryPlaceWord(String word) {
        // Handle the "Try Place Word" action with the collected data
        // System.out.println("Word: " + word);
        // System.out.println(firstRow + "," + firstCol);
        // System.out.println(lastRow + "," + lastCol);

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
        // System.out.println(queryWord);
        gameModel.tryPlaceWord(queryWord);

        return null;

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
        return gameModel.getPlayerProperties().isMyTurn();
    }

    public boolean isConnected() {
        return this.gameModel.isConnected();
    }

    public String getFinalScores() {
        return finalScores;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel) {

            String updateMsg = (String) arg;
            System.out.println("\n GVM = " + updateMsg);
            setUpdate(updateMsg);

            // if (updateMsg.startsWith(GetMethod.sendTo)) {
            //     String values = updateMsg.split(",")[1];
            //     if(values.split(":").length==3) {
            //         String name = values.split(":")[0];
            //         if (name.equals(myNameView.getValue())) {
            //             setChanged();
            //             notifyObservers(updateMsg);
            //         }
            //     } else {
            //         String sender = values.split(":")[1];
            //         if(!sender.equals(myNameView.getValue())){
            //             setChanged();
            //             notifyObservers(updateMsg);
            //         }
            //     }

            // } else {
                if (updateMsg.startsWith(GetMethod.endGame) && !updateMsg.split(",")[1].equals("HOST")) {
                    // Game end properly
                    finalScores = updateMsg.split(",")[1];
                }

                if (!updateMsg.startsWith(GetMethod.tryPlaceWord)) {

                    // Update the ViewModel's state based on changes in the GameModel
                    // System.out.println("VIEW-MODEL : GOT UPDATE");
                    buttonTiles = FXCollections.observableArrayList();
                    wordBuilder = new StringBuilder();

                    // My name
                    if (this.myNameView == null) {
                        this.myNameView = new SimpleStringProperty(gameModel.getPlayerProperties().getMyName());
                    }

                    // My turn
                    boolean myTurn = gameModel.getPlayerProperties().isMyTurn();
                    this.myTurnView = new SimpleStringProperty(String.valueOf(myTurn));
                    if (myTurn)
                        MessageReader.setMsg("It's your turn!");

                    // My tiles
                    List<String> obsTiles = new ArrayList<>();
                    for (Tile t : gameModel.getPlayerProperties().getMyHandTiles()) {
                        obsTiles.add(String.valueOf(t.getLetter()));
                        buttonTiles.add(new Button(String.valueOf(t.getLetter())));
                    }
                    this.myTilesView = FXCollections.observableArrayList(obsTiles);

                    // My score
                    this.myScoreView = new SimpleStringProperty(
                            String.valueOf(gameModel.getPlayerProperties().getMyScore()));

                    this.scoreProp.set(gameModel.getPlayerProperties().getMyScore());

                    // My words
                    List<String> obsWords = new ArrayList<>();
                    for (Word w : gameModel.getPlayerProperties().getMyWords()) {
                        obsWords.add(w.toString());
                    }
                    this.myWordsView = FXCollections.observableArrayList(obsWords);

                    // Other player's info
                    List<String> obsOthers = new ArrayList<>();
                    Map<String, String> otherInfo = gameModel.getPlayerProperties().getOtherPlayersInfo();
                    for (String n : otherInfo.keySet()) {
                        obsOthers.add(n + ":" + otherInfo.get(n));
                    }
                    this.othersInfoView = FXCollections.observableArrayList(obsOthers);

                    // Game books
                    List<String> gameBookList = new ArrayList<>(gameModel.getPlayerProperties().getGameBookList());
                    this.gameBooksView = FXCollections.observableArrayList(gameBookList);

                    // Bag count
                    this.bagCountView = new SimpleStringProperty(String.valueOf(gameModel.getBagCount()));

                    // if (message.startsWith(GetMethod.quitGame) ||
                    // message.startsWith(GetMethod.endGame)) {
                    // setMessage(message);
                    // }
                    if (!isGameEnd) {
                        setChanged();
                        notifyObservers();
                    }
                    if (updateMsg.startsWith(GetMethod.endGame)) {
                        this.isGameEnd = true;
                    }

                    // System.out.println(this.gameModel.getPlayerProperties());
                }
            // }

        }
    }

    public ObservableValue<String> getPlayerNameProperty() {
        return myNameView;
    }

    public ObservableValue<String> getMyTurnView() {
        return myTurnView;
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

    public IntegerProperty playerScoreProperty() {
        return scoreProp;
    }

    /**************** Try Place Word ****************/

    public String getWord() {
        return wordBuilder.toString();
    }

    public void addToWord(String tileValue) {
        wordBuilder.append(tileValue);
    }

    public void clearWord() {
        wordBuilder.setLength(0);
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
