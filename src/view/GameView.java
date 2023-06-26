package view;

import view_model.GameViewModel;
import view_model.MessageReader;

import java.util.*;

import javafx.beans.binding.*;
import javafx.scene.paint.Color;
import javafx.application.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.game.Tile;

public class GameView extends Application implements Observer {
    private Stage primaryStage;
    private GameViewModel gameViewModel;
    private String myName;
    private String MODE;
    private int numOfPlayers;

    Scene gameFlowScene;
    VBox sidebar;
    GridPane boardGridPane;
    VBox buttons;
    private List<Pane> selectedCells = new ArrayList<>();
    private Button tryPlaceWordButton;
    private ObservableList<Button> tileButtons;
    private boolean cellSelected = false;
    String blueButton = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkblue, grey), blue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String blueButtonHover = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(blue, grey), navy; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String yellowButton = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(orange, grey), gold; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String yellowButtonHover = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(gold, grey), orange; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greenButton = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(green, grey), limegreen; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greenButtonHover = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkgreen, grey), green; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String redButton = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(red, grey), crimson; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String redButtonHover = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(firebrick, grey), red; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        this.gameViewModel = new GameViewModel();
        gameViewModel.addObserver(this);

        // Create the first window with Host and Guest buttons
        VBox initialWindowBox = createInitialWindow();
        Scene initialWindowScene = new Scene(initialWindowBox, 500, 410);

        // Set up the primaryStage
        primaryStage.setTitle("Book Scrabble");
        primaryStage.setScene(initialWindowScene);
        primaryStage.show();
    }

    private VBox createInitialWindow() {
        VBox initialWindowBox = new VBox(10);
        initialWindowBox.setPadding(new Insets(20));
        initialWindowBox.setAlignment(Pos.CENTER);

        // Styled header label
        Label headerLabel = new Label("Welcome to Book Scrabble Game!");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label modeLabel = new Label("Choose Game Mode");
        modeLabel.setStyle("-fx-font-size: 12px;");

        // Button pane
        HBox buttonPane = new HBox(10);
        buttonPane.setAlignment(Pos.CENTER);

        Button hostButton = new Button("Host");
        hostButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkblue, black), blue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );");

        hostButton.setPrefSize(150, 50);

        Button guestButton = new Button("Guest");
        guestButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(blue, black), deepskyblue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );");
        guestButton.setPrefSize(150, 50);

        // Apply some additional styling to the buttons
        hostButton.setOnMouseEntered(event -> hostButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(blue, black), navy; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );"));
        hostButton.setOnMouseExited(event -> hostButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkblue, black), blue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );"));
        guestButton.setOnMouseEntered(event -> guestButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkblue, black), dodgerblue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );"));
        guestButton.setOnMouseExited(event -> guestButton.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: linear-gradient(blue, black), deepskyblue; -fx-background-radius: 5; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );"));

        buttonPane.getChildren().addAll(hostButton, guestButton);

        // TitledPane for the collapsible section
        TitledPane titledPane = new TitledPane();
        titledPane.setText("What's the difference?");
        titledPane.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        titledPane.setExpanded(false);

        Label explanationContentLabel = new Label(
                "In Host Mode, you will act as the game host and have the ability to host a game\nfor up to 4 players, including yourself.\nAs the host, you will run a server that connects and sends data to the guests.\nAdditionally, in host mode, you will have the capability to send word queries\nto the game server.\n\nOn the other hand, in Guest Mode, you will be joining an existing game hosted\nby another player.\nAs a guest, you will connect to the host's server and participate in the game as\none of the players.\nGuest mode provides an opportunity for you to engage with the game and\nenjoy the multiplayer experience without the responsibility of hosting the game.");
        explanationContentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: grey;");

        titledPane.setContent(explanationContentLabel);

        // Event handler for hostButton
        hostButton.setOnAction(event -> {
            MODE = "H";
            showHostWindow();
        });

        // Event handler for guestButton
        guestButton.setOnAction(event -> {
            MODE = "G";
            gameViewModel.setGameMode(MODE, 0);
            showLoginForm();
        });

        initialWindowBox.getChildren().addAll(headerLabel, modeLabel, buttonPane, titledPane);
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
        ipTextField.setText("localhost");
        TextField portTextField = new TextField();
        ListView<String> bookListView = new ListView<>(GameViewModel.getBookList());
        Button connectButton = new Button("Connect me");
        connectButton.setStyle(blueButton);
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(blueButtonHover));
        connectButton.setOnMouseExited(e -> connectButton.setStyle(blueButton));

        connectButton.setOnAction(event -> {
            myName = nameTextField.getText();
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setGameMode(MODE, numOfPlayers);
                gameViewModel.connectMe(myName, ip, port);
                gameViewModel.myBookChoice(selectedBook);
                gameViewModel.ready();

                // Disable form elements
                nameTextField.setDisable(true);
                ipTextField.setDisable(true);
                portTextField.setDisable(true);
                bookListView.setDisable(true);
                connectButton.setDisable(true);

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

        Scene loginFormScene = new Scene(loginFormBox, 300, 500);
        primaryStage.setScene(loginFormScene);
    }

    private void showGameFlowWindow() {
        // Implement the code to display the game flow window
        // You can use a new Scene and a different layout container to represent the
        // game flow window
        Platform.runLater(() -> {

            BorderPane root = new BorderPane();

            // Create the sidebar
            VBox sidebar = createSidebar();
            root.setRight(sidebar);

            // Create the game board
            GridPane boardGridPane = createBoardGridPane();
            root.setCenter(boardGridPane);

            // Create the buttons at the bottom
            VBox buttons = createButtons();
            root.setLeft(buttons);

            gameFlowScene = new Scene(root, 1300, 600);
            primaryStage.setScene(gameFlowScene);
        });
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(15));

        // Player Name
        Label nameLabel = new Label("Player Name:");
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(gameViewModel.getPlayerNameProperty());
        nameLabel.setStyle("-fx-font-size: 12px;");
        nameValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Player Score
        Label scoreLabel = new Label("Score:");
        Label scoreValueLabel = new Label();
        scoreValueLabel.textProperty().bind(gameViewModel.getPlayerScoreProperty());
        scoreLabel.setStyle("-fx-font-size: 12px;");
        scoreValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scoreValueLabel.setTextFill(Color.NAVY);

        // Player Turn
        Label turnLabel = new Label("My Turn:");
        Label turnValueLabel = new Label();

        StringBinding turnBinding = Bindings.createStringBinding(() -> {
            String turn = gameViewModel.getPlayerTurnProperty().getValue();
            if ("true".equals(turn)) {
                turnValueLabel.setTextFill(Color.GREEN);
            } else {
                turnValueLabel.setTextFill(Color.RED);
            }
            return turn;
        }, gameViewModel.getPlayerTurnProperty());

        turnValueLabel.textProperty().bind(turnBinding);
        turnLabel.setStyle("-fx-font-size: 12px;");
        turnValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Player Words
        Label wordsLabel = new Label("Words:");
        ListView<String> wordsListView = new ListView<>();
        wordsListView.setItems(gameViewModel.getPlayerWords());

        // Other Players' Scores
        Label othersScoreLabel = new Label("Other Players' Scores:");
        ListView<String> othersScoreListView = new ListView<>();
        othersScoreListView.setItems(gameViewModel.getOthersScores());

        sidebar.getChildren().addAll(
                nameLabel, nameValueLabel,
                scoreLabel, scoreValueLabel,
                turnLabel, turnValueLabel,
                wordsLabel, wordsListView,
                // tilesLabel, tilesListView,
                othersScoreLabel, othersScoreListView);

        return sidebar;
    }

    private GridPane createBoardGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);

        Tile[][] board = gameViewModel.getCurrentBoard();
        int boardSize = board.length;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Pane cellPane = new Pane();
                if (row == 7 && col == 7) {
                    cellPane.setStyle("-fx-border-color: black; -fx-alignment: center; -fx-background-color: green;");
                } else {

                    cellPane.setStyle(
                            "-fx-border-color: black; -fx-alignment: center;");
                }
                cellPane.setPrefSize(60, 60);

                Label letterLabel;
                if (board[row][col] == null) {
                    letterLabel = new Label("");
                } else {
                    letterLabel = new Label(String.valueOf(board[row][col].getLetter()));
                    cellPane.setStyle("-fx-background-color: #FFA500; -fx-border-color: black;");

                }
                letterLabel.setStyle("-fx-font-size: 27px; -fx-font-weight: bold;");
                letterLabel.setAlignment(Pos.CENTER);
                letterLabel.setPrefWidth(30);
                letterLabel.setPrefHeight(30);

                cellPane.getChildren().add(letterLabel);

                cellPane.setOnMouseClicked(event -> {
                    if (gameViewModel.isMyTurn() && !cellSelected) {
                        if (selectedCells.size() == 2) {
                            // If already two cells are selected, reset the selection
                            for (Pane selectedCell : selectedCells) {
                                selectedCell.setStyle("-fx-border-color: black;");
                            }
                            selectedCells.clear();
                        }

                        // Toggle cell selection
                        if (selectedCells.contains(cellPane)) {
                            selectedCells.remove(cellPane);
                            cellPane.setStyle("-fx-border-color: black;");
                        } else {
                            selectedCells.add(cellPane);
                            cellPane.setStyle("-fx-border-color: green; -fx-border-width: 3px;");
                        }

                        // Enable/disable buttons based on the number of selected cells
                        boolean enableButtons = selectedCells.size() == 2;
                        tryPlaceWordButton.setDisable(!enableButtons);
                        for (Button b : tileButtons) {
                            b.setDisable(!enableButtons);
                        }
                        if (enableButtons) {
                            int firstRow = GridPane.getRowIndex(selectedCells.get(0));
                            int firstCol = GridPane.getColumnIndex(selectedCells.get(0));
                            int lastRow = GridPane.getRowIndex(selectedCells.get(1));
                            int lastCol = GridPane.getColumnIndex(selectedCells.get(1));

                            gameViewModel.setFirstSelectedCellRow(firstRow);
                            gameViewModel.setFirstSelectedCellCol(firstCol);
                            gameViewModel.setLastSelectedCellRow(lastRow);
                            gameViewModel.setLastSelectedCellCol(lastCol);
                        } else {
                            gameViewModel.clearSelectedCells();
                        }
                    }
                });

                gridPane.add(cellPane, col, row);
            }
        }

        return gridPane;
    }

    private VBox createButtons() {
        VBox buttonsBox = new VBox(10);
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.setAlignment(Pos.CENTER);

        Label messageLabel = new Label(MessageReader.getMsg());
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create a FlowPane to hold the tile buttons
        FlowPane tilePane = new FlowPane(10, 10);
        tilePane.setAlignment(Pos.CENTER);

        // Clear existing tile buttons
        tilePane.getChildren().clear();

        // Create the tile buttons and add them to the FlowPane
        tileButtons = gameViewModel.getButtonTiles();
        for (Button tileButton : tileButtons) {
            tileButton.setPrefSize(40, 40);
            tileButton.setStyle(
                    "-fx-background-color: #FFA500; -fx-font-size: 16px; -fx-border-color: black; -fx-font-weight: bold;");
            if (!gameViewModel.isMyTurn()) {
                tileButton.setDisable(true);
            }
            tileButton.setOnAction(event -> {
                // Disable the tile button upon selection
                tileButton.setDisable(true);
                // Add the tile value to the word
                gameViewModel.addToWord(tileButton.getText());
            });

            tilePane.getChildren().add(tileButton);
        }

        Button passTurnButton = new Button("Pass Turn");
        passTurnButton.setStyle(yellowButton);
        passTurnButton.setOnMouseEntered(e -> passTurnButton.setStyle(yellowButtonHover));
        passTurnButton.setOnMouseExited(e -> passTurnButton.setStyle(yellowButton));
        passTurnButton.setOnAction(event -> {
            // Call the method to handle the "Pass Turn" action
            gameViewModel.skipTurn();
        });

        Button challengeButton = new Button("Challenge");
        challengeButton.setStyle(greenButton);
        challengeButton.setOnMouseEntered(e -> challengeButton.setStyle(greenButtonHover));
        challengeButton.setOnMouseExited(e -> challengeButton.setStyle(greenButton));
        challengeButton.setOnAction(event -> {
            // Call the method to handle the "Challenge" action
            gameViewModel.challenge();
        });

        if (!gameViewModel.isMyTurn()) {
            challengeButton.setDisable(true);
            passTurnButton.setStyle(yellowButton);
            passTurnButton.setDisable(true);
            //

        }

        Button quitGameButton = new Button("Quit Game");
        quitGameButton.setStyle(redButton);
        quitGameButton.setOnMouseEntered(e -> quitGameButton.setStyle(redButtonHover));
        quitGameButton.setOnMouseExited(e -> quitGameButton.setStyle(redButton));
        quitGameButton.setOnAction(event -> {
            // Call the method to handle the "Quit Game" action
            gameViewModel.quitGame();
            // Get the Stage object of the window you want to close
            Stage stage = (Stage) primaryStage.getScene().getWindow();

            // Call the close() method to close the window
            stage.close();
        });

        tryPlaceWordButton = new Button("Try Place Word");
        tryPlaceWordButton.setStyle(blueButton);
        tryPlaceWordButton.setOnMouseEntered(e -> tryPlaceWordButton.setStyle(blueButtonHover));
        tryPlaceWordButton.setOnMouseExited(e -> tryPlaceWordButton.setStyle(blueButton));
        tryPlaceWordButton.setOnAction(event -> {

            String word = gameViewModel.getWord();
            int firstRow = gameViewModel.getFirstSelectedCellRow();
            int firstCol = gameViewModel.getFirstSelectedCellCol();
            int lastRow = gameViewModel.getLastSelectedCellRow();
            int lastCol = gameViewModel.getLastSelectedCellCol();

            // Call the method to handle the "Try Place Word" action with the collected data
            gameViewModel.tryPlaceWord(word, firstRow, firstCol, lastRow, lastCol);

            // gameViewModel.clearPlayerTiles();
            gameViewModel.clearWord();

            // Clear existing tile buttons
            // tilePane.getChildren().clear();

        });
        tryPlaceWordButton.setDisable(true);

        buttonsBox.getChildren().addAll(messageLabel, tilePane, tryPlaceWordButton, passTurnButton, challengeButton, quitGameButton);
        return buttonsBox;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel) {
            System.out.println("VIEW GOT UPDATE");

            showGameFlowWindow();

        }
    }
}
