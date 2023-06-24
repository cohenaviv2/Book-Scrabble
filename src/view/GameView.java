package view;

import view_model.GameViewModel;


import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.game.Tile;

public class GameView extends Application implements Observer {
    private Stage primaryStage;
    private GameViewModel gameViewModel;
    private String myName;
    private String MODE;
    private int numOfPlayers;
    private boolean gameStarted = false;
    private boolean loginFormLocked = false;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        this.gameViewModel = new GameViewModel();
        gameViewModel.addObserver(this);

        // Create the first window with Host and Guest buttons
        VBox initialWindowBox = createInitialWindow();
        Scene initialWindowScene = new Scene(initialWindowBox, 300, 200);

        // Set up the primaryStage
        primaryStage.setTitle("Welcome to Book Scrabble Game!");
        primaryStage.setScene(initialWindowScene);
        primaryStage.show();
    }

    private VBox createInitialWindow() {
        VBox initialWindowBox = new VBox(10);
        initialWindowBox.setPadding(new Insets(20));
        initialWindowBox.setAlignment(Pos.CENTER);

        Label modeLabel = new Label("Choose Game Mode:");
        RadioButton hostRadioButton = new RadioButton("Host");
        RadioButton guestRadioButton = new RadioButton("Guest");
        Button submitButton = new Button("Next");

        // Create a ToggleGroup for the radio buttons
        ToggleGroup toggleGroup = new ToggleGroup();
        hostRadioButton.setToggleGroup(toggleGroup);
        guestRadioButton.setToggleGroup(toggleGroup);

        // Event handler for submitButton
        submitButton.setOnAction(event -> {
            if (hostRadioButton.isSelected()) {
                MODE = "H";
                showHostWindow();
            } else if (guestRadioButton.isSelected()) {
                MODE = "G";
                showLoginForm();
            }
        });

        initialWindowBox.getChildren().addAll(modeLabel, hostRadioButton, guestRadioButton, submitButton);
        return initialWindowBox;
    }

    private void showHostWindow() {
        VBox hostWindowBox = new VBox(10);
        hostWindowBox.setPadding(new Insets(20));
        hostWindowBox.setAlignment(Pos.CENTER);

        Label numOfPlayersLabel = new Label("Select Number of Players:");
        ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
        numOfPlayersComboBox.getItems().addAll(2, 3, 4);

        Button submitButton = new Button("Next");

        submitButton.setOnAction(event -> {
            numOfPlayers = numOfPlayersComboBox.getValue();
            showLoginForm();
        });

        hostWindowBox.getChildren().addAll(numOfPlayersLabel, numOfPlayersComboBox, submitButton);
        Scene hostWindowScene = new Scene(hostWindowBox, 300, 200);
        primaryStage.setScene(hostWindowScene);
    }

    private void showLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);

        TextField nameTextField = new TextField();
        TextField ipTextField = new TextField();
        TextField portTextField = new TextField();
        ListView<String> bookListView = new ListView<>(GameViewModel.getBookList());
        Button connectButton = new Button("Connect me");

        connectButton.setOnAction(event -> {
            myName = nameTextField.getText();
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setGameMode(MODE);
                gameViewModel.setNumOfPlayer(numOfPlayers);
                gameViewModel.connectMe(myName, ip, port);
                gameViewModel.myBookChoice(selectedBook);
                gameViewModel.ready();

                // Disable form elements
                nameTextField.setDisable(true);
                ipTextField.setDisable(true);
                portTextField.setDisable(true);
                bookListView.setDisable(true);
                connectButton.setDisable(true);

                // Set the login form as locked
                loginFormLocked = true;
                // showGameFlowWindow();
            }
        });

        loginFormBox.getChildren().addAll(
                new Label("My name:"),
                nameTextField,
                new Label("IP:"),
                ipTextField,
                new Label("Port:"),
                portTextField,
                new Label("Select a Book:"),
                bookListView,
                connectButton);

        Scene loginFormScene = new Scene(loginFormBox, 400, 500);
        primaryStage.setScene(loginFormScene);
    }

    private void showGameFlowWindow() {
        // Implement the code to display the game flow window
        // You can use a new Scene and a different layout container to represent the
        // game flow window
        Platform.runLater(() -> {
            // VBox gameFlowBox = new VBox(10);
            // gameFlowBox.setPadding(new Insets(20));
            // gameFlowBox.setAlignment(Pos.CENTER);

            // GridPane boardGridPane = createBoardGridPane();

            // gameFlowBox.getChildren().add(boardGridPane);
            // Scene gameFlowScene = new Scene(gameFlowBox, 800, 600);
            // primaryStage.setScene(gameFlowScene);

            BorderPane root = new BorderPane();

            // Create the sidebar
            VBox sidebar = createSidebar();
            root.setRight(sidebar);

            // Create the game board
            GridPane boardGridPane = createBoardGridPane();
            root.setCenter(boardGridPane);

            // Create the buttons at the bottom
            // HBox buttons = createButtons();
            // root.setBottom(buttons);

            Scene gameFlowScene = new Scene(root, 800, 600);
            primaryStage.setScene(gameFlowScene);
        });
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));

        // Player Name
        Label nameLabel = new Label("Player Name:");
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(gameViewModel.getPlayerNameProperty());

        // Player Score
        Label scoreLabel = new Label("Score:");
        Label scoreValueLabel = new Label();
        scoreValueLabel.textProperty().bind(gameViewModel.getPlayerScoreProperty());

        // Player trun
        Label tunrLabel = new Label("My turn:");
        Label tunrValueLabel = new Label();
        scoreValueLabel.textProperty().bind(gameViewModel.getPlayerTurnProperty());

        // Player Words
        Label wordsLabel = new Label("Words:");
        ListView<String> wordsListView = new ListView<>();
        wordsListView.setItems(gameViewModel.getPlayerWords());

        // Player Tiles
        Label tilesLabel = new Label("Tiles:");
        ListView<String> tilesListView = new ListView<>();
        tilesListView.setItems(gameViewModel.getPlayerTiles());

        // Other Players' Scores
        Label othersScoreLabel = new Label("Other Players' Scores:");
        ListView<String> othersScoreListView = new ListView<>();
        othersScoreListView.setItems(gameViewModel.getOthersScores());

        sidebar.getChildren().addAll(
                nameLabel, nameValueLabel,
                scoreLabel, scoreValueLabel,
                tunrLabel, tunrValueLabel,
                wordsLabel, wordsListView,
                tilesLabel, tilesListView,
                othersScoreLabel, othersScoreListView);

        return sidebar;
    }

    private GridPane createBoardGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        // // Get player information from the view model
        // String playerName = gameViewModel.getPlayerProperties().getMyName();
        // int playerScore = gameViewModel.getPlayerProperties().getMyScore();
        // List<String> playerWords = new ArrayList<>();
        // for (Word w : gameViewModel.getPlayerProperties().getMyWords()) {
        //     playerWords.add(w.toString());
        // }
        // List<Character> playerTiles = new ArrayList<>();
        // for (Tile t : gameViewModel.getPlayerProperties().getMyHandTiles()) {
        //     playerTiles.add(t.getLetter());
        // }
        // Map<String, Integer> otherPlayers = gameViewModel.getPlayerProperties().getPlayersScore();

        // // Create labels to display player information
        // Label playerNameLabel = new Label(playerName);
        // Label playerScoreLabel = new Label("My Score: " + playerScore);
        // Label playerWordsLabel = new Label("My Words:");
        // ListView<String> playerWordsListView = new ListView<>(FXCollections.observableArrayList(playerWords));
        // Label playerTilesLabel = new Label("My Hand Tiles:");
        // ListView<Character> playerTilesListView = new ListView<>(FXCollections.observableArrayList(playerTiles));

        // // Create labels to display other players' information
        // Label otherPlayersLabel = new Label("Other Players scores:");
        // GridPane otherPlayersGridPane = createOtherPlayersGridPane(otherPlayers);

        // // Add player information labels to the grid pane
        // gridPane.add(playerNameLabel, 0, 0);
        // gridPane.add(playerScoreLabel, 0, 1);
        // gridPane.add(playerWordsLabel, 0, 2);
        // gridPane.add(playerWordsListView, 0, 3);
        // gridPane.add(playerTilesLabel, 0, 4);
        // gridPane.add(playerTilesListView, 0, 5);

        // // Add other players information labels to the grid pane
        // gridPane.add(otherPlayersLabel, 1, 0);
        // gridPane.add(otherPlayersGridPane, 1, 1, 1, 5);

        // Add board tiles
        Tile[][] board = gameViewModel.getCurrentBoard();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Tile tile = board[row][col];
                Label label;
                if (tile == null) {
                    label = new Label(String.valueOf(' '));
                } else {
                    label = new Label(String.valueOf(tile.getLetter()));
                }
                label.setStyle("-fx-border-color: black;");
                label.setPrefSize(30, 30);
                gridPane.add(label, col + 2, row);
            }
        }

        return gridPane;
    }

    private GridPane createOtherPlayersGridPane(Map<String, Integer> otherPlayers) {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);

        // Create a label for each player
        int rowIndex = 0;
        for (Map.Entry<String, Integer> entry : otherPlayers.entrySet()) {
            String playerName = entry.getKey();
            int playerScore = entry.getValue();

            Label nameLabel = new Label(playerName);
            Label scoreLabel = new Label("Score: " + playerScore);

            gridPane.addRow(rowIndex, nameLabel, scoreLabel);
            rowIndex++;
        }

        return gridPane;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("UPDATE");
        if (o == gameViewModel) {
            System.out.println("GOT UPDATE FROM VIEW-MODEL");
            if (!gameStarted) {
                gameStarted = true;
                showGameFlowWindow();
            } 
            // else
            //     showGameFlowWindow();
        }
    }
}
