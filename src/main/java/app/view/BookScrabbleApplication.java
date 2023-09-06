package app.view;

import java.util.*;

import app.view_model.GameViewModel;
import javafx.application.*;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BookScrabbleApplication extends Application implements Observer {

    private GameViewModel gameViewModel;
    private WindowController windowController;

    @Override
    public void start(Stage primaryStage) {

        Stage prmStage = primaryStage;

        this.gameViewModel = new GameViewModel();
        this.gameViewModel.addObserver(this);
        this.windowController = new WindowController(primaryStage, gameViewModel);

        // Create the first window with Host and Guest buttons
        VBox initialWindowBox = windowController.createInitialWindow();
        Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);
        initialWindowBox.setCursor(Cursor.HAND);
        initialWindowBox.getStylesheets().add(windowController.CSS_STYLESHEET);

        // Set the application icon
        primaryStage.getIcons().add(new Image("icons/game-icon.png"));

        // Set up the primaryStage
        prmStage.setTitle("Book Scrabble");
        prmStage.setScene(initialWindowScene);
        prmStage.initStyle(StageStyle.UNDECORATED);
        prmStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel) {
            windowController.showGameFlowWindow();
        }
    }
}
