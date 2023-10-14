package app.view;

import javafx.stage.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

public class GameController {
    GameView gameView;
    boolean gameRunning;
    // Stages
    public Stage gameSetupStage;
    public Stage gameFlowStage;
    public Stage customStage;
    //
    private double xOffset = 0;
    private double yOffset = 0;
    //

    public GameController() {
        this.gameView = new GameView(this);
        customStage = new Stage();
        setUpStage(customStage);
        this.gameRunning = false;
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
        HBox osBar = gameView.createOSBar(gameSetupStage, false);

        VBox root = new VBox(osBar, gameModeBox);
        root.getStyleClass().add("wood-background");

        Scene gameModScene = new Scene(root, 600, 480);
        setUpScene(gameModScene);

        gameSetupStage.setScene(gameModScene);
        gameSetupStage.show();
    }

    public void showLoginForm(boolean isHost) {
        VBox formBox = gameView.createLoginForm();
        HBox osBar = gameView.createOSBar(gameSetupStage, isHost);

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

    public void showCustomWindow(Node content, double width, double height) {
        // Calculate the center coordinates of the main stage
        double mainStageX = getCurrentStage().getX();
        double mainStageY = getCurrentStage().getY();
        double mainStageWidth = getCurrentStage().getWidth();
        double mainStageHeight = getCurrentStage().getHeight();

        // double customStageWidth = width; // Adjust as needed
        // double customStageHeight = height; // Adjust as needed

        double customStageX = mainStageX + (mainStageWidth - width) / 2;
        double customStageY = mainStageY + (mainStageHeight - height) / 2;

        customStage.setX(customStageX);
        customStage.setY(customStageY);

        // Create a new HBox to use as the root
        HBox newContent = new HBox(content);
        // Set the horizontal grow behavior to ALWAYS
        HBox.setHgrow(content, Priority.ALWAYS);

        Scene customScene = new Scene(newContent, width, height);
        setUpScene(customScene);

        customStage.setScene(customScene);
        customStage.show();
    }

    public void closeCustomWindow() {
        customStage.close();
    }

    private Stage getCurrentStage() {
        return gameRunning ? gameFlowStage : gameSetupStage;
    }

    public void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) gameView.getOsBar().getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    public void handleMousePressed(MouseEvent event) {
        Stage stage = (Stage) gameView.getOsBar().getScene().getWindow();

        xOffset = event.getScreenX() - stage.getX();
        yOffset = event.getScreenY() - stage.getY();
    }

    public void close() {
        this.customStage.close();
        if (gameRunning) {
            gameFlowStage.close();
        } else {
            gameSetupStage.close();
        }
    }
}
