package model.host;

import model.GameModel;
import model.game.*;
import model.server.*;
import java.io.IOException;
import java.util.*;

public class HostModel implements GameModel, Observer {

    private static HostModel hm = null; // Singleton
    private static final int HOST_PORT = 8040;

    private MyServerParallel hostServer; // my Host server - support connection of up to 3 guests
    private GameManager gameManager; // game manager contains all the game data including players
    private GameProperties gameProperties; // for the host player

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

    public void setNumOfPlayers(int numOftotalPlayers) {
        /*
         * Sets The total players of this game.
         * The host need to choose game mode, it can be 2,3 or 4 total players in the
         * game (including the host player).
         */

        this.gameManager.setTotalPlayersCount(numOftotalPlayers);
    }

    public GameProperties getGameProperties() {
        return gameProperties;
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
        gameProperties = new GameProperties(name);

        // PRINT DEBUG
        // System.out.println(gameManager.getHostPlayer());
        System.out.println("HOST: " + name + " is Connected to the game server!");

    }

    @Override
    public void myBookChoice(String bookName) {
        /*
         * Adds this book to the book list of the game
         * each player chooses one book (Maximum 4)
         */
        String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "myBookChoice", bookName);

        // PRINT DEBUG
        // System.out.println("HostModel: myBookChoice ans = " + ans);
    }

    @Override
    public void ready(){
        gameManager.setReady();
    }

    @Override
    public void tryPlaceWord(Word myWord) {
        if (gameProperties.isMyTurn()) {
            try {
                String queryWord = ObjectSerializer.serializeObject(myWord);
                String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "tryPlaceWord",
                        queryWord);
                if (ans.equals("false")) {

                    // PRINT DEBUG
                    System.out.println("tryPlaceWord - some error/turn");

                } else if (ans.equals("notBoardLegal")) {

                    // PRINT DEBUG
                    System.out.println("Your word is not board legal!");

                } else if (ans.equals("notDictionaryLegal")) {

                    // PRINT DEBUG
                    System.out
                            .println(
                                    "Some word that was made is not dictionary legal!\nYou can try Challenge or skipTurn");
                    gameProperties.setMyTurn(false);

                } else if (ans.equals("cantSerialize")) {

                    // PRINT DEBUG
                    System.out.println("tryPlaceWord - cant serialize");

                } else {
                    updateAllStates();
                    gameProperties.setMyTurn(false);

                    // PRINT DEBUG
                    System.out.println("You got more points!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void challenge() {
        if (gameProperties.isMyTurn()) {
            String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "challenge", "true");
            if (ans.equals("false")) {

                // PRINT DEBUG
                System.out.println("challenge - some error/turn");

            } else if (ans.equals("skipTurn")) {

                // PRINT DEBUG
                System.out.println("some word that was made is not dictionary legal - skiping turn!");
                gameProperties.setMyTurn(false);

            } else {
                gameProperties.setMyScore(getMyScore());
                gameProperties.setMyBoard(getCurrentBoard());
                gameProperties.setMyTurn(false);
                gameProperties.setMyTiles(getMyTiles());
                gameProperties.setMyWords(getMyWords());

                // PRINT DEBUG
                System.out.println("You got extra points!");
            }
        }
    }

    @Override
    public void skipTurn() {
        if (gameProperties.isMyTurn()) {

            String ans = gameManager.processPlayerInstruction(gameManager.getHostPlayerId(), "skipTurn", "true");

            if (ans.equals("true")) {
                gameProperties.setMyTurn(false);
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
        this.gameManager.close();
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
            // PRINT DEBUG
            System.out.println("it is not your turn");
            return false;
        } else if (ans.equals("true")) {
            // PRINT DEBUG
            System.out.println("it is your turn!");
            return true;
        } else {
            // PRINT DEBUG
            System.out.println("isMyTurn - some error getting answer from GM");
            return false;
        }
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
        this.gameProperties.setMyBoard(getCurrentBoard());
        this.gameProperties.setMyTiles(getMyTiles());
        this.gameProperties.setMyScore(getMyScore());
        this.gameProperties.setMyWords(getMyWords());
        this.gameProperties.setPlayersScore(getOthersScore());
        this.gameProperties.setMyTurn(isMyTurn());
        System.out.println(gameProperties);

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameManager) {
            updateAllStates();
            this.hostServer.sendToAll("updateAll");
        }
    }

}
