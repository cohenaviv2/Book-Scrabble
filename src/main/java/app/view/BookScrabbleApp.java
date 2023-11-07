package app.view;

import java.util.*;

import app.view_model.GameViewModel;
import javafx.application.Application;
import javafx.stage.Stage;


public class BookScrabbleApp extends Application implements Observer {

    @Override
    public void start(Stage primaryStage) {

        GameViewModel gameViewModel = new GameViewModel();
        GameController gameController = new GameController(gameViewModel);
        gameController.showInitialWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
