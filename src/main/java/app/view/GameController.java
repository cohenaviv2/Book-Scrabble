package app.view;

import app.model.GetMethod;
import app.model.game.*;
import app.view.GameView.HighlightOutcome;
import app.view_model.GameViewModel;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.concurrent.Task;

/*
 * The GameController is responsible for creating, handling, and presenting windows, stages, and scenes.
 * It collaborates with the GameView class to generate JavaFX components and set them within windows.
 * It is also manages the interaction between the user interface and game logic.
 * 
 * @author: Aviv Cohen
 * 
 */

public class GameController implements Observer {
    private GameViewModel gameViewModel; // Game view model
    private GameView gameView; // Creates the game components
    // Stages
    protected Stage gameLoginStage;
    protected Stage gameFlowStage;
    protected Stage customStage;
    private double xOffset = 0;
    private double yOffset = 0;
    // Game logic
    private List<String> selectedBooks;
    private List<Pane> selectedCells;
    private Stack<Pane> placementCells;
    private List<Pane> placementTileList;
    private List<Word> turnWords;
    private boolean gameRunning;
    private boolean hostQuit;

    public GameController(GameViewModel viewModel) {
        this.gameViewModel = viewModel;
        viewModel.addObserver(this);
        this.gameView = new GameView(viewModel, this);
        customStage = new Stage();
        setUpStage(customStage);
        this.gameRunning = false;
        this.selectedBooks = new ArrayList<>();
        this.selectedCells = new ArrayList<>();
        this.placementCells = new Stack<>();
        this.placementTileList = new ArrayList<>();
        this.turnWords = new ArrayList<>();
    }

    private void setUpStage(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(gameView.getGameIcon());
        stage.setTitle("Book Scrabble");

        if (stage == customStage) {
            customStage.initOwner(getCurrentStage()); // Set the main stage as the owner
            customStage.initModality(Modality.APPLICATION_MODAL); // Set modality to NONE
        }
    }

    private void setUpScene(Scene scene) {
        scene.setCursor(Cursor.HAND);
        scene.getStylesheets().add(gameView.styleSheet);
    }

    public void showInitialWindow() {
        gameLoginStage = new Stage();
        setUpStage(gameLoginStage);

        VBox gameModeBox = gameView.createInitialBox();
        HBox osBar = gameView.createOsBar(gameLoginStage, false);

        VBox root = new VBox(osBar, gameModeBox);
        root.getStyleClass().add("wood-background");

        Scene gameModScene = new Scene(root, 630, 525); // H: 480
        setUpScene(gameModScene);

        gameLoginStage.setScene(gameModScene);
        gameLoginStage.show();
    }

    public void showLoginWindow(boolean isHost) {
        VBox formBox = gameView.createLoginBox();
        HBox osBar = gameView.createOsBar(gameLoginStage, false);

        VBox root = new VBox(osBar, formBox);
        root.getStyleClass().add("wood-background");

        Scene gameSetupScene = new Scene(root, 400, 620);
        setUpScene(gameSetupScene);

        gameLoginStage.setScene(gameSetupScene);
        gameLoginStage.show();
    }

    public void showBookSelectionWindow(boolean fullBookList) {
        customStage = new Stage();
        setUpStage(customStage);

        VBox bookSelectionBox = gameView.createBookSelectionBox(fullBookList);

        Scene bookSelectionScene = new Scene(bookSelectionBox, 900, 700);
        setUpScene(bookSelectionScene);

        customStage.setScene(bookSelectionScene);
        customStage.show();
    }

    public void showGameFlowWindow() {
        customStage.close();
        gameLoginStage.close();
        gameFlowStage = new Stage();
        setUpStage(gameFlowStage);

        HBox osBar = gameView.createOsBar(gameFlowStage, true);
        BorderPane gameFlowBox = gameView.createGameFlowBox();
        gameFlowBox.setTop(osBar);
        gameFlowBox.getStyleClass().add("game-flow-background");

        Scene gameFlowScene = new Scene(gameFlowBox, 1700, 840);
        setUpScene(gameFlowScene);

        gameFlowStage.setScene(gameFlowScene);
        gameFlowStage.show();
    }

    public void showGameInstructionsWindow() {
        customStage.close();
        VBox gameInstructionsBox = gameView.createGameInstuctionsBox();
        showCustomWindow(gameInstructionsBox, 900, 630);
    }

    private void showDrawTilesWindow(String resaults) {
        VBox drawTilesBox = gameView.createDrawTilesBox(resaults);
        showCustomWindow(drawTilesBox, 800, 700);
    }

    public void showTurnAlert() {
        VBox turnAlertBox = gameView.createTurnAlertBox();
        showCustomWindow(turnAlertBox, 530, 290);
    }

    private void showIllegalWordsAlert(List<Word> illegalWords, boolean afterChallenge) {
        VBox illegalWordsBox = gameView.createChallengeBox(illegalWords, afterChallenge);
        showCustomWindow(illegalWordsBox, 600, 300);

        if (afterChallenge) {
            // If afterChallenge is true, schedule the window to close after 2 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                customStage.close();
                gameView.highlightCellsForWords(illegalWords, HighlightOutcome.FAILURE);
            });
            pause.play();
        }
    }

    public void showCustomWindow(Node content, double width, double height) {
        // Calculate the center coordinates of the main stage
        double mainStageX = getCurrentStage().getX();
        double mainStageY = getCurrentStage().getY();
        double mainStageWidth = getCurrentStage().getWidth();
        double mainStageHeight = getCurrentStage().getHeight();
        customStage.setX(mainStageX + (mainStageWidth - width) / 2);
        customStage.setY(mainStageY + (mainStageHeight - height) / 2);

        // Create a new HBox to use as the root
        HBox rootContent = new HBox(content);
        // Set the horizontal grow behavior to ALWAYS
        HBox.setHgrow(content, Priority.ALWAYS);

        Scene customScene = new Scene(rootContent, width, height);
        setUpScene(customScene);

        customStage.setScene(customScene);
        customStage.show();
    }

    public void showMessageWindow(String sender, String message, boolean toAll) {
        VBox messageBox = gameView.createMessageAlertBox(sender, message, toAll);
        showCustomWindow(messageBox, 700, 350);
    }

    public void closeCustomWindow() {
        customStage.close();
    }

    public void showQuitGameWindow() {
        VBox quitGameBox = gameView.createQuitBox();
        showCustomWindow(quitGameBox, 550, 300);
    }

    public Stage getCurrentStage() {
        return gameRunning ? gameFlowStage : gameLoginStage;
    }

    public void resetWordPlacement(boolean isResetCells) {
        // Able Tile Buttons
        for (Button tb : gameView.getTileButtons()) {
            tb.setDisable(false);
        }
        // Reset Placement Tile List Style
        for (Pane cell : placementTileList) {
            cell.getStyleClass().removeIf(style -> style.startsWith("character-"));
            cell.setStyle("");
            // cell.getStyleClass().add("board-cell");
        }
        // Clear All
        gameViewModel.clearWord();
        // Push The Cells Again
        placementCells.clear();
        for (Pane cell : placementTileList) {
            placementCells.add(cell);
        }

        if (isResetCells) {
            placementTileList.clear();

            // Reset Placement Cells Style
            for (Pane cell : placementCells) {
                cell.getStyleClass().removeIf(style -> style.startsWith("character-"));
            }
            placementCells.clear();
            // Reset Selected Cells
            for (Pane cell : selectedCells) {
                cell.getStyleClass().remove("selected");
            }
            selectedCells.clear();
        }
    }

    public void setPlacementCells() {
        Tile[][] board = gameViewModel.currentBoardProperty().get();
        int size = gameViewModel.getWordLength();
        boolean isVer = gameViewModel.isWordVertical();
        int lastRow = gameViewModel.getLastSelectedCellRow();
        int lastCol = gameViewModel.getLastSelectedCellCol();

        if (isVer) {
            for (int i = size; i > 0; i--) {
                if (board[lastRow][lastCol] == null) {
                    Pane cell = (Pane) gameView.getCellFromBoard(lastRow, lastCol);
                    placementCells.push(cell);
                    placementTileList.add(cell);
                }
                lastRow--;
            }
        } else {
            for (int i = size; i > 0; i--) {
                if (board[lastRow][lastCol] == null) {
                    Pane cell = (Pane) gameView.getCellFromBoard(lastRow, lastCol);
                    placementCells.push(cell);
                    placementTileList.add(cell);
                }
                lastCol--;
            }
        }
    }

    public void handleMouseDragged(MouseEvent event) {
        Platform.runLater(() -> {
            Stage stage = getCurrentStage();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void handleMousePressed(MouseEvent event) {
        Platform.runLater(() -> {
            Stage stage = getCurrentStage();
            xOffset = event.getScreenX() - stage.getX();
            yOffset = event.getScreenY() - stage.getY();
        });
    }

    public void checkHostConnection(boolean isGameServer) {
        // Check Game Server Connection
        if (isGameServer) {
            Task<Boolean> networkTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return gameViewModel.isGameServerConnect();
                }
            };
            networkTask.setOnSucceeded(e -> {
                boolean isGameServerConnected = networkTask.getValue();
                if (!isGameServerConnected) {
                    Platform.runLater(() -> {
                        VBox gameServerAlertBox = gameView.createHostNetworkBox(isGameServer);
                        showCustomWindow(gameServerAlertBox, 550, 350);
                    });
                }
            });
            new Thread(networkTask).start();
        }
        // Check Host Server Connection
        else {
            Task<Boolean> newtworkTask = new Task<Boolean>() {

                @Override
                protected Boolean call() throws Exception {
                    Thread.sleep(2000); // 2-second delay
                    return gameViewModel.isConnected();
                }
            };

            newtworkTask.setOnSucceeded(e -> {

                boolean isHostServerConnected = newtworkTask.getValue();
                if (!isHostServerConnected) {
                    Platform.runLater(() -> {
                        VBox hostServerAlertBox = gameView.createHostNetworkBox(isGameServer);
                        showCustomWindow(hostServerAlertBox, 550, 350);
                    });
                }
            });

            new Thread(newtworkTask).start();

        }
    }

    protected void close() {
        this.customStage.close();
        if (gameRunning) {
            gameFlowStage.close();
        } else {
            gameLoginStage.close();
        }
        selectedBooks.clear();
        gameRunning = false;
        hostQuit = false;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel && arg instanceof String) {
            Platform.runLater(() -> {
                String message = (String) arg;
                // Update All
                if (message.startsWith(GetMethod.drawTiles)) {
                    gameRunning = true;
                    showGameFlowWindow();
                    String drawResault = message.split(":")[1];
                    showDrawTilesWindow(drawResault);
                } else if (message.startsWith(GetMethod.waitingRoomError)) {
                    gameViewModel.quitGame();
                    VBox waitingRoomQuitBox = gameView.createNewGameBox("Player Quits Waiting Room",
                            "A player has left the waiting room.\nThe game cannot proceed without all players.");
                    showCustomWindow(waitingRoomQuitBox, 570, 350);
                } else if (message.startsWith(GetMethod.tryPlaceWord)) {
                    String update = message.split(",")[1];
                    if (update.equals("notBoardLegal") || update.equals("illegal")) {
                        VBox illegalAlertBox = gameView.createIllegalMoveBox();
                        showCustomWindow(illegalAlertBox, 650, 420);
                    } else {
                        String res = update.split(":")[0];
                        String turnWordsSerialized = update.split(":")[1];
                        try {
                            turnWords = (List<Word>) ObjectSerializer.deserializeObject(turnWordsSerialized);
                        } catch (ClassNotFoundException | IOException e) {
                        }
                        if (res.equals("false")) {
                            gameView.closeProgressIndicator();
                            showIllegalWordsAlert(turnWords, false);
                        } else {
                            gameView.highlightCellsForWords(turnWords, HighlightOutcome.SUCCESS);
                            turnWords.clear();
                        }
                    }
                } else if (message.startsWith(GetMethod.challenge)) {
                    String update = message.split(",")[1];
                    String res = update.split(":")[0];
                    String turnWordsSerialized = update.split(":")[1];
                    try {
                        turnWords = (List<Word>) ObjectSerializer.deserializeObject(turnWordsSerialized);
                    } catch (ClassNotFoundException | IOException e) {
                    }
                    if (res.equals("false")) {
                        showIllegalWordsAlert(turnWords, true);
                    } else {
                        gameView.highlightCellsForWords(turnWords, HighlightOutcome.CHALLENGE_SUCCESSFUL);
                        gameView.showDoubleScoreEffect();
                        turnWords.clear();
                    }

                } else if (message.startsWith(GetMethod.quitGame)) {
                    if (gameView.isHost() && gameViewModel.othersInfoProperty().size() == 0) {
                        VBox allQuitBox = gameView.createQuitAlertBox("Game Over", "You left alone in the game", true);
                        showCustomWindow(allQuitBox, 550, 300);
                    } else {
                        if (gameRunning) {
                            String player = message.split(":")[1];
                            VBox quitAlertBox = gameView.createClosableAlertBox("", player + " has quit the game!",
                                    true);
                            showCustomWindow(quitAlertBox, 500, 260);
                        }
                    }
                } else if (message.startsWith(GetMethod.endGame)) {
                    String update = message.split(":")[1];
                    hostQuit = update.equals("HOST");
                    VBox endGameBox = gameView.createEndGameBox(hostQuit);
                    if (!gameView.isHost() || !hostQuit) {
                        showCustomWindow(endGameBox, 850, hostQuit ? 800 : 700);
                    }
                    gameViewModel.quitGame();
                } else if (message.equals(GetMethod.exit)) {

                    if (hostQuit) {
                        close();
                        System.exit(0);
                    }
                } else if (message.startsWith(gameViewModel.myNameProperty().get())) {
                    String sender = message.split(":")[2];
                    String msg = message.split(":")[1];
                    showMessageWindow(sender, msg, false);
                } else if (message.startsWith("All")) {
                    String sender = message.split(":")[2];
                    String msg = message.split(":")[1];
                    if (!sender.equals(gameViewModel.myNameProperty().get())) {
                        showMessageWindow(sender, msg, true);
                    }
                }
            });
        }
    }

    public List<String> getSelectedBooks() {
        return selectedBooks;
    }

    public List<Pane> getSelectedCells() {
        return selectedCells;
    }

    public Stack<Pane> getPlacementCells() {
        return placementCells;
    }

    public List<Pane> getPlacementTileList() {
        return placementTileList;
    }

    public List<Word> getTurnWords() {
        return turnWords;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isName(String input) {
        String firstNameRegex = "^[A-Za-z]+$"; // Only letters are allowed

        Pattern pattern = Pattern.compile(firstNameRegex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches() && input.length() > 1;
    }

}
