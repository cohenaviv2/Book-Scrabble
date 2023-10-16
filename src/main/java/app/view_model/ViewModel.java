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
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ViewModel extends Observable implements Observer {
    private GameModel gameModel;

    private StringProperty myNameProperty;
    private ObjectProperty<Tile[][]> currentBoardProperty;
    private ListProperty<Tile> myTilesProperty;
    private ListProperty<String> myWordsProperty;
    private IntegerProperty myScoreProperty;
    private BooleanProperty myTurnProperty;
    private MapProperty<String, String> othersInfoProperty;
    private SetProperty<String> gameBooksProperty;
    private IntegerProperty bagCountProperty;

    private BooleanProperty connectedProperty;

    private int firstRow;
    private int firstCol;
    private int lastRow;
    private int lastCol;
    private StringBuilder wordBuilder;

    private boolean isGameEnd;

    public void initialize(boolean isHost) {
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

        wordBuilder = new StringBuilder();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameModel && arg instanceof String) {

            Platform.runLater(() -> {
                String message = (String) arg;

                System.out.println("View Model : "+message);

                myNameProperty.set(gameModel.getPlayerProperties().getMyName());
                currentBoardProperty.set(gameModel.getCurrentBoard());
                myTilesProperty.setAll(gameModel.getMyTiles());
                myScoreProperty.set(gameModel.getMyScore());
                List<String> myWordsList = gameModel.getMyWords().stream().map(w -> w.toString())
                        .collect(Collectors.toList());
                myWordsProperty.setAll(myWordsList);
                myTurnProperty.set(gameModel.isMyTurn());
                othersInfoProperty.putAll(gameModel.getOthersInfo());
                gameBooksProperty.addAll(gameModel.getGameBooks());
                bagCountProperty.set(gameModel.getBagCount());

                setChanged();
                notifyObservers(message);
            });

        }
    }

    public boolean isGameEnd() {
        return isGameEnd;
    }

    public StringProperty myNameProperty() {
        return myNameProperty;
    }

    public BooleanProperty connectedProperty() {
        return connectedProperty;
    }

    public ObjectProperty<Tile[][]> currentBoardProperty() {
        return currentBoardProperty;
    }

    public ListProperty<Tile> myTilesProperty() {
        return myTilesProperty;
    }

    public IntegerProperty myScoreProperty() {
        return myScoreProperty;
    }

    public ListProperty<String> myWordsProperty() {
        return myWordsProperty;
    }

    public BooleanProperty myTurnProperty() {
        return myTurnProperty;
    }

    public MapProperty<String, String> othersInfoProperty() {
        return othersInfoProperty;
    }

    public SetProperty<String> gameBooksProperty() {
        return gameBooksProperty;
    }

    public IntegerProperty bagCountProperty() {
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

    public void tryPlaceWord(String word) {
        boolean isVer = isWordVertical();
        int wordLen = getWordLength();
        int f_Row = getFirstSelectedCellRow();
        int f_Col = getFirstSelectedCellCol();

        Tile[] tiles = new Tile[wordLen];
        int j = 0;
        for (int i = 0; i < wordLen; i++) {
            if (isVer) {
                if (gameModel.getCurrentBoard()[f_Row + i][f_Col] != null) {
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
                if (gameModel.getCurrentBoard()[f_Row][f_Col + i] != null) {
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

    public static ObservableList<String> getBookList() {
        List<String> bookList = new ArrayList<>(GameModel.getFullBookList().keySet());
        Collections.shuffle(bookList); // Shuffle the list randomly
        return FXCollections.observableArrayList(bookList);
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
