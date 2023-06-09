package app.view;

import java.util.*;

import app.model.game.Tile;
import app.view_model.GameViewModel;
import app.view_model.MessageReader;
import javafx.beans.binding.*;
import javafx.scene.paint.Color;
import javafx.application.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class Main extends Application implements Observer {

    private GameViewModel gameViewModel;
    private Stage primaryStage;
    private String myName;
    private String MODE;
    String msg = MessageReader.getMsg();
    private Image boardImg = new Image(ClassLoader.getSystemResourceAsStream("resources/board.png"));
    BackgroundImage boardBackgroundImage = new BackgroundImage(
            new Image(ClassLoader.getSystemResourceAsStream("resources/board.png")),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);
    Background boradBackground = new Background(boardBackgroundImage);
    BackgroundImage gameBackgroundImage = new BackgroundImage(
            new Image(ClassLoader.getSystemResourceAsStream("resources/wood.jpg")),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);
    Background gameBackground = new Background(gameBackgroundImage);
    BackgroundImage woodBackgroundImage = new BackgroundImage(
            new Image(ClassLoader.getSystemResourceAsStream("resources/wood4.jpg")),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));

    Background woodBackground = new Background(woodBackgroundImage);

    private Scene gameFlowScene;
    private List<Pane> selectedCells = new ArrayList<>();
    private Button tryPlaceWordButton;
    private ObservableList<Button> tileButtons;
    private boolean cellSelected = false;
    //
    String blueButton = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkblue, grey), blue; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String blueButtonHover = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(blue, grey), navy; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String yellowButton = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(orange, grey), gold; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String yellowButtonHover = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(gold, grey), orange; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greenButton = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(green, grey), limegreen; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greenButtonHover = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkgreen, grey), green; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String redButton = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(red, grey), crimson; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String redButtonHover = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: linear-gradient(firebrick, grey), red; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greyButton = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: linear-gradient(silver, grey), grey; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    String greyButtonHover = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: linear-gradient(darkgray, grey), cadetblue; -fx-background-radius: 10; -fx-text-fill: white; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    //
    String textFieldStyle = "-fx-background-radius: 10px; -fx-background-color: burlywood; -fx-text-fill: darkslategray; -fx-font-size: 20px; -fx-font-weight: bold";

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        this.gameViewModel = new GameViewModel();
        gameViewModel.addObserver(this);

        // Create the first window with Host and Guest buttons
        VBox initialWindowBox = createInitialWindow();
        Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);

        // Set up the primaryStage
        primaryStage.setTitle("Book Scrabble");
        primaryStage.setScene(initialWindowScene);
        primaryStage.show();
    }

    private VBox createInitialWindow() {
        VBox initialWindowBox = new VBox(10);
        initialWindowBox.setPadding(new Insets(20));
        initialWindowBox.setAlignment(Pos.CENTER);
        initialWindowBox.setBackground(woodBackground);
        initialWindowBox.setCursor(Cursor.HAND);

        // Styled header label
        Label headerLabel = new Label("Welcome to Book Scrabble Game");
        headerLabel.setStyle("-fx-font-size: 30px; -fx-font-family: 'Comic Sans MS'; " +
                "-fx-background-radius: 30px; -fx-background-image: url('resources/plank2.png');" +
                "-fx-background-repeat: no-repeat; -fx-background-size: cover; -fx-padding: 15px; " +
                "-fx-text-fill: papayawhip; -fx-effect: dropshadow(gaussian, #000000, 5, 0, 0, 1);");

        Label modeLabel = new Label("Choose Game Mode");
        modeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;" +
                "-fx-background-color: #D2B48C; -fx-padding: 5px; " +
                "-fx-text-fill: black; -fx-effect: dropshadow(gaussian, #000000, 5, 0, 0, 1);");
        // Button pane
        HBox buttonPane = new HBox(10);
        buttonPane.setAlignment(Pos.CENTER);

        Button hostButton = new Button("   Host a\nnew game");
        hostButton.setStyle(greenButton);
        hostButton.setPrefSize(200, 80);

        Button guestButton = new Button(" Connect \nas a guest");
        guestButton.setStyle(yellowButton);
        guestButton.setPrefSize(200, 80);

        // Apply some additional styling to the buttons
        hostButton.setOnMouseEntered(event -> hostButton.setStyle(greenButtonHover));
        hostButton.setOnMouseExited(event -> hostButton.setStyle(greenButton));
        guestButton.setOnMouseEntered(event -> guestButton.setStyle(yellowButtonHover));
        guestButton.setOnMouseExited(event -> guestButton.setStyle(yellowButton));

        buttonPane.getChildren().addAll(hostButton, guestButton);

        // TitledPane for the collapsible section
        TitledPane titledPane = new TitledPane();
        titledPane.setText("What's the difference?");
        titledPane.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titledPane.setExpanded(false);

        Label explanationContentLabel = new Label(
                "In Host Mode, you can host a game for up to 4 players, including yourself.\nAs the host, you run a server and send data to the guests.\nYou can also send word queries to the game server.\n\nIn Guest Mode, you join an existing game hosted by another player.\nAs a guest, you connect to the host's server and participate as\none of the players.\nGuest mode allows you to enjoy the multiplayer experience\nwithout hosting the game.");
        explanationContentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: black;");

        titledPane.setContent(explanationContentLabel);

        // Event handler for hostButton
        hostButton.setOnAction(event -> {
            MODE = "H";
            showHostLoginForm();
        });

        // Event handler for guestButton
        guestButton.setOnAction(event -> {
            MODE = "G";
            gameViewModel.setGameMode(MODE, 0);
            showGuestLoginForm();
        });

        initialWindowBox.getChildren().addAll(headerLabel, modeLabel, buttonPane, titledPane);
        return initialWindowBox;
    }

    private void showBookSelectionWindow() {
        // Create a new dialog window
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Select Books");
        dialog.setHeaderText("Select one or more books");

        // Set the dialog content
        ListView<String> bookListView = new ListView<>(gameViewModel.getBookList());
        bookListView.setCellFactory(param -> new ListCell<String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // Set the book image
                    imageView.setImage(new Image("resources/harry.jpg"));
                    imageView.setFitWidth(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        // Enable multiple selection
        bookListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set the dialog buttons
        ButtonType selectButtonType = new ButtonType("Select", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Enable the select button only if at least one book is selected
        Node selectButton = dialog.getDialogPane().lookupButton(selectButtonType);
        selectButton.setDisable(true);
        bookListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> selectButton
                .setDisable(bookListView.getSelectionModel().getSelectedItems().isEmpty()));

        dialog.getDialogPane().setContent(bookListView);

        // Convert the selected items to a list when the select button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return new ArrayList<>(bookListView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        // Show the dialog and wait for the user's selection
        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(selectedBooks -> {
            // Process the selected books
            for (String book : selectedBooks) {
                // Do something with each selected book
            }
        });
    }

    private void showHostLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setBackground(woodBackground);
        loginFormBox.setCursor(Cursor.HAND);

        Label numOfPlayersLabel = new Label("Select Number of Players:");
        numOfPlayersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
        numOfPlayersComboBox.setValue(2);
        numOfPlayersComboBox.getItems().addAll(2, 3, 4);
        numOfPlayersComboBox.setStyle(textFieldStyle);

        Label nameLabel = new Label("My name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        TextField nameTextField = new TextField();
        nameTextField.setStyle(textFieldStyle);
        nameTextField.setText("Aviv");

        Label gameServerlabel = new Label("Game server is runnig localy on port 11224");
        gameServerlabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gold;");

        Label waintingLabel = new Label("");

        Label selectBookLabel = new Label("Select a Book:");
        selectBookLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        ListView<String> bookListView = new ListView<>(GameViewModel.getBookList());
        Button connectButton = new Button("Connect me");
        connectButton.setStyle(blueButton);
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(blueButtonHover));
        connectButton.setOnMouseExited(e -> connectButton.setStyle(blueButton));

        connectButton.setOnAction(event -> {
            myName = nameTextField.getText();
            /***********************/
            String ip = "localhost";
            int port = 11224;
            /***********************/
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setGameMode(MODE, numOfPlayersComboBox.getValue());
                gameViewModel.connectMe(myName, ip, port);
                gameViewModel.myBookChoice(selectedBook);
                gameViewModel.ready();

                // Disable form elements
                numOfPlayersComboBox.setDisable(true);
                nameLabel.setDisable(true);
                gameServerlabel.setText("");
                gameServerlabel.setDisable(true);
                nameTextField.setDisable(true);
                numOfPlayersLabel.setDisable(true);
                waintingLabel.setText("Waiting for all players to connect");
                waintingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                nameTextField.setDisable(true);
                selectBookLabel.setDisable(true);
                bookListView.setDisable(true);
                connectButton.setDisable(true);

            }
        });

        loginFormBox.getChildren().addAll(
                nameLabel,
                nameTextField, numOfPlayersLabel, numOfPlayersComboBox,
                gameServerlabel, waintingLabel,
                selectBookLabel,
                bookListView,
                connectButton);

        Scene loginFormScene = new Scene(loginFormBox, 400, 550);
        primaryStage.setScene(loginFormScene);
    }

    private void showGuestLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setBackground(woodBackground);

        Label nameLabel = new Label("My name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        TextField nameTextField = new TextField();
        nameTextField.setStyle(textFieldStyle);
        //
        nameTextField.setText("Moshe");

        Label ipLabel = new Label("Host's IP:");
        ipLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        TextField ipTextField = new TextField();
        ipTextField.setStyle(textFieldStyle);
        //
        ipTextField.setText("localhost");

        Label portLabel = new Label("Host's Port:");
        portLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");
        TextField portTextField = new TextField();
        portTextField.setStyle(textFieldStyle);
        //
        portTextField.setText("8040");

        Label booksLabel = new Label("Select a Book:");
        booksLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gainsboro;");

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
                gameViewModel.setGameMode(MODE, 0);
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
                nameLabel,
                nameTextField,
                ipLabel,
                ipTextField,
                portLabel,
                portTextField,
                booksLabel,
                bookListView,
                connectButton);

        Scene loginFormScene = new Scene(loginFormBox, 400, 550);
        primaryStage.setScene(loginFormScene);
    }

    private void showGameFlowWindow() {
        // Implement the code to display the game flow window
        // You can use a new Scene and a different layout container to represent the
        // game flow window
        Platform.runLater(() -> {

            BorderPane root = new BorderPane();

            root.setBackground(gameBackground);
            root.setCursor(Cursor.HAND);
            // Create the sidebar
            VBox sidebar = createSidebar();
            root.setRight(sidebar);

            // Create the game board
            GridPane boardGridPane = createBoardGridPane(boardImg);
            boardGridPane.setMinSize(734, 734); // Set the desired minimum size
            boardGridPane.setMaxSize(734, 734); // Set the desired maximum size

            root.setCenter(boardGridPane);

            // Create the buttons at the bottom
            VBox buttons = createButtons();
            root.setLeft(buttons);

            gameFlowScene = new Scene(root, 1600, 800);
            primaryStage.setScene(gameFlowScene);
        });
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(15));

        // Player Name
        Label nameLabel = new Label("My Name:");
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(gameViewModel.getPlayerNameProperty());
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameValueLabel.setStyle("-fx-font-size: 23px; -fx-font-weight: bold;" +
                "-fx-background-color: silver; -fx-padding: 5px; -fx-border-radius: 30;" +
                "-fx-text-fill: black; -fx-effect: dropshadow(gaussian, #000000, 5, 0, 0, 1);");

        // Player Score
        Label scoreLabel = new Label("Score:");
        Label scoreValueLabel = new Label();
        scoreValueLabel.textProperty().bind(gameViewModel.getPlayerScoreProperty());
        scoreLabel.setStyle("-fx-font-size: 14px;");
        scoreValueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;" +
                "-fx-background-color: silver; -fx-padding: 5px; -fx-border-radius: 30;" +
                "-fx-text-fill: navy; -fx-effect: dropshadow(gaussian, #000000, 5, 0, 0, 1);");
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
        turnLabel.setStyle("-fx-font-size: 14px;");
        turnValueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Player Words
        Label wordsLabel = new Label("My Words:");
        ListView<String> wordsListView = new ListView<>();
        wordsListView.setItems(gameViewModel.getPlayerWords());

        Label othersScoreLabel = new Label("Other Players:");
        othersScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> othersScoreListView = new ListView<>();
        othersScoreListView.setItems(gameViewModel.getOthersScores());

        othersScoreListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle(
                            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: darkblue;");
                    setPadding(new Insets(5));
                }
            }
        });

        sidebar.getChildren().addAll(
                nameLabel, nameValueLabel,
                scoreLabel, scoreValueLabel,
                turnLabel, turnValueLabel,
                wordsLabel, wordsListView,
                // tilesLabel, tilesListView,
                othersScoreLabel, othersScoreListView);

        return sidebar;
    }

    private GridPane createBoardGridPane(Image boardImage) {
        GridPane gridPane = new GridPane();
        // gridPane.setPrefSize(732, 733); // Set the desired size of the grid

        Tile[][] board = gameViewModel.getCurrentBoard();
        int boardSize = board.length;

        // Set the background image
        gridPane.setBackground(boradBackground);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Pane cellPane = new Pane();
                cellPane.setPrefSize(60, 60);

                Label letterLabel;
                if (board[row][col] == null) {
                    letterLabel = new Label(null);
                    cellPane.setStyle("-fx-border-color: transparent;");
                } else {
                    letterLabel = new Label(String.valueOf(board[row][col].getLetter()));
                    cellPane.setStyle("-fx-background-color: transparent; -fx-background-image: url('resources/tiles/"
                            + letterLabel.getText() + ".png');" +
                            "-fx-background-size: cover; -fx-border-color: transparent; -fx-border-radius: 4px;");
                }

                cellPane.setOnMouseClicked(event -> {
                    if (gameViewModel.isMyTurn() && !cellSelected) {
                        String cellStyle = cellPane.getStyle();
                        // Toggle cell selection
                        if (selectedCells.contains(cellPane)) {
                            selectedCells.remove(cellPane);
                            cellPane.setStyle("-fx-border-color: transparent;");
                        } else if (selectedCells.size() < 2) {
                            selectedCells.add(cellPane);
                            cellPane.setStyle(cellStyle + "-fx-alignment: center;" +
                                    "-fx-border-color: black; -fx-border-width: 4px; -fx-border-radius: 5px;");
                        } else if (selectedCells.size() == 2 && selectedCells.contains(cellPane)) {
                            selectedCells.remove(cellPane);
                            cellPane.setStyle(cellStyle.replace(
                                    "-fx-border-color: black; -fx-border-width: 4px; -fx-border-radius: 5px;", ""));
                        } else {
                            // Un-select one of the selected cells
                            Pane selectedCell = selectedCells.get(0);
                            selectedCells.remove(selectedCell);
                            selectedCell.setStyle(cellStyle.replace(
                                    "-fx-border-color: black; -fx-border-width: 4px; -fx-border-radius: 5px;", ""));

                            selectedCells.add(cellPane);
                            cellPane.setStyle(cellStyle + "-fx-alignment: center;" +
                                    "-fx-border-color: black; -fx-border-width: 4px; -fx-border-radius: 5px;");
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
        messageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create a FlowPane to hold the tile buttons
        FlowPane tilePane = new FlowPane(10, 10);
        tilePane.setAlignment(Pos.CENTER);

        // Clear existing tile buttons
        tilePane.getChildren().clear();

        // Create the tile buttons and add them to the FlowPane
        Button resetTurnButton = new Button("\u2B6F");

        tileButtons = gameViewModel.getButtonTiles();
        for (Button tileButton : tileButtons) {
            tileButton.setPrefSize(45, 45);
            tileButton.setStyle("-fx-background-color: transparent; -fx-background-image: url('resources/tiles/"
                    + tileButton.getText() + ".png');" +
                    "-fx-background-size: cover;");
            String letter = tileButton.getText();
            tileButton.setText(null);
            if (!gameViewModel.isMyTurn()) {
                tileButton.setDisable(true);
            }
            tileButton.setOnAction(event -> {
                // Disable the tile button upon selection
                tileButton.setDisable(true);
                resetTurnButton.setDisable(false);
                // Add the tile value to the word
                gameViewModel.addToWord(letter);
            });

            tilePane.getChildren().add(tileButton);
        }

        // Reset button
        resetTurnButton.setStyle(greyButton);
        resetTurnButton.setOnMouseEntered(e -> resetTurnButton.setStyle(greyButtonHover));
        resetTurnButton.setOnMouseExited(e -> resetTurnButton.setStyle(greyButton));
        resetTurnButton.setDisable(true);
        resetTurnButton.setOnAction(event -> {
            // Call the method to handle the "Pass Turn" action
            for (Button tb : tileButtons) {
                tb.setDisable(false);
                resetTurnButton.setDisable(true);
                gameViewModel.clearWord();
            }
        });
        // Set the preferred width and height of the button
        resetTurnButton.setPrefWidth(60); // Adjust the width as desired
        resetTurnButton.setPrefHeight(20); // Adjust the height as desired

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
        challengeButton.setDisable(true);
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
            tryPlaceWordButton.setDisable(true);
            challengeButton.setDisable(false);

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

        buttonsBox.getChildren().addAll(messageLabel, tilePane, resetTurnButton, tryPlaceWordButton, passTurnButton,
                challengeButton,
                quitGameButton);

        return buttonsBox;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == gameViewModel) {
            // PRINT DEBUG
            System.out.println("VIEW GOT UPDATE");
            showGameFlowWindow();
        }
    }
}
