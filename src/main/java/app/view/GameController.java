package app.view;

import app.model.GetMethod;
import app.model.game.ObjectSerializer;
import app.model.game.Tile;
import app.model.game.Word;
import app.view_model.ViewModel;
import javafx.stage.*;

import java.io.IOException;
import java.util.*;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

public class GameController implements Observer {
    private ViewModel gameViewModel;
    private GameView gameView;
    // Stages
    protected Stage gameSetupStage;
    protected Stage gameFlowStage;
    protected Stage customStage;
    // Logic
    private List<String> selectedBooks;
    private List<Pane> selectedCells;
    private Stack<Pane> placementCelles;
    private List<Pane> placementList;
    private List<Word> turnWords;
    //
    private boolean gameRunning;
    private double xOffset = 0;
    private double yOffset = 0;
    //

    public GameController(ViewModel viewModel) {
        this.gameViewModel = viewModel;
        viewModel.addObserver(this);
        this.gameView = new GameView(viewModel, this);
        customStage = new Stage();
        setUpStage(customStage);
        this.gameRunning = false;
        this.selectedBooks = new ArrayList<>();
        this.selectedCells = new ArrayList<>();
        this.placementCelles = new Stack<>();
        this.placementList = new ArrayList<>();
        this.turnWords = new ArrayList<>();
    }

    private void setUpStage(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(gameView.getGameIcon());
        stage.setTitle("Book Scrabble");

        if (stage == customStage) {
            stage.initOwner(getCurrentStage()); // Set the owner window
            stage.initModality(Modality.APPLICATION_MODAL); // Set modality to APPLICATION_MODAL
        }
    }

    private void setUpScene(Scene scene) {
        scene.setCursor(Cursor.HAND);
        scene.getStylesheets().add(gameView.styleSheet);
    }

    public void showInitialWindow() {
        gameSetupStage = new Stage();
        setUpStage(gameSetupStage);

        VBox gameModeBox = gameView.createInitialBox();
        HBox osBar = gameView.createOsBar(gameSetupStage, false);

        VBox root = new VBox(osBar, gameModeBox);
        root.getStyleClass().add("wood-background");

        Scene gameModScene = new Scene(root, 600, 480);
        setUpScene(gameModScene);

        gameSetupStage.setScene(gameModScene);
        gameSetupStage.show();
    }

    public void showLoginForm(boolean isHost) {
        VBox formBox = gameView.createLoginBox();
        HBox osBar = gameView.createOsBar(gameSetupStage, isHost);

        VBox root = new VBox(osBar, formBox);
        root.getStyleClass().add("wood-background");

        Scene gameSetupScene = new Scene(root, 400, 600);
        setUpScene(gameSetupScene);

        gameSetupStage.setScene(gameSetupScene);
        gameSetupStage.show();
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
        gameSetupStage.close();
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

    private void showDrawTilesWindow(String info) {
        VBox drawTilesBox = gameView.createDrawTilesBox(info);
        showCustomWindow(drawTilesBox, 700, 400);
    }

    private void showIlegalWordAlert(String iligalWords) {
        VBox iligalWordsBox = gameView.createIlegalWordBox(iligalWords);
        showCustomWindow(iligalWordsBox, 600, 300);
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
        VBox messageBox = gameView.createMessageAlert(sender, message, toAll);
        showCustomWindow(messageBox, 700, 300);
    }

    public void closeCustomWindow() {
        customStage.close();
    }

    public void showQuitGameWindow() {
        VBox quitGameBox = gameView.createQuitBox();
        showCustomWindow(quitGameBox, 550, 300);
    }

    public Stage getCurrentStage() {
        return gameRunning ? gameFlowStage : gameSetupStage;
    }

    public void resetWordPlacement() {
        for (Button tb : gameView.getTileButtons()) {
            tb.setDisable(true);
        }
        for (Pane cell : placementList) {
            boolean hasLetterStyle = cell.getStyleClass().stream()
                    .anyMatch(style -> style.startsWith("character-"));
            // Clear all added style classes except letter styles
            cell.getStyleClass().removeIf(style -> style.startsWith("character-"));

            // Clear inline styles
            cell.setStyle("");

            // Add the default style if it didn't have a letter style
            cell.getStyleClass().add("board-cell");
        }
        for (Pane cell : selectedCells) {
            cell.getStyleClass().remove("selected");
        }
        for (Pane cell : placementCelles) {
            cell.getStyleClass().removeIf(style -> style.startsWith("character-"));
        }
        gameViewModel.clearWord();
        placementCelles.clear();
        placementList.clear();
        selectedCells.clear();
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
                    placementCelles.push(cell);
                    placementList.add(cell);
                }
                lastRow--;
            }
        } else {
            for (int i = size; i > 0; i--) {
                if (board[lastRow][lastCol] == null) {
                    Pane cell = (Pane) gameView.getCellFromBoard(lastRow, lastCol);
                    placementCelles.push(cell);
                    placementList.add(cell);
                }
                lastCol--;
            }
        }
    }

    public void handleMouseDragged(MouseEvent event) {
        Stage stage = getCurrentStage();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    public void handleMousePressed(MouseEvent event) {
        Stage stage = getCurrentStage();
        xOffset = event.getScreenX() - stage.getX();
        yOffset = event.getScreenY() - stage.getY();
    }

    protected void close() {
        this.customStage.close();
        if (gameRunning) {
            gameFlowStage.close();
        } else {
            gameSetupStage.close();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel && arg instanceof String) {
            String message = (String) arg;

            // Update All
            if (message.startsWith(GetMethod.updateAll)) {
                if (!gameRunning) {
                    gameRunning = true;
                    showGameFlowWindow();
                }
                String mod = message.split(",")[1].split(":")[0];
                String value = message.split(",")[1].split(":")[1];
                if (mod.equals("drawTiles")) {
                    // showDrawTilesWindow(value);
                } else if (mod.equals(gameView.myName)) {
                    turnWords.clear();
                    if (value.equals("-1")) {
                        showIlegalWordAlert(null);
                    } else if (value.equals("0")) {
                        // Do nothing... player choose to pass turn
                    } else {
                        try {
                            turnWords = (List<Word>) ObjectSerializer.deserializeObject(value);
                        } catch (ClassNotFoundException | IOException e) {
                        }
                    }
                }

                // Try Place Word
            } else if (message.startsWith(GetMethod.tryPlaceWord)) {
                String params = message.split(",")[1];
                String[] turnWords = params.split(":");
                String iligalWords = "";
                for (int i = 1; i < turnWords.length; i++) {
                    iligalWords += turnWords[i];
                    if (i != turnWords.length - 1)
                        iligalWords += ",";
                }
                showIlegalWordAlert(iligalWords);

                // Got message from
            } else if (message.startsWith(GetMethod.sendTo)) {
                String values = message.split(",")[1];
                String msg;

                if (values.split(":").length == 3) {
                    msg = values.split(":")[1];
                    String sender = values.split(":")[2];
                    showMessageWindow(sender, msg, false);
                } else {
                    msg = values.split(":")[0];
                    String sender = values.split(":")[1];
                    showMessageWindow(sender, msg, true);
                }
            } else {
                System.out.println("View : " + message);
            }
        }
    }

    public List<String> getSelectedBooks() {
        return selectedBooks;
    }

    public List<Pane> getSelectedCells() {
        return selectedCells;
    }

    public Stack<Pane> getPlacementCelles() {
        return placementCelles;
    }

    public List<Pane> getPlacementList() {
        return placementList;
    }

    public List<Word> getTurnWords() {
        return turnWords;
    }

}
