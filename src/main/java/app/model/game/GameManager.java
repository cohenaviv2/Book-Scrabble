package app.model.game;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import app.model.MethodInvoker;
import app.model.game.Tile.Bag;
import app.model.server.RunGameServer;

/*
 * The Game Manager maintains the game board, the bag of tiles, the books
 * and the player profile of each participant.
 * Contains a list of books chosen for this game where each player chooses unlimited number of books.
 * Also contains Turn manager.
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class GameManager extends Observable {

    private static GameManager gm = null; // Singleton

    // Game
    private Board gameBoard;
    private Bag gameBag;
    private final TurnManager turnManager;
    private Map<String, String> fullBookList;
    private Set<String> selectedBooks;
    private String gameBooksString;

    // Participants
    private Player hostPlayer;
    private Map<Integer, Player> playersByID;
    private Map<String, Player> playersByName;
    private int totalPlayersNum;
    private volatile AtomicInteger readyToPlay;

    // Connectivity
    private Socket gameServerSocket;
    private final String gameServerIP;
    private final int gameServerPORT;

    public GameManager() {

        // GAME SERVER'S IP AND PORT FROM ENV FILE
        this.gameServerIP = RunGameServer.loadProperties().getProperty("GAME_SERVER_IP");
        this.gameServerPORT = Integer.parseInt(RunGameServer.loadProperties().getProperty("GAME_SERVER_PORT"));

        this.gameBoard = Board.getBoard();
        this.gameBag = Tile.Bag.getBag();
        this.turnManager = new TurnManager();

        // initial all game Books from directory
        this.fullBookList = new HashMap<>(); // book name to path
        File booksDirectory = new File("src\\main\\resources\\books");
        File[] txtFiles = booksDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles != null) {
            for (File file : txtFiles) {
                String fileName = file.getName().replaceAll(".txt", "");
                String gameServerPath = "server/books/" + fileName + ".txt"; // GAME SERVER PATH
                fullBookList.put(fileName, gameServerPath);
            }
        }
        this.gameBooksString = "";
        this.selectedBooks = new HashSet<>();
        this.playersByID = new HashMap<>();
        this.playersByName = new HashMap<>();
        this.readyToPlay = new AtomicInteger(0);
    }

    public static GameManager get() {
        if (gm == null)
            gm = new GameManager();
        return gm;
    }

    public void setTotalPlayersCount(int totalPlayers) {
        if (totalPlayers >= 2 && totalPlayers <= 4) {
            this.totalPlayersNum = totalPlayers;
        } else {
            System.out.println("Can manage game only with 2-4 players");
        }
    }

    public boolean isReadyToPlay() {
        return readyToPlay.get() == totalPlayersNum;
    }

    private static int generateID() {
        /*
         * Generates a unique ID for each player
         * only Game Manager can create player profiles and ID's
         */
        UUID idOne = UUID.randomUUID();
        String str = "" + idOne;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str) / 1000;
    }

    public void createHostPlayer(String hostName) {
        /*
         * Creates host Player profile
         * also puts this player in the players hash-maps
         */
        int hostID = generateID();
        Player host = new Player(hostName, hostID, true);
        this.hostPlayer = host;
        this.playersByID.put(hostID, host);
        this.playersByName.put(hostName, host);
    }

    private void createGuestPlayer(String guestName) {
        /* Creates guest Player profile */

        int guestID = generateID();
        Player guest = new Player(guestName, guestID, false);
        this.playersByID.put(guestID, guest);
        this.playersByName.put(guestName, guest);
    }

    private boolean isIdExist(int id) {
        return this.playersByID.get(id) != null || this.hostPlayer.getID() == id;
    }

    public String getGameBooks() {
        return gameBooksString;
    }

    public Map<String, String> getFullBookList() {
        return fullBookList;
    }

    public int getHostPlayerId() {
        return hostPlayer.getID();
    }

    public Player getPlayerByID(int id) {
        return playersByID.get(id);
    }

    public Player getPlayerByName(String name) {
        return playersByName.get(name);
    }

    private int getBagCount() {
        return this.gameBag.size();
    }

    private String sentToGameServer(String query) throws UnknownHostException, IOException {
        this.gameServerSocket = new Socket(gameServerIP, gameServerPORT);
        PrintWriter out = new PrintWriter(gameServerSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(gameServerSocket.getInputStream()));
        out.println(query);
        String ans = in.readLine();
        gameServerSocket.close();
        out.close();
        in.close();

        return ans;
    }

    public boolean isGameServerConnect() {
        try {
            String ans = sentToGameServer("ack");
            if (ans.equals("connected"))
                return true;
        } catch (IOException e) {
        }
        return false;
    }

    public String processPlayerInstruction(int guestId, String modifier, String value) {
        if (isIdExist(guestId)) {
            // All model cases that indicates some guest instructions
            switch (modifier) {
                case MethodInvoker.myBooksChoice:
                    return addBooksHandler(value);
                case MethodInvoker.ready:
                    return readyHandler(value);
                case MethodInvoker.getOthersInfo:
                    return getPlayersInfoHandler(guestId, value);
                case MethodInvoker.getCurrentBoard:
                    return boardHandler(value);
                case MethodInvoker.getMyScore:
                    return scoreHandler(guestId, value);
                case MethodInvoker.getMyTiles:
                    return tilesHandler(guestId, value);
                case MethodInvoker.getMyWords:
                    return wordsHandler(guestId, value);
                case MethodInvoker.isMyTurn:
                    return myTurnHandler(guestId, value);
                case MethodInvoker.tryPlaceWord:
                    return queryHandler(guestId, value);
                case MethodInvoker.challenge:
                    return challengeHandler(guestId, value);
                case MethodInvoker.skipTurn:
                    return skipTurnHandler(guestId, value);
                default:
                    // PRINT DEBUG
                    System.out.println("GAME MANAGER: wrong instructions operator - " + modifier);
                    return null;
            }
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: ID do not exist");
            return null;
        }
    }

    public int connectGuestHandler(String guestName) {
        /*
         * Connect handler which use to handle a guset "connectMe" request
         * Sets the guest profile and returns the guest id as response
         */
        createGuestPlayer(guestName);
        // PRINT DEBUG
        System.out.println("GAME MANAGER: guest " + guestName + " profile was created!");
        return getPlayerByName(guestName).getID();
    }

    public String addBooksHandler(String listOfBooksSer) {
        try {
            List<String> playerBookList = (List<String>) ObjectSerializer.deserializeObject(listOfBooksSer);

            for (String book : playerBookList) {
                if (fullBookList.containsKey(book)) {
                    this.selectedBooks.add(book);
                }
            }
            // PRINT DEBUG
            System.out.println("Game Manager: your book " + listOfBooksSer + " is set up!\n");

            return "true";

        } catch (ClassNotFoundException | IOException e) {
        }

        // PRINT DEBUG
        System.out.println("Game Manager: failed to set up this book");
        return "false";
    }

    private String readyHandler(String value) {
        setReady();
        return "ready";
    }

    public void setReady() {

        if (readyToPlay.incrementAndGet() == totalPlayersNum) {
            // PRINT DEBUG
            System.out.println("Game Manager: ALL PLAYERS ARE READY TO PLAY! draw tiles...\n");

            // Set the Server's game books List
            for (String book : selectedBooks) {
                String serverBookPath = fullBookList.get(book);
                this.gameBooksString += serverBookPath + ",";
            }

            // Draw tiles - Who's playing first
            turnManager.drawTiles();
        }
    }

    private String getPlayersInfoHandler(int guestId, String value) {
        if (value.equals("true")) {

            List<Player> otherPlayerList = this.playersByID.values().stream()
                    .filter(player -> player.getID() != guestId)
                    .collect(Collectors.toList());

            Map<String, String> othersInfo = new HashMap<>();

            for (Player p : otherPlayerList) {
                String info = String.valueOf(p.getScore()) + ":" + String.valueOf(p.isMyTurn());
                othersInfo.put(p.getName(), info);
            }
            try {
                String sc = ObjectSerializer.serializeObject(othersInfo);
                return sc;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "cantSerialize";
        } else {
            return "false";
        }

    }

    private String boardHandler(String value) {
        if (value.equals("true")) {
            try {
                String board = ObjectSerializer.serializeObject(gameBoard.getTiles());
                return board;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "cantSerialize";
        } else
            return "false";
    }

    private String scoreHandler(int guestId, String value) {
        if (value.equals("true")) {
            return String.valueOf(getPlayerByID(guestId).getScore());
        } else
            return "false";
    }

    private String tilesHandler(int guestId, String value) {
        if (value.equals("true")) {
            try {
                String tilesString = ObjectSerializer.serializeObject(getPlayerByID(guestId).getMyHandTiles());
                return tilesString;
            } catch (IOException e) {
                e.printStackTrace();
            }
            // PRINT DEBUG
            System.out.println("can not serialize hand tile of " + getPlayerByID(guestId).getName());
            return "cantSerialize";
        } else
            return "false";
    }

    private String wordsHandler(int guestId, String value) {
        if (value.equals("true")) {
            String words;
            try {
                words = ObjectSerializer.serializeObject(getPlayerByID(guestId).getMyWords());
                return words;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "cantSerialize";
        } else
            return "false";
    }

    private String myTurnHandler(int guestId, String value) {
        if (value.equals("true") && getPlayerByID(guestId).isMyTurn())
            return "true";
        else
            return "false";
    }

    public void quitGameHandler(String quitGameID) {
        String[] params = quitGameID.split(",");
        int id = Integer.parseInt(params[0]);
        String quitMod = params[1];
        String bool = params[2];
        if (isIdExist(id) && quitMod.equals(MethodInvoker.quitGame) && bool.equals("true")) {
            Player p = this.playersByID.get(id);
            String name = p.getName();
            // put back player tiles
            for (Tile t : p.getMyHandTiles()) {
                this.gameBag.put(t);
            }
            this.playersByID.remove(id);
            this.playersByName.remove(p.getName());
            // PRINT DEBUG
            System.out.println("GameManager: " + name + " has quit the game!");
        }
    }

    private String queryHandler(int id, String moveValue) {

        System.out.println("\n\n\n\n\n\n\n\n query Handler\n\n\n\n\n\n\n\n");

        /*
         * if Active word is activated - can not try place this word.
         * need to check if the guest has all the correct tiles and make the string word
         * to a Word object
         * go to board.tryPlaceWord() - if not board legal - returns -1 (need to try
         * again), else set Active
         * word
         * if word was dictionary legal : updated score, updated words, pull tiles,
         * change turn...
         * notifyAll to getChanges (turn index, get correct Board, players points and
         * such...)
         * if the word wasn't dictionary legal : need to set Active word and inform the
         * player that some word
         * that was created is not dictionary legal and the player can try to challenge
         * or skip turn.
         */
        if (turnManager.getActiveWord() != null) {
            return "false";
        }
        try {
            Word queryWord = (Word) ObjectSerializer.deserializeObject(moveValue);
            Player player = getPlayerByID(id);
            int score = gameBoard.tryPlaceWord(queryWord);
            if (score == -1) {
                return "notBoardLegal";
            } else if (score == 0) {
                // some word that was made is not dictionary legal
                // players can challenge this word or skip turn
                turnManager.setActiveWord(queryWord);
                player.setIsActiveWord(true);
                return "notDictionaryLegal";
            } else {
                player.addPoints(score);
                player.addWords(gameBoard.getTurnWords());
                for (Tile t : queryWord.getTiles()) {
                    player.getMyHandTiles().remove(t);
                }
                turnManager.pullTiles(id);
                // send some updated to the players, so they could getChanges():
                // getCurrentBoard, getMyTiles, getMyWords

                // setChanged();
                // notifyObservers();

                turnManager.nextTurn();

                String playerScore = String.valueOf(score);
                return playerScore;
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return "cantSerialize";
    }

    private String challengeHandler(int playerId, String moveValue) {
        /*
         * if Active word is NOT activated - can not try challange this word.
         * need to ask the game sever to challenge this word -
         * if server return true - need to go board.tryPlaceWord() - if all words is
         * dictionary legal need to updated all stated and add some extra points, if not
         * dic legal - skip turn
         * if server return false - need to drop players points and update all states
         * Any way need to turn off Active word.
         * 
         */
        if (moveValue.equals("true")) {
            if (turnManager.getActiveWord() == null || !getPlayerByID(playerId).isActiveWord()) {
                return "false";
            }
            ArrayList<Word> turnWords = gameBoard.getTurnWords();
            Word activeWord = turnManager.getActiveWord();
            String answer = challengeToGameServer(turnWords);
            Player player = getPlayerByID(playerId);

            if (answer.equals("true")) {
                // Challenge successfull - player get extra points
                int score = gameBoard.tryPlaceWord(activeWord);

                // Turn off active word
                turnManager.setActiveWord(null);
                player.setIsActiveWord(false);
                // double points
                score *= 2;
                player.addPoints(score);
                player.addWords(turnWords);
                for (Tile t : activeWord.getTiles()) {
                    player.getMyHandTiles().remove(t);
                }
                turnManager.pullTiles(playerId);
                // send some updated to the players so they could getChanges():
                // getCurrentBoard, getMyTiles, getMyWords

                // setChanged();
                // notifyObservers();

                this.turnManager.nextTurn();

                String playerScore = String.valueOf(score);
                return playerScore;

            } else if (answer.equals("false")) {
                // Challenge failed - player loses points
                // Turn off active word
                turnManager.setActiveWord(null);
                player.setIsActiveWord(false);
                // Negative score set
                int negScore = -10;
                player.addPoints(negScore);
                // send some updated to the players so they could getChanges():
                // getCurrentBoard, getMyTiles, getMyWord
                // ???
                setChanged();
                notifyObservers("updateAll");

                this.turnManager.nextTurn();
                return MethodInvoker.skipTurn;
            } else {
                return MethodInvoker.skipTurn;
            }
        } else {
            return "false";
        }
    }

    protected boolean dictionaryLegal(Word word) {
        boolean flag = (word == null);
        System.out.println("\n\n\n\n\n\n\n\n dictionary Legal \n\n word is null : " + flag + "\n\n\n\n\n\n\n\n\n\n");

        /* ask the game server */
        //
        System.out.println("Dic Word - " + word);
        //
        String queryWord = gameBoard.wordToString(word);

        String ans = null;

        String req = "Q," + getGameBooks() + queryWord;

        try {
            ans = sentToGameServer(req);
        } catch (IOException e) {}
        System.out.println("\n\n\n SERVER RESPONDE - " + ans);
        if (ans.equals("true")) {
            return true;
        } else if (ans.equals("false")) {
            return false;
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: wrong query answer from game server\n");
            return false;
        }
    }

    private String challengeToGameServer(ArrayList<Word> turnWords) {
        /*
         * Asks the game server to challenge all the words that was made in this turn
         * game server will perform an I/O search in the game books
         * returns "true" of "false" whether the word was found or not
         * any way - updates the cache for the next query
         */

        ArrayList<String> words = new ArrayList<>();
        for (Word w : turnWords) {
            words.add(gameBoard.wordToString(w));
        }

        String ans = null;

        for (String cw : words) {

            String req = "C," + getGameBooks() + cw;

            try {
                ans = sentToGameServer(req);
            } catch (IOException e) {
            }

            if (ans.equals("false")) {
                return "false";
            }
            if (!ans.equals("true") && !ans.equals("false")) {
                // PRINT DEBUG
                System.out.println("GAME MANAGER: wrong challenge answer form game server\n");
                return "false";
            }
        }

        return "true";
    }

    private String skipTurnHandler(int playerId, String moveValue) {
        if (moveValue.equals("true")) {
            if (this.turnManager.activeWord != null) {
                this.turnManager.activeWord = null;
                getPlayerByID(playerId).setIsActiveWord(false);
            }
            this.turnManager.nextTurn();
            return "true";
        } else
            return "false";
    }

    public static void main(String[] args) {
        GameManager g = new GameManager();
        g.createHostPlayer("Aviv");
        g.createGuestPlayer("Moshe");
        g.createGuestPlayer("Matan");
        g.createGuestPlayer("Amit");

        // Test draw tiles Lottery
        int bagSize = g.gameBag.size();
        g.turnManager.drawTiles();
        int newSize;
        if ((newSize = g.gameBag.size()) != bagSize - 7 * 4) {
            System.out.println("bag size doesnt match: " + newSize);
        }
        // Test next turn
        g.turnManager.nextTurn();
        if (g.turnManager.getCurrentTurnIndex() != 1) {
            System.out.println("wrong turn index");
        }
        g.turnManager.nextTurn();
        if (g.turnManager.getCurrentTurnIndex() != 2) {
            System.out.println("wrong turn index");
        }
        g.turnManager.nextTurn();
        if (g.turnManager.getCurrentTurnIndex() != 3) {
            System.out.println("wrong turn index");
        }
        g.turnManager.nextTurn();
        if (g.turnManager.getCurrentTurnIndex() != 0) {
            System.out.println("wrong turn index");
        }
        // Test serialize hand tiles
        List<Tile> randTiles = new ArrayList<>();
        System.out.println("before serialize:");
        for (int i = 0; i < 10; i++) {
            Tile tt = g.gameBag.getRand();
            randTiles.add(tt);
            System.out.print(tt.getLetter() + ",");
        }
        System.out.println("\n");
        try {
            String tileString = ObjectSerializer.serializeObject(randTiles);
            List<Tile> tcopy = (List<Tile>) ObjectSerializer.deserializeObject(tileString);
            System.out.println("after serialize:");
            for (Tile t : tcopy)
                System.out.print(t.getLetter() + ",");
            System.out.println("\n");
            System.out.println(randTiles.get(0).equals(tcopy.get(0)) + "\n");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test serialize tiles board
        Tile[][] board = new Tile[15][15];
        board[5][7] = g.gameBag.getTile('A');
        board[6][7] = g.gameBag.getTile('V');
        board[7][7] = g.gameBag.getTile('I');
        board[8][7] = g.gameBag.getTile('V');

        System.out.println("before serialize:");
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j] == null)
                    System.out.print("- ");
                else
                    System.out.print(board[i][j].getLetter() + " ");
            }
            System.out.println();
        }
        System.out.println("\n");

        String boardString;
        try {
            boardString = ObjectSerializer.serializeObject(board);
            Tile[][] copyBoard = (Tile[][]) ObjectSerializer.deserializeObject(boardString);
            System.out.println("after serialize:");
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    if (copyBoard[i][j] == null)
                        System.out.print("- ");
                    else
                        System.out.print(copyBoard[i][j].getLetter() + " ");
                }
                System.out.println();
            }
            System.out.println("\n");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("done");
    }

    public class TurnManager {

        /*
         * The turn manager is responsible to set players turn indexes
         * drawTiles method conducts a lottery which sets the order of the players,
         * according to the rules of the game (more details in the method)
         * 
         * @author: Aviv Cohen
         * 
         */

        private int currentTurnIndex;
        private Word activeWord;

        private TurnManager() {
            this.activeWord = null;
            this.currentTurnIndex = -1;
        }

        public void pullTiles(int playerId) {
            /* Completes players hand to 7 Tiles */
            Player p = getPlayerByID(playerId);

            while (p.getMyHandTiles().size() < 7) {
                Tile t = gameBag.getRand();
                while (t == null) {
                    t = gameBag.getRand();
                }
                p.getMyHandTiles().add(t);
            }
        }

        private void drawTiles() {
            /*
             * Each player randomly draws a tile from the bag
             * The order of the player's turn index is determined by the order of the
             * letters drawn -
             * from the smallest to the largest
             * If an empty tile is drawn, will return it to the bag and draw another one.
             * All tiles return to the bag.
             * Sets all players turn index's and the first player isTurn to true.
             */

            // Pull tile for each player
            for (Player p : playersByID.values()) {
                Tile t = gameBag.getRand();
                while (t == null) {
                    t = gameBag.getRand();
                }
                p.getMyHandTiles().add(0, t);
            }

            // Sort tiles
            List<Player> players = playersByID.values().stream().sorted((p1, p2) -> {
                Tile t1 = p1.getMyHandTiles().get(0);
                Tile t2 = p2.getMyHandTiles().get(0);
                return t1.getLetter() - t2.getLetter();
            }).collect(Collectors.toList());

            // int firstPlayerId = 0;
            System.out.println("Turn Manager: players turn indexes :\n##########################");
            // Set turn indexes, first player turn, and put tiles back to the bag
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);

                if (i == 0) {
                    p.setMyTurn(true);
                    // firstPlayerId = p.getID();
                }
                Tile myTile = p.getMyHandTiles().get(0);
                p.setTurnIndex(i);
                System.out.println(p.getName() + " - " + i + " (" + myTile.getLetter() + ")");
                gameBag.put(myTile);
                p.getMyHandTiles().remove(0);
                turnManager.pullTiles(p.getID());
            }
            System.out.println();

            setCurrentTurnIndex(0);
            setChanged();
            notifyObservers();
        }

        public void nextTurn() {
            /* Set turn to the next player (turn index) */

            int i = this.currentTurnIndex;
            int j = (i + 1) % playersByID.size();

            Player oldPlayer = playersByID.values().stream().filter(p -> p.getTurnIndex() == i).findFirst().get();
            Player nextPlayer = playersByID.values().stream().filter(p -> p.getTurnIndex() == j).findFirst().get();

            oldPlayer.setMyTurn(false);
            nextPlayer.setMyTurn(true);
            setCurrentTurnIndex(j);
            setChanged();
            notifyObservers();
            // notifyObservers(nextPlayer.getID() + "yourTurn");
            printTurnInfo();
        }

        public void printTurnInfo() {
            System.out.println("GameManager: Turn information:\n***************************");
            for (Player p : playersByID.values()) {
                System.out.println(p.getName() + " - " + p.isMyTurn() + " - " + p.getTurnIndex());
            }
        }

        private void setActiveWord(Word activeWord) {
            this.activeWord = activeWord;
        }

        private void setCurrentTurnIndex(int turnIndex) {
            this.currentTurnIndex = turnIndex;
        }

        public Word getActiveWord() {
            return activeWord;
        }

        public int getCurrentTurnIndex() {
            return currentTurnIndex;
        }

        public void close() {
        }
    }

}
