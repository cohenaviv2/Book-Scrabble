package app.view;

import java.util.*;

import app.model.game.Tile;
import app.model.host.HostModel;
import app.view_model.GameViewModel;
import app.view_model.MessageReader;
import javafx.beans.binding.*;
import javafx.scene.paint.Color;
import javafx.application.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Main extends Application implements Observer {

    private GameViewModel gameViewModel;
    private Stage primaryStage;
    private String myName;
    private String MODE;
    String msg = MessageReader.getMsg();
  

    private Scene gameFlowScene;
    private List<String> selectedBooks = new ArrayList<>();
    private String MYBOOK;
    private List<Pane> selectedCells = new ArrayList<>();
    private Button tryPlaceWordButton;
    private ObservableList<Button> tileButtons;
    private boolean cellSelected = false;

    String CSS_STYLESHEET = getClass().getResource("/style.css").toExternalForm();

    private double xOffset = 0;
    private double yOffset = 0;
    HBox osBar;

    
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) osBar.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    private void handleMousePressed(MouseEvent event) {
        Stage stage = (Stage) osBar.getScene().getWindow();

        xOffset = event.getScreenX() - stage.getX();
        yOffset = event.getScreenY() - stage.getY();
    }

    HBox createOsBar(Boolean gameIsRunning) {
        osBar = new HBox();
        osBar.setSpacing(10);
        osBar.getStyleClass().add("os-bar");
        osBar.setMinHeight(30);

        Button closeButton = new Button("\u2718");
        closeButton.getStyleClass().add("red-button");

        if (gameIsRunning) {
            closeButton.setOnAction(event -> {
                this.gameViewModel.quitGame();
                Stage stage = (Stage) primaryStage.getScene().getWindow();
                stage.close();
                System.exit(0);
            });
        } else {
            closeButton.setOnAction(event -> {
                if (MODE == "H")
                    HostModel.get().stopHostServer();
                System.exit(0);
            });
        }

        Button minimizeButton = new Button("_");
        minimizeButton.getStyleClass().add("green-button");

        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });

        osBar.getChildren().addAll(closeButton, minimizeButton);
        osBar.setOnMousePressed(this::handleMousePressed);
        osBar.setOnMouseDragged(this::handleMouseDragged);

        return osBar;
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        this.gameViewModel = new GameViewModel();
        gameViewModel.addObserver(this);

        // Create the first window with Host and Guest buttons
        VBox initialWindowBox = createInitialWindow();
        Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);
        initialWindowBox.setCursor(Cursor.HAND);
        initialWindowBox.getStylesheets().add(CSS_STYLESHEET);

        // Set up the primaryStage
        primaryStage.setTitle("Book Scrabble");
        primaryStage.setScene(initialWindowScene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    private void openCustomWindow(String content) {
        Stage customStage = new Stage();
        customStage.initStyle(StageStyle.UNDECORATED);

        // Calculate the center coordinates of the main stage
        double mainStageX = primaryStage.getX();
        double mainStageY = primaryStage.getY();
        double mainStageWidth = primaryStage.getWidth();
        double mainStageHeight = primaryStage.getHeight();

        double customStageWidth = 500; // Adjust as needed
        double customStageHeight = 350; // Adjust as needed

        double customStageX = mainStageX + (mainStageWidth - customStageWidth) / 2;
        double customStageY = mainStageY + (mainStageHeight - customStageHeight) / 2;

        customStage.setX(customStageX);
        customStage.setY(customStageY);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("content-label");

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("red-button");
        closeButton.setOnAction(event -> customStage.close());

        StackPane topPane = new StackPane(closeButton);
        BorderPane.setAlignment(topPane, Pos.TOP_CENTER);
        topPane.setPadding(new Insets(40, 0, 0, 0)); // Add padding to the top

        VBox centerContent = new VBox(10, contentLabel);
        centerContent.setAlignment(Pos.CENTER);

        BorderPane customRoot = new BorderPane();
        customRoot.getStyleClass().add("content-background");
        customRoot.setTop(topPane);
        customRoot.setCenter(centerContent);

        Scene customScene = new Scene(customRoot, customStageWidth, customStageHeight);
        customScene.getStylesheets().add(CSS_STYLESHEET);
        customScene.setCursor(Cursor.HAND);
        customStage.setScene(customScene);
        customStage.show();
    }

    private VBox createInitialWindow() {
        VBox initialWindowBox = new VBox(10);
        initialWindowBox.setPadding(new Insets(20));
        initialWindowBox.setAlignment(Pos.CENTER);
        initialWindowBox.getStyleClass().add("wood-background");

        // Styled header label
        Label headerLabel = new Label("Book Scrabble");
        headerLabel.getStyleClass().add("book-scrabble-header");

        Label modeLabel = new Label("Choose Game Mode");
        modeLabel.getStyleClass().add("mode-label");

        // Button pane
        HBox buttonPane = new HBox(10);
        buttonPane.setAlignment(Pos.CENTER);

        Button hostButton = new Button("Host Mode");
        hostButton.getStyleClass().add("red-button");
        hostButton.setPrefSize(200, 80);

        Button guestButton = new Button("Guest Mode");
        guestButton.getStyleClass().add("blue-button");
        guestButton.setPrefSize(200, 80);

        buttonPane.getChildren().addAll(hostButton, guestButton);

        Button helpButton = new Button("\u2753");
        helpButton.getStyleClass().add("purple-button");
        helpButton.setOnAction(e -> openCustomWindow(
                "In Host Mode, you can host a game for up to 4 players,\nincluding yourself.\nAs the host, you run a server and send data to the guests.\nYou can also send word queries to the game server.\n\nIn Guest Mode, you join an existing game hosted by another player.\nAs a guest, you connect to the host's server and participate as\none of the players.\nGuest mode allows you to enjoy the multiplayer experience\nwithout hosting the game."));

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

        Label justLabel = new Label("");
        Label justLabel2 = new Label("");

        HBox bar = createOsBar(false);
        initialWindowBox.getChildren().addAll(bar, headerLabel, justLabel, modeLabel, buttonPane, justLabel2,
                helpButton);
        return initialWindowBox;
    }

    private void showBookSelectionWindow() {
        Stage bookSelectionStage = new Stage();
        bookSelectionStage.initStyle(StageStyle.UNDECORATED);
        bookSelectionStage.setTitle("Select Books");

        VBox rootContainer = new VBox(40);
        rootContainer.setPadding(new Insets(10));
        rootContainer.getStyleClass().add("wood-background");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: black;");

        FlowPane bookContainer = new FlowPane(40, 40);
        bookContainer.getStyleClass().add("night-sky-background");
        bookContainer.setAlignment(Pos.CENTER);
        bookContainer.setPadding(new Insets(80));

        for (String book : GameViewModel.getBookList()) {
            VBox bookBox = new VBox(10);
            bookBox.setAlignment(Pos.CENTER);

            ImageView imageView = new ImageView();
            imageView.setImage(new Image("books_images/" + book + ".jpg"));
            imageView.setFitWidth(120); // Adjust the width of the image here
            imageView.setPreserveRatio(true);

            Label bookLabel = new Label(book);
            bookLabel.setAlignment(Pos.CENTER);
            bookLabel.getStyleClass().add("book-label");
            bookLabel.setTextFill(Color.WHITE);

            bookBox.getChildren().addAll(imageView, bookLabel);

            bookBox.setOnMouseClicked(event -> {
                if (selectedBooks.contains(book)) {
                    selectedBooks.remove(book);
                    imageView.getStyleClass().remove("selected-book-image");
                    MYBOOK = null;
                } else {
                    selectedBooks.add(book);
                    MYBOOK = book;
                    imageView.getStyleClass().add("selected-book-image");
                }
            });

            bookContainer.getChildren().add(bookBox);
        }

        scrollPane.setContent(bookContainer);

        HBox buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);

        Button nextButton = new Button("Done");
        nextButton.getStyleClass().add("green-button");
        nextButton.setPrefHeight(60);
        nextButton.setPrefWidth(120);
        nextButton.setOnAction(event -> {
            // Handle the action with the selected books
            // e.g., pass the selectedBooks list to the next screen or perform an action

            bookSelectionStage.close();
        });

        buttonPane.getChildren().add(nextButton);

        rootContainer.getChildren().addAll(scrollPane, buttonPane);
        // VBox.setVgrow(scrollPane, Priority.ALWAYS);
        Scene bookSelectionScene = new Scene(rootContainer, 900, 700);
        bookSelectionScene.getStylesheets().add(CSS_STYLESHEET);
        bookSelectionScene.setCursor(Cursor.HAND);
        bookSelectionStage.setScene(bookSelectionScene);
        bookSelectionStage.show();
    }

    private void showHostLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setSpacing(22);
        loginFormBox.getStyleClass().add("wood-background");

        // My name
        Label nameLabel = new Label("My name:");
        nameLabel.getStyleClass().add("login-label");
        TextField nameTextField = new TextField();
        //
        nameTextField.setText("Aviv");
        //
        nameTextField.setAlignment(Pos.CENTER);
        nameTextField.setMaxWidth(200);
        nameTextField.getStyleClass().add("text-field");

        // Number of players
        Label numOfPlayersLabel = new Label("Number of Players:");
        numOfPlayersLabel.getStyleClass().add("login-label");
        ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
        numOfPlayersComboBox.setValue(2);
        numOfPlayersComboBox.getItems().addAll(2, 3, 4);
        numOfPlayersComboBox.getStyleClass().add("text-field");

        // Select books
        Label selectBookLabel = new Label("Select a Book:");
        selectBookLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> showBookSelectionWindow());

        // game server label
        Label gameServerlabel = new Label("Game server is uploaded and powered by Oracle Cloud,\nUnubtu 22.04 VM");
        gameServerlabel.setPrefHeight(80);
        gameServerlabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gold;");

        Label waintingLabel = new Label("");

        // Next button
        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("blue-button");
        nextButton.setPrefHeight(60);
        nextButton.setPrefWidth(120);

        nextButton.setOnAction(event -> {
            myName = nameTextField.getText();
            if (selectedBooks.size() != 0) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setGameMode(MODE, numOfPlayersComboBox.getValue());
                gameViewModel.connectMe(myName, "0", 0); // 0 = default - ORACLE_GAME_SERVER
                gameViewModel.myBookChoice(MYBOOK);
                gameViewModel.ready();

                // Disable form elements

                numOfPlayersComboBox.setDisable(true);
                booksButton.setDisable(true);
                nameLabel.setDisable(true);
                gameServerlabel.setText("");
                gameServerlabel.setDisable(true);
                nameTextField.setDisable(true);
                numOfPlayersLabel.setDisable(true);
                waintingLabel.setText("Waiting for all players to connect");
                waintingLabel
                        .setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: lightgoldenrodyellow;");
                waintingLabel.setTextFill(Color.BLACK);
                nameTextField.setDisable(true);
                nextButton.setDisable(true);

            }
        });

        HBox osBar = createOsBar(false);

        loginFormBox.getChildren().addAll(
                osBar, nameLabel,
                nameTextField, selectBookLabel, booksButton, numOfPlayersLabel, numOfPlayersComboBox,
                gameServerlabel, waintingLabel,
                nextButton);

        Scene hostLoginFormScene = new Scene(loginFormBox, 400, 600);
        hostLoginFormScene.getStylesheets().add(CSS_STYLESHEET);
        primaryStage.setScene(hostLoginFormScene);
    }

    private void showGuestLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.getStyleClass().add("wood-background");

        // My name
        Label nameLabel = new Label("My name:");
        nameLabel.getStyleClass().add("login-label");
        TextField nameTextField = new TextField();
        //
        nameTextField.setText("Moshe");
        //
        nameTextField.setMaxWidth(200);
        nameTextField.setAlignment(Pos.CENTER);
        nameTextField.getStyleClass().add("text-field");

        // Select books
        Label booksLabel = new Label("Select a Book:");
        booksLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> showBookSelectionWindow());

        // Host's ip
        Label ipLabel = new Label("Host's IP:");
        ipLabel.getStyleClass().add("login-label");
        TextField ipTextField = new TextField();
        //
        ipTextField.setText("localhost");
        //
        ipTextField.setMaxWidth(250);
        ipTextField.setAlignment(Pos.CENTER);
        ipTextField.getStyleClass().add("text-field");

        // Host's port
        Label portLabel = new Label("Host's Port:");
        portLabel.getStyleClass().add("login-label");
        TextField portTextField = new TextField();
        //
        portTextField.setText("8040");
        //
        portTextField.setMaxWidth(150);
        portTextField.setAlignment(Pos.CENTER);
        portTextField.getStyleClass().add("text-field");

        HBox roundButtonsPane = new HBox(10);
        roundButtonsPane.setAlignment(Pos.CENTER);
        double preferredHeight = 100; // Set your desired height here
        roundButtonsPane.setMinHeight(preferredHeight);
        roundButtonsPane.setPrefHeight(preferredHeight);

        Button settingButton = new Button("\u263C");
        settingButton.getStyleClass().add("grey-button");

        Button helpButton = new Button("\u2753");
        helpButton.getStyleClass().add("purple-button");

        roundButtonsPane.getChildren().addAll(helpButton, settingButton);

        Label waintingLabel = new Label("");

        // Connect me
        Button connectButton = new Button("Connect me");
        connectButton.getStyleClass().add("blue-button");

        connectButton.setOnAction(event -> {
            myName = nameTextField.getText();
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            if (selectedBooks.size() != 0) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setGameMode(MODE, 0); // 0 = default for guest
                gameViewModel.connectMe(myName, ip, port);
                gameViewModel.myBookChoice(MYBOOK);
                gameViewModel.ready();

                // Disable form elements
                nameTextField.setDisable(true);
                ipTextField.setDisable(true);
                portTextField.setDisable(true);
                connectButton.setDisable(true);
                waintingLabel.setText("Waiting for all players to connect");
                waintingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                waintingLabel.setTextFill(Color.BLACK);
            }
        });

        HBox bar = createOsBar(false);

        loginFormBox.getChildren().addAll(
                bar, nameLabel,
                nameTextField,
                booksLabel,
                booksButton,
                ipLabel,
                ipTextField,
                roundButtonsPane,
                waintingLabel,
                connectButton);

        Scene guestLoginFormScene = new Scene(loginFormBox, 400, 600);
        guestLoginFormScene.getStylesheets().add(CSS_STYLESHEET);
        primaryStage.setScene(guestLoginFormScene);
    }

    private void showGameFlowWindow() {
        // Implement the code to display the game flow window
        // You can use a new Scene and a different layout container to represent the
        // game flow window
        Platform.runLater(() -> {
            BorderPane root = new BorderPane();

            HBox bar = createOsBar(true);
            root.setTop(bar);

            // root.setBackground(gameBackground);
            root.getStyleClass().add("game-flow-background");

            // Create the sidebar
            VBox sidebar = createSidebar();
            root.setRight(sidebar);

            // Create the game board
            GridPane boardGridPane = createBoardGridPane();
            boardGridPane.setMinSize(734, 734); // Set the desired minimum size
            boardGridPane.setMaxSize(734, 734); // Set the desired maximum size

            root.setCenter(boardGridPane);

            // Create the buttons at the bottom
            VBox buttons = createButtons();
            root.setLeft(buttons);

            gameFlowScene = new Scene(root, 1620, 840);
            gameFlowScene.getStylesheets().add(CSS_STYLESHEET);
            gameFlowScene.setCursor(Cursor.HAND);

            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            // Calculate the centered x and y coordinates
            double centerX = (screenWidth - primaryStage.getWidth()) / 2;
            double centerY = (screenHeight - primaryStage.getHeight()) / 2;

            // Set the stage position
            primaryStage.setX(centerX);
            primaryStage.setY(centerY);

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

    private GridPane createBoardGridPane() {
        GridPane gridPane = new GridPane();
        // gridPane.setPrefSize(732, 733); // Set the desired size of the grid

        Tile[][] board = gameViewModel.getCurrentBoard();
        int boardSize = board.length;

        // Set the background image
        gridPane.getStyleClass().add("board-background");
        gridPane.setStyle(" -fx-effect: dropshadow(gaussian, #000000, 20, 0, 8, 2);");

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
                    cellPane.setStyle("-fx-background-color: transparent; -fx-background-image: url('tiles/"
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
        Button resetTurnButton = new Button("⟲"); // \u2B6F

        tileButtons = gameViewModel.getButtonTiles();
        for (Button tileButton : tileButtons) {
            tileButton.setPrefSize(45, 45);
            tileButton.setStyle("-fx-background-color: transparent; -fx-background-image: url('tiles/"
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
        resetTurnButton.getStyleClass().add("grey-button");
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
        passTurnButton.getStyleClass().add("yellow-button");
        passTurnButton.setOnAction(event -> {
            // Call the method to handle the "Pass Turn" action
            gameViewModel.skipTurn();
        });

        Button challengeButton = new Button("Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setDisable(true);
        challengeButton.setOnAction(event -> {
            // Call the method to handle the "Challenge" action
            gameViewModel.challenge();
        });

        if (!gameViewModel.isMyTurn()) {
            challengeButton.setDisable(true);
            passTurnButton.setDisable(true);
        }

        Button quitGameButton = new Button("Quit Game");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setOnAction(event -> {
            // Call the method to handle the "Quit Game" action
            gameViewModel.quitGame();
            // Get the Stage object of the window you want to close
            Stage stage = (Stage) primaryStage.getScene().getWindow();

            // Call the close() method to close the window
            stage.close();
        });

        tryPlaceWordButton = new Button("Try Place Word");
        tryPlaceWordButton.getStyleClass().add("blue-button");
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
