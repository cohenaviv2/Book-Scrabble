package app.model.guest;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import app.model.MethodInvoker;
import app.model.game.*;
import app.view_model.MessageReader;

public class CommunicationHandler {
    private Socket hostSocket;
    BufferedReader in;
    PrintWriter out;
    private int myId;
    private String QUIT_GAME_QUERY;
    ExecutorService executorService;
    PlayerProperties playerProperties;

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
        out.println("0" + "," + MethodInvoker.connectMe + "," + name);
        String[] ans = in.readLine().split(",");
        if (!ans[2].equals("0")) {
            this.myId = Integer.parseInt(ans[2]);
            this.QUIT_GAME_QUERY = myId + "," + MethodInvoker.quitGame + "," + "true";
            // PRINT DEBUG
            System.out.println("CommHandler: got my id " + myId + ", " + name + " is Connected!");
        } else {
            System.err.println("CommHandler - connectMe: wrong answer from Host server " + ans);
        }
    }

    public void addMyBookChoice(String myBooksSerilized) throws Exception {
        out.println(myId + "," + MethodInvoker.myBooksChoice + "," + myBooksSerilized);
        String[] ans = in.readLine().split(",");
        int id = Integer.parseInt(ans[0]);
        String modifier = ans[1];
        String value = ans[2];
        if (id == myId && modifier.equals(MethodInvoker.myBooksChoice) && value.equals("true")) {

            // PRINT DEBUG
            System.out.println("CommHandler: your book list is set up! starting chat...");
            startUpdateListener();

        } else {
            // PRINT DEBUG
            // System.out.println("CommHandler - addBookHandler: wrong answer from Host
            // server " + ans);
            throw new Exception("CommHandler - addBookHandler: wrong answer from Host server " + ans);

        }

    }

    public void startUpdateListener() throws IOException, ClassNotFoundException {
        executorService.submit(() -> {
            try {
                String serverMessage;
                while (!(serverMessage = in.readLine()).equals(QUIT_GAME_QUERY)) {
                    if (serverMessage.equals(MethodInvoker.updateAll)) {
                        System.out.println("COMM got update");
                        requestProperties();
                    } else {
                        if (serverMessage.equals(MethodInvoker.ready)) {
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
                // PRINT DEBUG
                System.out.println("CommHandler: you quit that game");

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

    }

    private void requestProperties() {
        sendMessage(MethodInvoker.getCurrentBoard, "true");
        sendMessage(MethodInvoker.isMyTurn, "true");
        sendMessage(MethodInvoker.getMyWords, "true");
        sendMessage(MethodInvoker.getMyTiles, "true");
        sendMessage(MethodInvoker.getMyScore, "true");
        sendMessage(MethodInvoker.getOthersInfo, "true");
        sendMessage(MethodInvoker.getGameBooks, "true");
        sendMessage(MethodInvoker.getBagCount, "true");
    }

    private void handleResponse(String modifier, String returnedVal) throws IOException, ClassNotFoundException {
        switch (modifier) {
            case MethodInvoker.getOthersInfo:
                getOtherInfoHandler(returnedVal);
                break;
            case MethodInvoker.getCurrentBoard:
                getCurrentBoardHandler(returnedVal);
                break;
            case MethodInvoker.getMyScore:
                getMyScoreHandler(returnedVal);
                break;
            case MethodInvoker.getMyTiles:
                getMyTilesHandler(returnedVal);
                break;
            case MethodInvoker.getMyWords:
                getMyWordsHandler(returnedVal);
                break;
            case MethodInvoker.getGameBooks:
                getGameBooksHandler(returnedVal);
                break;
            case MethodInvoker.getBagCount:
                getBagCountHandler(returnedVal);
                break;
            case MethodInvoker.isMyTurn:
                isMyTurnHandler(returnedVal);
                break;
            case MethodInvoker.tryPlaceWord:
                tryPlaceWordHandler(returnedVal);
                break;
            case MethodInvoker.challenge:
                challengeHandler(returnedVal);
                break;
            case MethodInvoker.skipTurn:
                skipTurnHandler(returnedVal);
                break;
            case MethodInvoker.quitGame:
                quitGameHandler(returnedVal);
                break;
            default:
                // PRINT DEBUG
                System.out.println("CommHandler: wrong instructions operator - " + modifier);
        }
    }

    private void getBagCountHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get bag count");
        } else {
            int bagCount = Integer.parseInt(returnedVal);
            playerProperties.setBagCount(bagCount);
        }
        // All props are set
        System.out.println(this.playerProperties);
        GuestModel.get().update();
    }

    private void getGameBooksHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (playerProperties.getGameBookList() == null) {
            if (returnedVal.equals("false")) {
                // PRINT DEBUG
                System.out.println("CommHandler: cant get my game books");
            } else {
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
            // PRINT DEBUG
            System.out.println("CommHandler: you skipped your turn");

        } else if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant skip your turn/it is not your turn");

        }
    }

    private void challengeHandler(String returnedVal) {
        if (returnedVal.equals("false")) {

            // PRINT DEBUG
            System.out.println("challenge - some error/turn");

        } else if (returnedVal.equals("skipTurn")) {

            // PRINT DEBUG
            System.out.println("challenge failed - skiping turn!");
            MessageReader.setMsg("Challenge failed, You lose 10 points");

        } else {
            // PRINT DEBUG
            System.out.println("challenge success - you got extra points!");
            MessageReader.setMsg("Challenge was successful!\nYou got more Points!");

        }
    }

    private void tryPlaceWordHandler(String returnedVal) {
        if (returnedVal.equals("false")) {

            // PRINT DEBUG
            System.out.println("CommHandler: its not your turn");

        } else if (returnedVal.equals("notBoardLegal")) {

            // PRINT DEBUG
            System.out.println("CommHandler: " + returnedVal);
            MessageReader.setMsg("Word's not Board legal, Try again");

        } else if (returnedVal.equals("notDictionaryLegal")) {

            // PRINT DEBUG
            System.out.println("CommHandler: " + returnedVal);

            MessageReader.setMsg("Some word is not Dictionary legal\nYou can try Challenge or Pass turn");
        } else {
            // PRINT DEBUG
            System.out.println("CommHandler: you get points");
            MessageReader.setMsg("You got more Points!");

        }
    }

    private void isMyTurnHandler(String returnedVal) {
        if (returnedVal.equals("true")) {
            playerProperties.setMyTurn(true);
            // PRINT DEBUG
            System.out.println("CommHandler: your turn - TRUE");

            MessageReader.setMsg("It's your turn!");

        } else if (returnedVal.equals("false")) {
            playerProperties.setMyTurn(false);
            // PRINT DEBUG
            System.out.println("CommHandler: your turn - FALSE");

        } else {
            // PRINT DEBUG
            System.out.println("CommHandler: wrong answer from Host server " + returnedVal);

        }
    }

    private void getMyWordsHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get my words");
        } else {
            ArrayList<Word> word = (ArrayList<Word>) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyWords(word);

        }
    }

    private void getMyTilesHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get my tiles");
        } else {
            ArrayList<Tile> tile = (ArrayList<Tile>) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyTiles(tile);
        }
    }

    private void getMyScoreHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get score");
        } else {
            int score = Integer.parseInt(returnedVal);
            playerProperties.setMyScore(score);
        }
    }

    private void getCurrentBoardHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get board");
        } else {
            Tile[][] board = (Tile[][]) ObjectSerializer.deserializeObject(returnedVal);
            playerProperties.setMyBoard(board);
        }
    }

    private void getOtherInfoHandler(String returnedVal) throws ClassNotFoundException, IOException {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get others Scores");
        } else {
            Map<String, String> othersScores = (Map<String, String>) ObjectSerializer
                    .deserializeObject(returnedVal);
            playerProperties.setPlayersInfo(othersScores);
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
