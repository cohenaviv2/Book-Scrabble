package app.model.host;

import java.io.IOException;
import java.util.*;

import app.model.GameModel;
import app.model.game.*;
import app.model.server.*;
import app.view_model.MessageReader;

public class HostModel extends Observable implements GameModel, Observer {

    private static HostModel hm = null; // Singleton
    private static final int HOST_PORT = 8040;
    public MyServerParallel hostServer; // Host server - support connection of up to 3 guests parallel
    private GameManager gameManager; // game manager contains all the game data and logic
    private PlayerProperties playerProperties; // All player properties for the game view

    private HostModel() {
        /* starts the host server on port 8040 */
        this.hostServer = new MyServerParallel(HOST_PORT, new GuestHandler());
        this.hostServer.start();
        this.gameManager = GameManager.get();
        this.gameManager.addObserver(this);
    }

    public static HostModel get() {
        if (hm == null)
            hm = new HostModel();
        return hm;
    }

    public void stopHostServer(){
        hostServer.close();
    }

    public void setNumOfPlayers(int numOftotalPlayers) {
        /*
         * Sets The total players of this game.
         * The host need to choose game mode, it can be 2,3 or 4 total players in the
         * game (including the host player).
         */

        try {
            this.gameManager.setTotalPlayersCount(numOftotalPlayers);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        gameManager.setGameServerSocket(ip, port);
        gameManager.createHostPlayer(name);
        playerProperties = PlayerProperties.get();
        playerProperties.setMyName(name);

        System.out.println("HOST: " + name + " is Connected to the game server!");

    }

    @Override
    public void myBooksChoice(String bookName) {
        /*
         * Adds this book to the book list of the game
         * each player chooses one book (Maximum 4)
         */
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "myBookChoice", bookName);

        // PRINT DEBUG
        // System.out.println("HostModel: myBookChoice ans = " + ans);
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
                String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "tryPlaceWord",
                        queryWord);
                if (ans.equals("false")) {

                    // PRINT DEBUG
                    System.out.println("tryPlaceWord - some error/turn");

                } else if (ans.equals("notBoardLegal")) {

                    // PRINT DEBUG
                    System.out.println("Word's not Board legal, Try again");
                    MessageReader.setMsg("notBoardLegal");

                } else if (ans.equals("notDictionaryLegal")) {

                    // PRINT DEBUG
                    System.out.println("Some word is not Dictionary legal\nYou can try Challenge or Pass turn");

                    MessageReader.setMsg("notDictionaryLegal");

                } else if (ans.equals("cantSerialize")) {

                    // PRINT DEBUG
                    System.out.println("tryPlaceWord - cant serialize");

                } else {

                    // PRINT DEBUG
                    System.out.println("You got more points!");
                    MessageReader.setMsg("You got more points!");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void challenge() {
        if (playerProperties.isMyTurn()) {
            String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "challenge", "true");
            if (ans.equals("false")) {

                // PRINT DEBUG
                System.out.println("challenge - some error/turn");

            } else if (ans.equals("skipTurn")) {

                // PRINT DEBUG
                System.out.println("some word that was made is not dictionary legal - skiping turn!");
                MessageReader.setMsg("Challenge failed, You lose 10 points");

            } else {

                // PRINT DEBUG
                System.out.println("Challenge was successful!\nYou got more Points!");
                MessageReader.setMsg("Challenge was successful!\nYou got more Points!");

            }
        }
    }

    @Override
    public void skipTurn() {
        if (playerProperties.isMyTurn()) {

            String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "skipTurn", "true");

            if (ans.equals("true")) {
                // playerProperties.setMyTurn(false);
                // PRINT DEBUG
                System.out.println("Your turn is skipped");
            } else if (ans.equals("false")) {
                // PRINT DEBUG
                System.out.println("Cant skip your turn");
            } else {
                // PRINT DEBUG
                System.out.println("skipTurn - wrong answer from GM");
            }
        }
    }

    @Override
    public void quitGame() {
        this.hostServer.close();
        System.out.println("Host server is closed and the host has quit the game");
    }

    @Override
    public Map<String, Integer> getOthersScore() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "getOthersScore", "true");
        if (ans.equals("false")) {
            // PRINT DEBUG
            System.out.println("Cant get your others scores");
            return null;
        } else {
            Map<String, Integer> othersScores;
            try {
                othersScores = (Map<String, Integer>) ObjectSerializer.deserializeObject(ans);
                return othersScores;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            // PRINT DEBUG
            System.out.println("Cant serialize");
            return null;
        }
    }

    @Override
    public int getMyScore() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "getMyScore", "true");
        if (ans.equals("false")) {
            return 0;
        } else {
            return Integer.parseInt(ans);
        }
    }

    @Override
    public boolean isMyTurn() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "isMyTurn", "true");
        if (ans.equals("false")) {
            return false;
        } else if (ans.equals("true")) {
            MessageReader.setMsg("It's your turn!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PlayerProperties getPlayerProperties() {
        return PlayerProperties.get();
    }

    @Override
    public Tile[][] getCurrentBoard() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "getCurrentBoard", "true");
        if (ans.equals("false")) {
            // PRINT DEBUG
            System.out.println("cant get board");
            return null;
        } else if (ans.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("Cant serialize");
            return null;
        } else {
            try {
                Tile[][] board = (Tile[][]) ObjectSerializer.deserializeObject(ans);
                return board;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            // PRINT DEBUG
            System.out.println("Cant deserialize");
            return null;
        }
    }

    @Override
    public ArrayList<Tile> getMyTiles() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "getMyTiles", "true");
        if (ans.equals("false")) {
            // PRINT DEBUG
            System.out.println("cant get your tiles");
            return null;
        } else if (ans.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("Cant serialize");
            return null;
        } else {
            try {
                ArrayList<Tile> tiles = (ArrayList<Tile>) ObjectSerializer.deserializeObject(ans);
                return tiles;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            // PRINT DEBUG
            System.out.println("Cant deserialize");
            return null;
        }
    }

    @Override
    public ArrayList<Word> getMyWords() {
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "getMyWords", "true");
        if (ans.equals("false")) {
            // PRINT DEBUG
            System.out.println("cant get your words");
            return null;
        } else if (ans.equals("cantSerialize")) {
            // PRINT DEBUG
            System.out.println("Cant serialize");
            return null;
        } else {
            try {
                ArrayList<Word> words = (ArrayList<Word>) ObjectSerializer.deserializeObject(ans);
                return words;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            // PRINT DEBUG
            System.out.println("Cant deserialize");
            return null;
        }
    }

    private void updateAllStates() {
        this.playerProperties.setMyBoard(getCurrentBoard());
        this.playerProperties.setMyTiles(getMyTiles());
        this.playerProperties.setMyScore(getMyScore());
        this.playerProperties.setMyWords(getMyWords());
        this.playerProperties.setPlayersScore(getOthersScore());
        this.playerProperties.setMyTurn(isMyTurn());
        System.out.println(playerProperties);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameManager) {
            this.hostServer.sendToAll("updateAll");
            updateAllStates();
            setChanged();
            notifyObservers();
        }
    }

}
