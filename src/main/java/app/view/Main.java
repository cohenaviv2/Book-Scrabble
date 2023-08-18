package app.view;

import java.util.*;

import app.view_model.GameViewModel;
import javafx.application.*;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application implements Observer {

    private GameViewModel gameViewModel;
    private WindowController windowController;
    private static Main instance; // To hold the Main instance

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

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
        initialWindowBox.getStylesheets().add(windowController.getStyleSheet());

        // Set up the primaryStage
        prmStage.setTitle("Book Scrabble");
        prmStage.setScene(initialWindowScene);
        prmStage.initStyle(StageStyle.UNDECORATED);
        prmStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showAlert(String alert) {
        this.windowController.showAlert(alert);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel) {
            // PRINT DEBUG
            System.out.println("VIEW GOT UPDATE");
            windowController.showGameFlowWindow();
        }
    }
}
