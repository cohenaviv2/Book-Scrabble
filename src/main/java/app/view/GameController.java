package app.view;

import app.model.GetMethod;
import app.model.game.Tile;
import app.view_model.ViewModel;
import javafx.stage.*;
import java.util.*;

import javafx.application.Platform;
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
    protected List<Pane> selectedCells;
    protected Stack<Pane> placementCelles;
    protected List<Pane> placementList;
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
        this.selectedCells = new ArrayList<>();
        this.placementCelles = new Stack<>();
        this.placementList = new ArrayList<>();
    }

    private void setUpStage(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(gameView.gameIcon);
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

        VBox bookSelectionBox = gameView.createBookBox(fullBookList);

        Scene bookSelectionScene = new Scene(bookSelectionBox, 900, 700);
        setUpScene(bookSelectionScene);

        customStage.setScene(bookSelectionScene);
        customStage.show();
    }

    public void showGameFlowWindow(){
        gameSetupStage.close();
        customStage.close();
        gameFlowStage = new Stage();
        setUpStage(gameFlowStage);
        BorderPane gameFlowBox = gameView.createGameFlowBox();
        HBox osBar = gameView.createOsBar(gameFlowStage, true);

        VBox root = new VBox(osBar, gameFlowBox);
        root.getStyleClass().add("game-flow-background");
        VBox.setVgrow(gameFlowBox, Priority.ALWAYS);
        HBox.setHgrow(gameFlowBox, Priority.ALWAYS);

        Scene gameFlowScene = new Scene(root, 1700, 840);
        setUpScene(gameFlowScene);

        gameFlowStage.setScene(gameFlowScene);
        gameFlowStage.show();
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
            // Clear all added style classes
            cell.getStyleClass().clear();
            // Clear inline styles
            cell.setStyle("");
            // Add the default style
            cell.getStyleClass().add("board-cell");
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


    protected void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) gameView.getOsBar().getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    protected void handleMousePressed(MouseEvent event) {
        Stage stage = (Stage) gameView.getOsBar().getScene().getWindow();

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
            if (message.startsWith(GetMethod.updateAll)) {
                if (!gameRunning) {
                    gameRunning = true;
                    showGameFlowWindow();
                }
            }
            else {
                System.out.println("View : "+message);
            }
        }
    }
}
