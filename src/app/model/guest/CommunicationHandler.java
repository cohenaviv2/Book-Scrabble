package app.model.guest;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import app.model.game.ObjectSerializer;
import app.model.game.PlayerProperties;
import app.model.game.Tile;
import app.model.game.Word;
import app.view_model.MessageReader;

public class CommunicationHandler {
    private Socket hostSocket;
    BufferedReader in;
    PrintWriter out;
    private int myId;
    private String quitGameString;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    PlayerProperties playerProperties = GuestModel.get().getPlayerProperties();

    public CommunicationHandler(String ipString, int port) throws IOException {
        this.hostSocket = new Socket(ipString, port);
        this.in = new BufferedReader(new InputStreamReader(this.hostSocket.getInputStream()));
        this.out = new PrintWriter(this.hostSocket.getOutputStream(), true);
    }

    public void sendMessage(String modifier, String value) {
        out.println(myId + "," + modifier + "," + value);

    }

    public void connectMe(String name) {
        try {
            out.println("0,connectMe," + name);
            String[] ans = in.readLine().split(",");
            if (!ans[2].equals("0")) {
                this.myId = Integer.parseInt(ans[2]);
                this.quitGameString = myId + ",quitGame,true";
                // PRINT DEBUG
                System.out.println("CommHandler: got my id " + myId + ", " + name + " is Connected!");
            } else {
                throw new Exception("CommHandler - connectMe: wrong answer from Host server " + ans);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMyBookChoice(String book) {
        try {
            out.println(myId + ",myBookChoice," + book);
            String[] ans = in.readLine().split(",");
            int id = Integer.parseInt(ans[0]);
            String modifier = ans[1];
            String value = ans[2];
            if (id == myId && modifier.equals("myBookChoice") && value.equals("true")) {

                // PRINT DEBUG
                System.out.println("CommHandler: your book " + book + " is set up! starting chat...");
                startUpdateListener();

            } else {
                // PRINT DEBUG
                // System.out.println("CommHandler - addBookHandler: wrong answer from Host
                // server " + ans);
                throw new Exception("CommHandler - addBookHandler: wrong answer from Host server " + ans);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startUpdateListener() throws IOException {
        new Thread(() -> {
            try {
                String serverMessage;
                while (!(serverMessage = in.readLine()).equals(quitGameString)) {
                    if (serverMessage.equals("updateAll")) {
                        requestAllStates();
                    } else {
                        if (serverMessage.equals("ready"))
                            continue;
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

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void requestAllStates() {
        sendMessage("getCurrentBoard", "true");
        sendMessage("isMyTurn", "true");
        sendMessage("getMyWords", "true");
        sendMessage("getMyTiles", "true");
        sendMessage("getMyScore", "true");
        sendMessage("getOthersScore", "true");
        // while (playerProperties.getMyBoard() == null &&
        // playerProperties.getMyHandTiles() == null
        // && playerProperties.getMyWords() == null &&
        // playerProperties.getPlayersScore() == null)
        // ;
        // System.out.println("END OF BUSY WAITING - commHandler");
    }

    private void handleResponse(String modifier, String returnedVal) {
        switch (modifier) {
            case "getOthersScore":
                getOtherScoreHandler(returnedVal);
                break;
            case "getCurrentBoard":
                getCurrentBoardHandler(returnedVal);
                break;
            case "getMyScore":
                getMyScoreHandler(returnedVal);
                break;
            case "getMyTiles":
                getMyTilesHandler(returnedVal);
                break;
            case "getMyWords":
                getMyWordsHandler(returnedVal);
                break;
            case "isMyTurn":
                isMyTurnHandler(returnedVal);
                break;
            case "tryPlaceWord":
                tryPlaceWordHandler(returnedVal);
                break;
            case "challenge":
                challengeHandler(returnedVal);
                break;
            case "skipTurn":
                skipTurnHandler(returnedVal);
                break;
            case "quitGame":
                quitGameHandler(returnedVal);
                break;
            default:
                // PRINT DEBUG
                System.out.println("CommHandler: wrong instructions operator - " + modifier);
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

        } else if (returnedVal.equals("cantSerialize")) {

            // PRINT DEBUG
            System.out.println("CommHandler: " + returnedVal);

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

    private void getMyWordsHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get my words");
        } else if (returnedVal.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant serialize my words");
        } else {
            try {
                ArrayList<Word> word = (ArrayList<Word>) ObjectSerializer.deserializeObject(returnedVal);
                playerProperties.setMyWords(word);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getMyTilesHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get my tiles");
        } else if (returnedVal.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant serialize my tiles");
        } else {
            try {
                ArrayList<Tile> tile = (ArrayList<Tile>) ObjectSerializer.deserializeObject(returnedVal);
                playerProperties.setMyTiles(tile);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
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

    private void getCurrentBoardHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get board");
        } else if (returnedVal.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant serialize board");
        } else {
            try {
                Tile[][] board = (Tile[][]) ObjectSerializer.deserializeObject(returnedVal);
                playerProperties.setMyBoard(board);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getOtherScoreHandler(String returnedVal) {
        if (returnedVal.equals("false")) {
            // PRINT DEBUG
            System.out.println("CommHandler: cant get others Scores");
        } else if (returnedVal.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("CommHandler: " + returnedVal);
        } else {
            try {
                Map<String, Integer> othersScores = (Map<String, Integer>) ObjectSerializer
                        .deserializeObject(returnedVal);
                playerProperties.setPlayersScore(othersScores);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(this.playerProperties);
        GuestModel.get().update();

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
