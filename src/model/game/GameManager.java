package model.game;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import model.game.Tile.Bag;

/*
 * The Game Manager maintains the game board, the bag of tiles, the books
 * and the player profile of each participant.
 * Contains a list of books chosen for this game where each player chooses one book (max 4)
 * Also contains turn and "Active word" indicators
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class GameManager {

    private static GameManager gm = null; // Singleton

    // Game:
    private Board gameBoard;
    private Bag gameBag;
    private TurnManager turnManager;
    private Map<String, String> fullBookList;
    private StringBuilder gameBooks;

    // Participants:
    private final Player hostPlayer;
    private Map<Integer, Player> playersByID;
    private Map<String, Player> playersByName;

    // Connectivity
    private Socket gameServerSocket;
    private String gameServerIP;
    private int gameServerPORT;

    public GameManager() {
        this.gameBoard = Board.getBoard();
        this.gameBag = Tile.Bag.getBag();
        this.turnManager = new TurnManager();
        this.hostPlayer = null;
        // initial all game Books from directory
        this.fullBookList = new HashMap<>();
        File booksDirectory = new File("resources/books");
        File[] txtFiles = booksDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles != null) {
            for (File file : txtFiles) {
                String fileName = file.getName();
                String filePath = file.getPath();
                fullBookList.put(fileName, filePath);
            }
        }
        this.gameBooks = new StringBuilder();
        this.playersByID = new HashMap<>();
        this.playersByName = new HashMap<>();
    }

    public static GameManager getGM() {
        if (gm == null)
            gm = new GameManager();
        return gm;
    }

    public void setGameServer(String gameServerIP, int gameServerPORT) {
        this.gameServerIP = gameServerIP;
        this.gameServerPORT = gameServerPORT;
    }

    public static int generateID() {
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

    public void addBook(String bookName) {
        /*
         * Add book chosen by a player to String gameBooks
         * gameBooks is used for communicating with the game server
         */
        this.gameBooks.append(bookName + ",");
    }

    public int connectMe(String firstLine) {
        /*
         * Connect handler which use to handle a guset "connectMe" request
         * Sets the guest profile and returns the guest id as response
         */
        String[] params = firstLine.split(",");
        if (params[0].equals("0") && params[1].equals("connectMe")) {
            String guestName = params[2];
            createGuestPlayer(guestName);
            // PRINT DEBUG
            System.out.println("GAME MANAGER: guest " + guestName + " profile was created!");
            return getPlayerByName(guestName).getID();
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: faild to connect guest");
            return 0;
        }
    }

    private boolean isIdExist(int id) {
        return this.playersByID.get(id) != null || this.hostPlayer.getID() == id;
    }

    public String getBookByName(String book) {
        return this.fullBookList.get(book);
    }

    public String getGameBooks() {
        return gameBooks.toString();
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }

    public Player getPlayerByID(int id) {
        return playersByID.get(id);
    }

    public Player getPlayerByName(String name) {
        return playersByName.get(name);
    }

    public String processGuestInstruction(int guestId, String modifier, String value) {
        if (isIdExist(guestId)) {
            // All model cases that indicates some guest instructions which not refers to a
            // game move
            switch (modifier) {
                case "myBookChoice":
                    return addBookHandler(value);
                case "getChanges":
                    return changesHandler(guestId, value);
                case "getCurrentBoard":
                    return boardHandler(value);
                case "getMyScore":
                    return scoreHandler(guestId, value);
                case "getMyTiles":
                    return tilesHandler(guestId, value);
                case "getMyWords":
                    return wordsHandler(guestId, value);
                case "getMyName":
                    return getNameHandler(guestId, value);
                case "getMyID":
                    return getIdHandler(guestId, value);
                case "isMyTurn":
                    return myTurnHandler(guestId, value);
                default:
                    // PRINT DEBUG
                    System.out.println("GAME MANAGER: wrong instructions operator - " + modifier);
                    return null;
            }
        } else {
            // PRINT DEBUG
            System.out.println("GAME MANAGER: id do not exist");
            return null;
        }
    }

    private String addBookHandler(String value) {
        String bookPath;
        if ((bookPath = fullBookList.get(value)) != null) {
            addBook(bookPath);
            return "true";
        }
        return "false";
    }

    private String changesHandler(int guestId, String value) {
        return null;
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
            if (gameBoard.getCurrentWords().size() == 0)
                return "empty";
            String words;
            try {
                words = ObjectSerializer.serializeObject(gameBoard.getCurrentWords());
                return words;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "cantSerialize";
        } else
            return "false";
    }

    private String getNameHandler(int guestId, String value) {
        if (value.equals("true")) {
            return getPlayerByID(guestId).getName();
        } else
            return "false";
    }

    private String getIdHandler(int guestId, String value) {
        if (value.equals("true")) {
            return String.valueOf(getPlayerByID(guestId).getID());
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
        if (isIdExist(id) && quitMod.equals("quitGame") && bool.equals("true")) {
            Player p = this.playersByID.get(id);
            // put back player tiles
            for (Tile t : p.getMyHandTiles()) {
                this.gameBag.put(t);
            }
            this.playersByID.remove(id);
            this.playersByName.remove(p.getName());
        }
    }

    public String processGuestMove(int moveId, String moveModifier, String moveValue) {

        switch (moveModifier) {
            // All model cases that indicates a possible guest game move
            case "tryPlaceWord":
                return queryHandler(moveId, moveValue);
            case "challenge":
                return challengeHandler(moveId, moveValue);
            case "skipTurn":
                return skipTurnHandler(moveId, moveValue);
            default:
                // PRINT DEBUG
                System.out.println("GAME MANAGER: wrong game move operator");
                return null;
        }
    }

    private void updatePlayers() {
    }

    private String queryHandler(int id, String moveValue) {
        /*
         * TODO:
         * if Active word is activated - can not try place this word.
         * need to check if the guest has all the currect tiles and make the string word
         * to a Word object
         * go to board.tryPlaceWord() - if not board legal - returns -1 (need to try
         * again), else set Active
         * word
         * if word was dictionary legal : updated score, updated words, pull tiles,
         * change turn...
         * notifyAll to getChanges (turn index, get currectBoard, players points and
         * such...)
         * if the word wasnt dictionary legal : need to set Active word and inform the
         * player that some word
         * that was created is not dictionary legal and the player can try to challenge
         * or skip turn.
         */
        if (this.turnManager.getActiveWord() != null) {
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
                this.turnManager.setActiveWord(queryWord);
                player.setIsActiveWord(true);
                return "notDictionaryLegal";
            } else {
                player.addPoints(score);
                player.addWords(gameBoard.getCurrentWords());
                this.turnManager.pullTiles(id);
                // send some updated to the players so they could getChanges():
                // getCurrentBoard, getMyTiles, getMyWords
                updatePlayers();
                this.turnManager.nextTurn();
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
         * TODO:
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
            if (this.turnManager.getActiveWord() == null || !getPlayerByID(playerId).isActiveWord()) {
                return "false";
            }
            ArrayList<Word> turnWords = gameBoard.getCurrentWords();
            Word activeWord = turnManager.getActiveWord();
            String answer = challengeFromServer(turnWords);
            Player player = getPlayerByID(playerId);

            if (answer.equals("true")) {
                int score = gameBoard.tryPlaceWord(activeWord);
                if (score == 0) {
                    // some word that was made is not dictionary legal - skip turn
                    this.turnManager.setActiveWord(null);
                    player.setIsActiveWord(false);
                    this.turnManager.nextTurn();
                    return "skipTurn";
                } else {
                    // double points
                    score *= 2;
                    player.addPoints(score);
                    player.addWords(gameBoard.getCurrentWords());
                    this.turnManager.pullTiles(playerId);
                    this.turnManager.setActiveWord(null);
                    player.setIsActiveWord(false);
                    // send some updated to the players so they could getChanges():
                    // getCurrentBoard, getMyTiles, getMyWords
                    updatePlayers();
                    this.turnManager.nextTurn();
                    String playerScore = String.valueOf(score);

                    return playerScore;
                }

            } else if (answer.equals("false")) {
                // players loses points and skiping turn
                int negScore = -10;
                player.addPoints(negScore);
                this.turnManager.setActiveWord(null);
                player.setIsActiveWord(false);
                // send some updated to the players so they could getChanges():
                // getCurrentBoard, getMyTiles, getMyWords
                updatePlayers();
                this.turnManager.nextTurn();
                String playerScore = String.valueOf(negScore);

                return playerScore;
            } else {
                return "false";
            }
        } else {
            return "false";
        }
    }

    public boolean queryFromServer(Word word) {
        /* TODO: need to ask the game server */

        String queryWord = gameBoard.wordToString(word);

        try {
            this.gameServerSocket = new Socket(gameServerIP, gameServerPORT);
            PrintWriter out = new PrintWriter(gameServerSocket.getOutputStream(), false);
            BufferedReader in = new BufferedReader(new InputStreamReader(gameServerSocket.getInputStream()));

            String req = "Q," + gameBooks.toString() + queryWord;
            out.println(req);
            String res = in.readLine();
            if (res.equals("true")) {
                return true;
            } else if (res.equals("false")) {
                return false;
            } else {
                // PRINT DEBUG
                System.out.println("GAME MANAGER: wrong query answer form game server\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String challengeFromServer(ArrayList<Word> turnWords) {
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

        try {
            this.gameServerSocket = new Socket(gameServerIP, gameServerPORT);
            PrintWriter out = new PrintWriter(gameServerSocket.getOutputStream(), false);
            BufferedReader in = new BufferedReader(new InputStreamReader(gameServerSocket.getInputStream()));

            for (String c : words) {
                String req = "C," + gameBooks.toString() + c;
                out.println(req);
                String ans = in.readLine();
                if (ans.equals("false")) {
                    return "false";
                }
                if (!ans.equals("true") && !ans.equals("false")) {
                    // PRINT DEBUG
                    System.out.println("GAME MANAGER: wrong challenge answer form game server\n");
                }
            }
            return "true";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "gameServerError";
    }

    private String skipTurnHandler(int id, String moveValue) {
        if (moveValue.equals("true")) {
            if (this.turnManager.activeWord != null) {
                this.turnManager.activeWord = null;
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
        if ((newSize = g.gameBag.size()) != bagSize) {
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

    public void close() {
        try {
            this.gameServerSocket.close();
            this.turnManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TurnManager {

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

        private void pullTiles(int playerId) {
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

            // Set turn indexes, first player turn, and put tiles back to the bag
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (i == 0) {
                    p.setMyTurn(true);
                }
                p.setTurnIndex(i);
                gameBag.put(p.getMyHandTiles().get(0));
                p.getMyHandTiles().remove(0);
            }
            setCurrentTurnIndex(0);
        }

        private void nextTurn() {
            /* Set turn to the next player (turn index) */

            int i = this.currentTurnIndex;
            int j = (i + 1) % playersByID.size();

            Player oldPlayer = playersByID.values().stream().filter(p -> p.getTurnIndex() == i).findFirst().get();
            Player nextPlayer = playersByID.values().stream().filter(p -> p.getTurnIndex() == j).findFirst().get();

            oldPlayer.setMyTurn(false);
            nextPlayer.setMyTurn(true);
            setCurrentTurnIndex(j);
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
