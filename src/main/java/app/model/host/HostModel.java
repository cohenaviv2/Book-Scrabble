package app.model.host;

import java.io.IOException;
import java.util.*;

import app.model.GameModel;
import app.model.GetMethod;
import app.model.game.*;
import app.model.server.*;
import javafx.concurrent.Task;

public class HostModel extends Observable implements GameModel, Observer {

    private static HostModel hm = null; // Instance
    public static final int HOST_SERVER_PORT = 8040;
    private MyServer hostServer; // Host server - support connection of up to 3 guests parallel
    private GameManager gameManager; // game manager contains all the game data & logic
    private PlayerProperties playerProperties; // All player properties for the game view

    private HostModel() {
        /* Sets the host server with default port 8040 */
        this.hostServer = new MyServer(HOST_SERVER_PORT, new GuestHandler());
        this.gameManager = GameManager.get();
        this.gameManager.addObserver(this);
    }

    public static HostModel get() {
        if (hm == null) {
            hm = new HostModel();
        }
        return hm;
    }

    // public void stopHostServer() {
    //     hostServer.close();
    // }

    // public void startHostServer() {
    //     if (!hostServer.isRunning()) {
    //         hostServer.start();
    //     }
    // }

    public void setNumOfPlayers(int numOftotalPlayers) {
        /*
         * Sets The total players of this game.
         * The host need to choose game mode, it can be 2,3 or 4 total players in the
         * game (including the host player).
         */
        this.gameManager.setTotalPlayersCount(numOftotalPlayers);
    }

    public boolean isGameServerConnect() {
        return this.gameManager.isGameServerConnect();
    }

    @Override
    public void connectMe(String name, String ip, int port) {
        /*
         * Sets the game server ip and port (Game Manager)
         * game server is local for now, hence the ip field should be "localhost".
         * game server is responsible for checking whether a word is dirctionary legal
         * or not
         * also creates host player profile and sets the Game Properties
         */

        if (port != 0 && port != HostModel.HOST_SERVER_PORT) {
            // this.hostServer.close();
            this.hostServer = new MyServer(port, new GuestHandler());
        }
        this.hostServer.start();
        gameManager.createHostPlayer(name);
        playerProperties = PlayerProperties.get();
        playerProperties.setMyName(name);

    }

    @Override
    public void myBooksChoice(List<String> myBooks) {
        /*
         * Adds this book to the book list of the game
         * each player chooses one book (Maximum 4)
         */
        try {
            String myBooksSerilized = ObjectSerializer.serializeObject(myBooks);
            gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.myBooksChoice,
                    myBooksSerilized);
        } catch (ClassNotFoundException | IOException e) {
        }
    }

    @Override
    public void ready() {
        gameManager.setReady();
    }

    @Override
    public void tryPlaceWord(Word myWord) {
        if (playerProperties.isMyTurn()) {
            try {
                String queryWord = ObjectSerializer.serializeObject(myWord);
                String ans = gameManager.processPlayerInstruction(gameManager.getHostID(),
                        GetMethod.tryPlaceWord,
                        queryWord);

                if (ans.equals("notBoardLegal")) {
                    setChanged();
                    notifyObservers(GetMethod.tryPlaceWord + "," + "notBoardLegal");

                } else {
                    setChanged();
                    notifyObservers(GetMethod.tryPlaceWord + "," + ans);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void challenge() {
        if (playerProperties.isMyTurn()) {
            try {
                String ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.challenge,
                        "true");

                setChanged();
                notifyObservers(GetMethod.challenge + "," + ans);
            } catch (ClassNotFoundException | IOException e) {
            }
        }
    }

    @Override
    public void skipTurn() {
        if (playerProperties.isMyTurn()) {
            try {
                gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.skipTurn, "true");
            } catch (ClassNotFoundException | IOException e) {
            }
        }
    }

    @Override
    public void quitGame() {
        if (this.hostServer.getNumOfClients() > 0) {
            String quitGameMod = String.valueOf(gameManager.getHostID()) + "," + GetMethod.quitGame + "," + "true";
            this.gameManager.quitGameHandler(quitGameMod);

            // Run the waiting logic in a background task
            Task<Void> waitingTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Sleep for 3 seconds
                    Thread.sleep(3000);
                    return null;
                }
            };

            waitingTask.setOnSucceeded(event -> {
                if (this.hostServer.getNumOfClients() == 0) {
                    this.hostServer.close();
                    setChanged();
                    notifyObservers(GetMethod.exit);
                    System.out.println("\n0 Clients - Host server is closed, and the host has quit the game");
                } else {
                    System.out.println(
                            "\n\n*** There are still clients connected ! ***\n\n" + hostServer.getNumOfClients());
                }
            });

            new Thread(waitingTask).start(); // Start the task in a separate thread
        } else {
            this.hostServer.close();
            System.out.println("\nNo clients - Host server is closed, and the host has quit the game");
        }
    }

    private void updateProperties() {
        getCurrentBoard();
        isMyTurn();
        getMyTiles();
        getMyScore();
        getMyWords();
        getOthersInfo();
        getGameBooks();
        getBagCount();
    }

    @Override
    public Map<String, String> getOthersInfo() {
        try {
            String ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getOthersInfo,
                    "true");
            if (ans.equals("false")) {
                return null;
            } else {
                @SuppressWarnings(value = "unchecked")
                Map<String, String> othersInfo = (Map<String, String>) ObjectSerializer.deserializeObject(ans);
                playerProperties.setPlayersInfo(othersInfo);
                return playerProperties.getOtherPlayersInfo();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getMyScore() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getMyScore,
                    "true");
            if (ans.equals("false")) {
                return 0;
            } else {
                int score = Integer.parseInt(ans);
                playerProperties.setMyScore(score);
                return playerProperties.getMyScore();
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return 0;
    }

    @Override
    public boolean isMyTurn() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.isMyTurn,
                    "true");
            if (ans.equals("false")) {
                playerProperties.setMyTurn(false);
                return playerProperties.isMyTurn();

            } else if (ans.equals("true")) {

                playerProperties.setMyTurn(true);
                return playerProperties.isMyTurn();

            } else {
                return false;
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return false;
    }

    @Override
    public PlayerProperties getPlayerProperties() {
        return PlayerProperties.get();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getCurrentBoard,
                    "true");
            if (ans.equals("false")) {
                return null;
            } else if (ans.equals("cantSerialize")) {
                return null;
            } else {
                try {
                    Tile[][] board = (Tile[][]) ObjectSerializer.deserializeObject(ans);
                    playerProperties.setMyBoard(board);
                    return playerProperties.getMyBoard();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return null;
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getMyTiles,
                    "true");
            if (ans.equals("false")) {
                return null;
            } else if (ans.equals("cantSerialize")) {
                return null;
            } else {
                try {
                    @SuppressWarnings(value = "unchecked")
                    ArrayList<Tile> tiles = (ArrayList<Tile>) ObjectSerializer.deserializeObject(ans);
                    playerProperties.setMyTiles(tiles);
                    return playerProperties.getMyHandTiles();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return null;
    }

    @Override
    public ArrayList<Word> getMyWords() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getMyWords,
                    "true");
            if (ans.equals("false")) {
                return null;
            } else if (ans.equals("cantSerialize")) {
                return null;
            } else {
                try {
                    @SuppressWarnings(value = "unchecked")
                    ArrayList<Word> words = (ArrayList<Word>) ObjectSerializer.deserializeObject(ans);
                    playerProperties.setMyWords(words);
                    return playerProperties.getMyWords();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return null;
    }

    @Override
    public Set<String> getGameBooks() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getGameBooks,
                    "true");
            if (ans.equals("false")) {
                return null;
            } else if (ans.equals("cantSerialize")) {
                return null;
            } else {
                try {
                    @SuppressWarnings(value = "unchecked")
                    Set<String> gameBooks = (Set<String>) ObjectSerializer.deserializeObject(ans);
                    playerProperties.setGameBookList(gameBooks);
                    return playerProperties.getGameBookList();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return null;
    }

    @Override
    public int getBagCount() {
        String ans;
        try {
            ans = gameManager.processPlayerInstruction(gameManager.getHostID(), GetMethod.getBagCount,
                    "true");
            if (ans.equals("false")) {
                return 0;
            } else {
                int bagCount = Integer.parseInt(ans);
                playerProperties.setBagCount(bagCount);
                return playerProperties.getBagCount();
            }
        } catch (ClassNotFoundException | IOException e) {
        }
        return 0;
    }

    public void resetGame() {
        gameManager.resetGame();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameManager) {
            // String message = (String) arg;
            // if(message.startsWith(GetMethod.updateAll)){
            // hostServer.sendToAll(message);
            // }
            // if (message.startsWith(GetMethod.sendTo)) {
            // checkMessage(message);
            // } else {
            // updateProperties();
            // setChanged();
            // notifyObservers(message);
            // }
            String update = (String) arg;
            if (update.startsWith(GetMethod.updateAll)) {
                hostServer.sendToAll(update);
                updateProperties();
                setChanged();
                notifyObservers(update);

            } else if (update.startsWith(GetMethod.sendTo)) {
                hostServer.sendToAll(update);
                String message = update.split(",")[1];
                checkForMessage(message);
            } else if (update.startsWith("GAME-SERVER-ERROR")) {
                System.out.println(update);
            } else {
                setChanged();
                notifyObservers(update);
            }
        }
    }

    private void checkForMessage(String update) {
        if (update.startsWith(playerProperties.getMyName()) || update.startsWith("All")) {
            setChanged();
            notifyObservers(update);
        }
    }

    @Override
    public boolean isConnected() {
        if (!hostServer.isRunning()) {
            return false;
        } else if (!isGameServerConnect()) {
            return false;
        } else
            return true;
    }

    @Override
    public void sendTo(String name, String message) {
        hostServer.sendToAll(GetMethod.sendTo + "," + name + ":" + message + ":" + playerProperties.getMyName());
    }

    @Override
    public void sendToAll(String message) {
        hostServer.sendToAll(GetMethod.sendToAll + "," + "All" + ":" + message + ":" + playerProperties.getMyName());
    }

}
