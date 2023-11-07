package app.model.guest;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import app.model.GetMethod;
import app.model.game.*;

public class CommunicationHandler extends Observable {
    private Socket hostSocket;
    private BufferedReader in;
    private PrintWriter out;
    private int myId;
    private boolean flag;
    private String MESSAGE;
    private String QUIT_GAME_QUERY;
    private ExecutorService executorService;
    private PlayerProperties playerProperties;

    public CommunicationHandler(String ipString, int port) throws IOException {
        this.hostSocket = new Socket(ipString, port);
        this.in = new BufferedReader(new InputStreamReader(this.hostSocket.getInputStream()));
        this.out = new PrintWriter(this.hostSocket.getOutputStream(), true);
        this.executorService = Executors.newSingleThreadExecutor();
        this.playerProperties = GuestModel.get().getPlayerProperties();
    }

    public void sendMessage(String modifier, String value) {
        out.println(myId + "," + modifier + "," + value);

    }

    public void connectMe(String name) throws IOException {
        out.println("0" + "," + GetMethod.connectMe + "," + name);
        String[] ans = in.readLine().split(",");
        if (!ans[2].equals("0")) {
            this.myId = Integer.parseInt(ans[2]);
            this.QUIT_GAME_QUERY = myId + "," + GetMethod.quitGame + "," + "true";
            // Connected!");
            flag = false;
        } else {
            // System.err.println("CommHandler - connectMe: wrong answer from Host server "
            // + ans);
            flag = true;
            close();
        }
    }

    public void addMyBookChoice(List<String> myBooks) throws Exception {
        if (!flag) {

            String myBooksSerilized = ObjectSerializer.serializeObject(myBooks);
            out.println(myId + "," + GetMethod.myBooksChoice + "," + myBooksSerilized);
            String[] ans = in.readLine().split(",");
            int id = Integer.parseInt(ans[0]);
            String modifier = ans[1];
            String value = ans[2];
            if (id == myId && modifier.equals(GetMethod.myBooksChoice) && value.equals("true")) {

                // chat...");
                startUpdateListener();

            } else {
                // erver " + ans);
                // throw new Exception("CommHandler - addBookHandler: wrong answer from Host
                // server " + ans);

            }
        }

    }

    public void startUpdateListener() throws IOException, ClassNotFoundException {
        executorService.submit(() -> {
            try {
                String serverMessage;
                while (!(serverMessage = in.readLine()).equals(QUIT_GAME_QUERY)) {
                    // General update message
                    if (!serverMessage.startsWith(String.valueOf(myId))) {
                        MESSAGE = serverMessage;
                        if (serverMessage.startsWith(GetMethod.updateAll)) {
                            requestProperties();
                        } else if (serverMessage.startsWith(GetMethod.sendTo)) {
                            String message = serverMessage.split(",")[1];
                            checkForMessage(message);
                        } else if (serverMessage.startsWith(GetMethod.endGame)) {
                            endGameHandler(serverMessage);
                        }
                    }

                    // Private update
                    else {
                        if (serverMessage.equals(GetMethod.ready)) {
                            continue;
                        }
                        String[] params = serverMessage.split(",");
                        int messageId = Integer.parseInt(params[0]);
                        String modifier = params[1];
                        String returnedVal = params[2];

                        // if it's not my id, Drop maessage
                        if (messageId == myId) {
                            handleResponse(modifier, returnedVal);
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

    }

    private void requestProperties() {
        sendMessage(GetMethod.getCurrentBoard, "true");
        sendMessage(GetMethod.isMyTurn, "true");
        sendMessage(GetMethod.getMyWords, "true");
        sendMessage(GetMethod.getMyTiles, "true");
        sendMessage(GetMethod.getMyScore, "true");
        sendMessage(GetMethod.getOthersInfo, "true");
        sendMessage(GetMethod.getGameBooks, "true");
        sendMessage(GetMethod.getBagCount, "true");
    }

    private void handleResponse(String modifier, String returnedVal) throws IOException, ClassNotFoundException {
        switch (modifier) {
            case GetMethod.getOthersInfo:
                getOtherInfoHandler(returnedVal);
                break;
            case GetMethod.getCurrentBoard:
                getCurrentBoardHandler(returnedVal);
                break;
            case GetMethod.getMyScore:
                getMyScoreHandler(returnedVal);
                break;
            case GetMethod.getMyTiles:
                getMyTilesHandler(returnedVal);
                break;
            case GetMethod.getMyWords:
                getMyWordsHandler(returnedVal);
                break;
            case GetMethod.getGameBooks:
                getGameBooksHandler(returnedVal);
                break;
            case GetMethod.getBagCount:
                getBagCountHandler(returnedVal);
                break;
            case GetMethod.isMyTurn:
                isMyTurnHandler(returnedVal);
                break;
            case GetMethod.sendTo:
                sentToHandler(returnedVal);
                break;
            case GetMethod.sendToAll:
                sendToAllHandler(returnedVal);
                break;
            case GetMethod.tryPlaceWord:
                tryPlaceWordHandler(returnedVal);
                break;
            case GetMethod.challenge:
                challengeHandler(returnedVal);
                break;
            case GetMethod.skipTurn:
                skipTurnHandler(returnedVal);
                break;
            case GetMethod.quitGame:
                quitGameHandler(returnedVal);
                break;
            default:
        }
    }

    private void sentToHandler(String returnedVal) {

    }

    private void sendToAllHandler(String returnedVal) {

    }

    private void getGameBooksHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (playerProperties.getGameBookList() == null) {
            if (returnedVal.equals("false")) {
            } else {
                @SuppressWarnings(value = "unchecked")
                Set<String> gameBooks = (Set<String>) ObjectSerializer.deserializeObject(returnedVal);
                playerProperties.setGameBookList(gameBooks);
            }
        }
    }

    private void quitGameHandler(String returnedVal) {
    }

    private void skipTurnHandler(String returnedVal) {
        if (returnedVal.equals("true")) {
            playerProperties.setMyTurn(false);

        } else if (returnedVal.equals("false")) {
            // turn");

        }
    }

    private void challengeHandler(String returnedVal) {
            setChanged();
            notifyObservers(GetMethod.challenge + "," + returnedVal);
    }

    private void tryPlaceWordHandler(String returnedVal) {

        if (returnedVal.startsWith("notBoardLegal")) {

            setChanged();
            notifyObservers(GetMethod.tryPlaceWord + "," + "notBoardLegal");

        } else {

            setChanged();
            notifyObservers(GetMethod.tryPlaceWord + "," + returnedVal);

        }
    }

    private void isMyTurnHandler(String returnedVal) {
        if (returnedVal.equals("true")) {
            playerProperties.setMyTurn(true);

        } else if (returnedVal.equals("false")) {
            playerProperties.setMyTurn(false);

        } else {
            // returnedVal);

        }
    }

    private void getMyWordsHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
        } else {
            @SuppressWarnings(value = "unchecked")
            ArrayList<Word> word = (ArrayList<Word>) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyWords(word);

        }
    }

    private void getMyTilesHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
        } else {
            @SuppressWarnings(value = "unchecked")
            ArrayList<Tile> tile = (ArrayList<Tile>) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyTiles(tile);
        }
    }

    private void getMyScoreHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
        } else {
            int score = Integer.parseInt(returnedVal);
            playerProperties.setMyScore(score);
        }
    }

    private void getCurrentBoardHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
        } else {
            Tile[][] board = (Tile[][]) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyBoard(board);
        }
    }

    private void getOtherInfoHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
        } else {
            @SuppressWarnings(value = "unchecked")
            Map<String, String> othersScores = (Map<String, String>) ObjectSerializer
                    .deserializeObject(returnedVal);
            playerProperties.setPlayersInfo(othersScores);
        }
    }

    private void getBagCountHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
        } else {
            int bagCount = Integer.parseInt(returnedVal);
            playerProperties.setBagCount(bagCount);
        }
        // All props are set
        setChanged();
        notifyObservers((MESSAGE != null) ? MESSAGE : GetMethod.updateAll);
        this.MESSAGE = null;
    }

    private void endGameHandler(String message) {
        setChanged();
        notifyObservers(message);
        sendMessage(GetMethod.quitGame, "true");
        GuestModel.get().setIsConnected(false);
    }

    private void checkForMessage(String update) {
        if (update.startsWith(playerProperties.getMyName()) || update.startsWith("All")) {
            setChanged();
            notifyObservers(update);
        }
    }

    public void close() {
        try {
            this.in.close();
            this.out.close();
            this.hostSocket.close();
            this.executorService.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
