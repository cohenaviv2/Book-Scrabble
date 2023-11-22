package app.view;

import java.io.*;
import java.net.*;
import java.util.*;

import app.model.game.Tile;
import app.model.game.Word;
import app.model.host.HostModel;
import app.view_model.GameViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/* GameView creates the visual components of the game using JavaFX, managing the game board, player details,
* and other interactive elements for a user-friendly interface.
* 
* @author: Aviv Cohen
*
*/

public class GameView {
    // View Model & View Controller
    private GameViewModel gameViewModel;
    private GameController gameController;
    // Utils
    private Map<String, String> descriptions;
    private Map<String, String> symbols;
    private Map<String, Image> icons;
    public final String styleSheet;
    public Timeline timer;
    // JavaFX
    private HBox osBar;
    private GridPane gameBoard;
    private List<Button> tileButtons;
    private Button tryPlaceWordButton;
    private Button challengeButton;
    private Button passTurnButton;
    private Button resetTilesButton;
    private Button sortTilesButton;
    private HBox scoreBox;
    private ProgressIndicator progressIndicator;
    // Properties
    public String myName;
    private boolean isHost;
    private int customPort = 0;
    private boolean isTilesSorted;
    private boolean isTilePlaced;

    public GameView(GameViewModel viewModel, GameController gameController) {
        this.osBar = null;
        this.gameViewModel = viewModel;
        this.gameController = gameController;
        this.myName = null;
        // Initialize and set up the initial UI components here.
        this.styleSheet = getClass().getResource("/style.css").toExternalForm();
        this.icons = new HashMap<>();
        icons.put("game", new Image("icons/game-icon.png"));
        icons.put("turn", new Image("icons/turn-icon.png"));
        icons.put("star", new Image("icons/star-icon.png"));
        icons.put("bag", new Image("icons/bag-icon.png"));
        icons.put("trophy", new Image("icons/trophy-icon.png"));
        icons.put("draw", new Image("icons/draw-icon2.png"));
        this.symbols = new HashMap<>();
        symbols.put("exit", "\uD83D\uDDD9");
        symbols.put("minimize", "\uD83D\uDDD5");
        symbols.put("settings", "\uD83D\uDD27");
        symbols.put("help", "❓");
        symbols.put("messages", "\uD83D\uDCE8");
        symbols.put("back", "\u2B05");
        symbols.put("left", "⮜");
        symbols.put("right", "⮞");
        symbols.put("up", "🡅");
        symbols.put("down", "🡇");
        symbols.put("copy", "📋");
        symbols.put("sort", "⭰");
        symbols.put("reset", "\u2B6F"); // \u2B6F ⟲ ⭯
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(25, 25);
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(false);
        this.descriptions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader("src\\main\\resources\\instructions\\descriptions.txt"))) {
            String line;
            String currentTitle = null;
            StringBuilder explanationBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    if (currentTitle != null) {
                        descriptions.put(currentTitle, explanationBuilder.toString());
                    }

                    String[] parts = line.split("=", 2);
                    currentTitle = parts[0].trim();
                    explanationBuilder = new StringBuilder(parts[1].trim());
                } else {
                    explanationBuilder.append("\n").append(line.trim());
                }
            }

            if (currentTitle != null) {
                descriptions.put(currentTitle, explanationBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HBox createOsBar(Stage currentStage, Boolean gameIsRunning) {
        osBar = new HBox();
        osBar.setSpacing(12);
        osBar.setPadding(new Insets(15, 0, 0, 15));

        // Exit Button
        Button exitButton = new Button(symbols.get("exit"));
        exitButton.getStyleClass().add("red-button");
        exitButton.setOnAction(event -> {
            if (gameIsRunning) {
                gameController.showQuitGameWindow();
            } else {
                currentStage.close();
                System.exit(0);
            }
        });

        // Minimize Button
        Button minimizeButton = new Button(symbols.get("minimize"));
        minimizeButton.getStyleClass().add("green-button");
        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });

        // OS Bar interraction
        osBar.getChildren().addAll(exitButton, minimizeButton);
        osBar.setOnMousePressed(e -> gameController.handleMousePressed(e));
        osBar.setOnMouseDragged(e -> gameController.handleMouseDragged(e));

        return osBar;
    }

    public VBox createInitialBox() {
        VBox gameModeBox = new VBox(10);
        gameModeBox.setAlignment(Pos.CENTER);

        // Styled header label
        Text headerLabel = new Text("Book Scrabble");
        headerLabel.getStyleClass().add("book-scrabble-header");

        ImageView logoImage = new ImageView(new Image("backgrounds/logo-main.png"));
        // logoImage.setFitWidth(610);
        // logoImage.setFitHeight(270);
        HBox headerBox = new HBox(logoImage);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, -30, 0));

        // Game mode label
        Label modeLabel = new Label("Choose Game Mode");
        modeLabel.getStyleClass().add("mode-label");

        // Host and Guest Button pane
        HBox buttonPane = new HBox(20);
        buttonPane.setAlignment(Pos.CENTER);

        // Host Button
        Button hostButton = new Button("Host a Game");
        hostButton.getStyleClass().add("blue-button");
        hostButton.setStyle("-fx-font-size: 24px;");
        hostButton.setMinSize(200, 80);
        hostButton.setMaxSize(200, 80);

        hostButton.setOnAction(event -> {
            this.isHost = true;
            this.gameViewModel.initialize(isHost);
            gameController.gameLoginStage.close();
            gameController.showLoginWindow(isHost);
        });

        // Guest Button
        Button guestButton = new Button("Join as a Guest");
        guestButton.getStyleClass().add("darkgreen-button");
        guestButton.setStyle("-fx-font-size: 24px;");
        guestButton.setMinSize(200, 80);
        guestButton.setMaxSize(200, 80);

        guestButton.setOnAction(event -> {
            this.isHost = false;
            this.gameViewModel.initialize(isHost);
            gameController.gameLoginStage.close();
            gameController.showLoginWindow(isHost);
        });

        buttonPane.getChildren().addAll(hostButton, guestButton);
        buttonPane.setPadding(new Insets(0, 0, 10, 0));

        // Help Button
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");

        Text hostModeTitle = new Text("Host a Game");
        hostModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text hostModeText = new Text(descriptions.get("game-mode-host"));
        hostModeText.getStyleClass().add("content-label");
        hostModeText.setTextAlignment(TextAlignment.CENTER);
        Text guestModeTitle = new Text("Join as a Guest");
        guestModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text guestModeText = new Text(descriptions.get("game-mode-guest"));
        guestModeText.getStyleClass().add("content-label");
        guestModeText.setTextAlignment(TextAlignment.CENTER);

        VBox centerContent = new VBox(10, hostModeTitle, hostModeText, guestModeTitle, guestModeText);
        centerContent.setAlignment(Pos.CENTER);

        Node[] helpBoxNodes = createCustomBox(centerContent, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpBoxNodes[0];
        helpButton.setOnAction(e -> {
            gameController.showCustomWindow(helpBox, 720, 500);
        });

        gameModeBox.getChildren().addAll(headerBox, modeLabel, buttonPane, helpButton);

        return gameModeBox;
    }

    public VBox createLoginBox() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setSpacing(15);

        if (isHost) {
            gameController.checkHostConnection(true);
        }

        List<TextField> textFields = new ArrayList<>();
        // Waiting Box
        VBox waitingBox = createWaitingBox("Preparing the game...\nWaiting for all players to connect...");
        Node[] nodes = createCustomBox(waitingBox, "red", "Quit Game", "alert");
        VBox fullWaitingBox = (VBox) nodes[0];
        Button quitGameButton = (Button) nodes[1];
        quitGameButton.setOnAction(e -> {
            gameViewModel.quitGame();
            gameController.close();
            System.exit(0);
        });

        /* Check for connection */

        // My name
        Label nameLabel = new Label("My name");
        nameLabel.getStyleClass().add("login-label");
        String tempName = isHost ? "Daniel" : "Anthony";
        // myName != null ? myName : ""
        TextField nameTextField = new TextField(myName != null ? myName : "");
        int maxCharacters = 11; // Maximum characters in the name field
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= maxCharacters) {
                return change;
            } else {
                return null;
            }
        });
        nameTextField.setTextFormatter(textFormatter);
        nameTextField.getStyleClass().add("text-field");
        nameTextField.setAlignment(Pos.CENTER);
        nameTextField.setMaxWidth(200);
        nameTextField
                .setOnMouseClicked(e -> {
                    if (nameTextField.getStyleClass().contains("name-error-field")) {
                        nameTextField.setText("");
                    }
                    nameTextField.getStyleClass()
                            .removeIf(s -> s.equals("error-field") || s.equals("name-error-field"));
                });
        textFields.add(nameTextField);
        VBox nameTextFieldBox = new VBox(nameTextField);
        nameTextFieldBox.setMinHeight(50);
        nameTextFieldBox.setAlignment(Pos.CENTER);
        loginFormBox.getChildren().addAll(nameLabel, nameTextFieldBox);

        // Select books
        Label selectBookLabel = new Label("My Books");
        selectBookLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setMinWidth(120);
        booksButton.setMinHeight(40);
        booksButton.setOnAction(e -> {
            booksButton.getStyleClass().remove("error-field");
            gameController.showBookSelectionWindow(true);
        });
        VBox booksButtonBox = new VBox(booksButton);
        booksButtonBox.setMinHeight(50);
        booksButtonBox.setAlignment(Pos.CENTER);
        loginFormBox.getChildren().addAll(selectBookLabel, booksButtonBox);

        // Round buttons pane
        HBox roundButtonsPane = createRoundButtons(false);
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        String mode = isHost ? "Play as a Host" : "Play as a Guest";
        Text modeTitle = new Text(mode);
        modeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        String descMode = isHost ? "host" : "guest";
        Text modeText = new Text(descriptions.get("mode-" + descMode));
        modeText.getStyleClass().add("content-label");
        modeText.setTextAlignment(TextAlignment.CENTER);
        VBox modeExp = new VBox(10, modeTitle, modeText);
        modeExp.setAlignment(Pos.CENTER);
        Node[] helpNodes = createCustomBox(modeExp, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpNodes[0];
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 600, 580));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        HBox settings = createSettingBox();
        Node[] settingBoxNodes = createCustomBox(settings, "red", symbols.get("exit"), "settings");
        VBox settingsBox = (VBox) settingBoxNodes[0];
        settingsButton.setOnAction(e -> gameController.showCustomWindow(settingsBox, 600, 500));

        // ** HOST **
        if (isHost) {
            // Number of players
            Label numOfPlayersLabel = new Label("Number of Players");
            numOfPlayersLabel.getStyleClass().add("login-label");
            ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
            // numOfPlayersComboBox.setValue(2);
            numOfPlayersComboBox.getItems().addAll(2, 3, 4);
            numOfPlayersComboBox.getStyleClass().add("text-field");
            numOfPlayersComboBox.setMinWidth(105);
            numOfPlayersComboBox.setOnAction(e -> numOfPlayersComboBox.getStyleClass().remove("error-field"));
            VBox numOfPlayersBox = new VBox(numOfPlayersComboBox);
            numOfPlayersBox.setMinHeight(50);
            numOfPlayersBox.setAlignment(Pos.CENTER);
            loginFormBox.getChildren().addAll(numOfPlayersLabel, numOfPlayersBox);

            loginFormBox.getChildren().add(roundButtonsPane);

            // Submit Buttons
            HBox submitButtons = createSubmitButtons();
            Button startGameButton = (Button) submitButtons.getChildren().get(1);
            startGameButton.setStyle("-fx-font-size: 22px;");
            loginFormBox.getChildren().add(submitButtons);

            startGameButton.setOnAction(event -> {
                boolean isEmpty = false;
                myName = nameTextField.getText();
                if (!myName.equals("") && !gameController.isName(myName)) {
                    nameTextField.getStyleClass().add("name-error-field");
                    isEmpty = true;
                }
                for (TextField tf : textFields) {
                    if (tf.getText().equals("")) {
                        isEmpty = true;
                        tf.getStyleClass().add("error-field");
                    }
                }
                if (numOfPlayersComboBox.getValue() == null) {
                    isEmpty = true;
                    numOfPlayersComboBox.getStyleClass().add("error-field");
                }
                if (!isEmpty) {
                    if (gameController.getSelectedBooks().size() == 0) {
                        booksButton.getStyleClass().add("error-field");
                    } else {
                        // Check Host Server Connection In The Background
                        gameController.checkHostConnection(false);

                        startGameButton.setDisable(true);
                        gameViewModel.setTotalPlayersCount(numOfPlayersComboBox.getValue());
                        gameViewModel.connectMe(myName, "0", customPort);
                        gameViewModel.myBooksChoice(gameController.getSelectedBooks());
                        gameViewModel.ready();

                        loginFormBox.setDisable(true);
                        osBar.setDisable(true);

                        gameController.showCustomWindow(fullWaitingBox, 500, 350);
                        // Set up a timer for 10 seconds
                        setupTimer();
                    }
                }
            });

            // ** GUEST **
        } else {
            // Host's ip
            Label ipLabel = new Label("Host IP");
            ipLabel.getStyleClass().add("login-label");
            TextField ipTextField = new TextField();
            ipTextField.setMaxWidth(250);
            // ipTextField.setText("localhost");
            ipTextField.setAlignment(Pos.CENTER);
            ipTextField.getStyleClass().add("text-field");
            ipTextField.setOnMouseClicked(e -> ipTextField.getStyleClass().remove("error-field"));
            textFields.add(ipTextField);
            VBox ipTextFieldBox = new VBox(ipTextField);
            ipTextFieldBox.setMinHeight(50);
            ipTextFieldBox.setAlignment(Pos.CENTER);
            loginFormBox.getChildren().addAll(ipLabel, ipTextFieldBox);

            loginFormBox.getChildren().add(roundButtonsPane);

            // Submit Buttons
            HBox submitButtons = createSubmitButtons();
            Button joinGameButton = (Button) submitButtons.getChildren().get(1);
            joinGameButton.setStyle("-fx-font-size: 22px;");
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(20, 20);
            progressIndicator.getStyleClass().add("progress-indicator");
            submitButtons.getChildren().add(progressIndicator);
            progressIndicator.setVisible(false);
            loginFormBox.getChildren().add(submitButtons);

            joinGameButton.setOnAction(event -> {
                boolean isEmpty = false;
                myName = nameTextField.getText();
                String ip = ipTextField.getText();

                if (!myName.equals("") && !gameController.isName(myName)) {
                    nameTextField.getStyleClass().add("name-error-field");
                    isEmpty = true;
                }
                for (TextField tf : textFields) {
                    if (tf.getText().equals("")) {
                        isEmpty = true;
                        tf.getStyleClass().add("error-field");
                    }
                }
                if (!isEmpty) {
                    if (gameController.getSelectedBooks().size() == 0) {
                        booksButton.getStyleClass().add("error-field");
                    } else {
                        joinGameButton.setDisable(true);
                        progressIndicator.setVisible(true);

                        // Move network operations to a background thread
                        Task<Boolean> networkTask = new Task<Boolean>() {
                            @Override
                            protected Boolean call() throws Exception {
                                if (customPort == 0) {
                                    customPort = HostModel.HOST_SERVER_PORT;
                                }
                                gameViewModel.connectMe(myName, ip, customPort);
                                boolean isConnected = gameViewModel.isConnected();

                                return isConnected;
                            }
                        };

                        networkTask.setOnSucceeded(e -> {
                            boolean isConnected = networkTask.getValue();

                            if (!isConnected) {
                                loginFormBox.setDisable(true);
                                progressIndicator.setVisible(false);
                                // Show a network error alert
                                VBox networkAlert = createTextAlertBox("Network Error",
                                        descriptions.get("guest-socket-error"),
                                        true);

                                Node[] p = createCustomBox(networkAlert, "red", "Exit Game", "alert");
                                VBox networkAlertBox = (VBox) p[0];
                                Button alertButton = (Button) p[1];
                                alertButton.setOnAction(ev -> {
                                    gameViewModel.quitGame();
                                    gameController.close();
                                    System.exit(0);
                                });

                                gameController.showCustomWindow(networkAlertBox, 550, 350);
                            } else {
                                gameViewModel.myBooksChoice(gameController.getSelectedBooks());
                                gameViewModel.ready();

                                // loginFormBox.setDisable(true);
                                // osBar.setDisable(true);

                                // Waiting Window
                                progressIndicator.setVisible(false);
                                gameController.showCustomWindow(fullWaitingBox, 500, 350);
                                // Set up a timer for 10 seconds
                                setupTimer();
                            }
                        });

                        // Start the background task
                        new Thread(networkTask).start();
                    }

                }

            });
        }

        return loginFormBox;
    }

    public VBox createBookSelectionBox(boolean fullBookList) {
        VBox rootContainer = new VBox(10);
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
        // Set preferred width and height to maintain a fixed size
        bookContainer.setPrefWidth(800);
        bookContainer.setPrefHeight(650);

        ObservableList<String> bookList = fullBookList ? GameViewModel.getBookList()
                : FXCollections.observableArrayList(gameViewModel.gameBooksProperty());

        for (String book : bookList) {
            VBox bookBox = new VBox(10);
            bookBox.setAlignment(Pos.CENTER);

            ImageView imageView = new ImageView();
            imageView.setImage(new Image("books_images/" + book + ".jpg"));
            imageView.setFitWidth(120); // Adjust the width of the image here
            imageView.setPreserveRatio(true);

            String firstLine = "";
            String secondLine = "";

            if (book.length() > 18) {
                int splitIndex = book.substring(0, 18).lastIndexOf(" ");
                if (splitIndex != -1) {
                    firstLine = book.substring(0, splitIndex);
                    secondLine = book.substring(splitIndex + 1);
                } else {
                    firstLine = book.substring(0, 18);
                    secondLine = book.substring(18);
                }
            } else {
                firstLine = book;
            }

            TextFlow textFlow = new TextFlow();

            Text firstLineText = new Text(firstLine);
            Text secondLineText = new Text(secondLine);
            firstLineText.setFill(Color.WHITE);
            secondLineText.setFill(Color.WHITE);
            secondLineText.setTextAlignment(TextAlignment.CENTER);

            textFlow.getChildren().addAll(firstLineText, new Text("\n"), secondLineText);

            textFlow.getStyleClass().add("book-label");
            textFlow.setTextAlignment(TextAlignment.CENTER);

            bookBox.getChildren().addAll(imageView, textFlow);

            if (fullBookList) {
                if (gameController.getSelectedBooks().contains(book)) {
                    imageView.getStyleClass().add("selected-book-image");
                }
                bookBox.setOnMouseClicked(event -> {
                    if (gameController.getSelectedBooks().contains(book)) {
                        gameController.getSelectedBooks().remove(book);
                        imageView.getStyleClass().remove("selected-book-image");
                    } else {
                        gameController.getSelectedBooks().add(book);
                        imageView.getStyleClass().add("selected-book-image");
                    }
                });
            }

            bookContainer.getChildren().add(bookBox);
        }

        scrollPane.setContent(bookContainer);

        HBox buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);

        String buttonText = fullBookList ? "Done" : "Got it!";
        Button submitButton = new Button(buttonText);
        submitButton.getStyleClass().add("green-button");
        submitButton.setStyle("-fx-font-size: 26px;");
        submitButton.setPrefHeight(60);
        submitButton.setPrefWidth(120);
        submitButton.setOnAction(event -> gameController.customStage.close());

        buttonPane.getChildren().add(submitButton);
        buttonPane.setPadding(new Insets(0, 0, 0, 0));

        rootContainer.getChildren().addAll(scrollPane, buttonPane);

        return rootContainer;
    }

    private HBox createRoundButtons(boolean isGameFlow) {
        HBox roundButtonsPane = new HBox(12);
        roundButtonsPane.setAlignment(Pos.CENTER);
        roundButtonsPane.setPadding(new Insets(30, 0, 0, 0));
        roundButtonsPane.setMinHeight(70);
        roundButtonsPane.setPrefHeight(70);

        // Settings/Messages
        String symbol = isGameFlow ? symbols.get("messages") : symbols.get("settings");
        String color = isGameFlow ? "gold" : "grey";
        Button settingButton = new Button(symbol);
        settingButton.getStyleClass().add(color + "-button");

        // Help
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");

        roundButtonsPane.getChildren().addAll(helpButton, settingButton);

        return roundButtonsPane;
    }

    private HBox createSubmitButtons() {
        // Submit button
        Button submitButton = new Button(isHost ? "Start Game" : "Join Game");
        submitButton.getStyleClass().add("blue-button");
        submitButton.setPrefWidth(160);
        submitButton.setMinHeight(55);

        // Go Back button
        Button goBackButton = new Button(symbols.get("back"));
        goBackButton.getStyleClass().add("green-button");
        goBackButton.setOnAction(e -> {
            gameController.close();
            gameController.gameLoginStage.close();
            gameController.showInitialWindow();
            isHost = false;
        });

        HBox submitButtons = new HBox(10, goBackButton, submitButton);
        submitButtons.setPadding(new Insets(30, 0, 10, 0));
        submitButtons.setAlignment(Pos.CENTER);

        return submitButtons;
    }

    protected VBox createQuitBox() {
        VBox quitAlertBox = createTextAlertBox("Quit Game", "Are you sure you want to quit game?", true);

        Button yesButton = new Button("Yes");
        yesButton.getStyleClass().add("blue-button");
        yesButton.setOnAction(event -> {
            gameController.closeCustomWindow();

            if (isHost && gameController.isGameRunning()) {
                Platform.runLater(() -> {
                    VBox waitingBox = createWaitingBox("Waiting for all players to disconnect...");
                    Node[] boxNodes = createCustomBox(waitingBox, null, null, "alert");
                    VBox waitingAlertBox = (VBox) boxNodes[0];
                    gameController.showCustomWindow(waitingAlertBox, 500, 350);
                    gameViewModel.quitGame();
                });
            } else {
                gameViewModel.quitGame();
                gameController.close();
                System.exit(0);
            }

        });

        Button noButton = new Button("No");
        noButton.getStyleClass().add("red-button");
        noButton.setOnAction(event -> gameController.closeCustomWindow());

        HBox buttons = new HBox(13, yesButton, noButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(30, 0, 10, 0));

        quitAlertBox.getChildren().addAll(buttons);

        Node[] quitGameBox = createCustomBox(quitAlertBox, null, null, "alert");

        return (VBox) quitGameBox[0];
    }

    private Node[] createCustomBox(Node content, String btnColor, String btnText, String theme) {
        VBox customRoot = new VBox(20);
        customRoot.setAlignment(Pos.CENTER);
        customRoot.getStyleClass().add(theme + "-content-background");

        customRoot.getChildren().add(content);

        Button button = null;

        if (btnColor != null) {
            button = new Button(btnText);
            button.getStyleClass().add(btnColor + "-button");
            button.setOnAction(event -> {
                gameController.closeCustomWindow();
            });

            StackPane buttonPane = new StackPane(button);
            BorderPane.setAlignment(buttonPane, Pos.TOP_CENTER);
            buttonPane.setPadding(new Insets(10, 0, 0, 0)); // Add padding to the top

            customRoot.getChildren().add(buttonPane);
        }

        Node[] nodes = new Node[2];
        nodes[0] = customRoot;
        nodes[1] = button;

        return nodes;
    }

    private VBox createWaitingBox(String text) {
        Text waitingText = new Text(text);
        waitingText.getStyleClass().add("content-label");
        waitingText.setTextAlignment(TextAlignment.CENTER);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.getStyleClass().add("progress-indicator");
        VBox waitingBox = new VBox(10, waitingText, progressIndicator);
        waitingBox.setAlignment(Pos.CENTER);

        return waitingBox;
    }

    private void setupTimer() {
        double waitingMin = isHost ? 3 : 2;
        timer = new Timeline(new KeyFrame(Duration.minutes(waitingMin), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Check whether the game is running
                if (!gameController.isGameRunning()) {
                    // Show 'New Game' window
                    gameViewModel.quitGame();
                    VBox newGameBox = createNewGameBox("Game Start Timeout", descriptions.get("new-game-window"));
                    gameController.showCustomWindow(newGameBox, 550, 300);
                }

                // Stop the timer
                timer.stop();
            }
        }));
        timer.setCycleCount(1); // Run only once
        timer.play();
    }

    public VBox createTextAlertBox(String title, String text, boolean isTextCenter) {
        Text alertTitle = new Text(title);
        alertTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text alertErrorText = new Text(text);
        alertErrorText.getStyleClass().add("content-label");
        if (isTextCenter)
            alertErrorText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(20, alertTitle, alertErrorText);
        alertBox.setAlignment(Pos.CENTER);

        return alertBox;
    }

    public VBox createTurnAlertBox() {
        VBox turnAlert = createTextAlertBox("It's Not Your Turn", "Wait for your turn to play", true);
        Node[] turnAlertNodes = createCustomBox(turnAlert, "blue", "OK", "alert");
        VBox turnAlertBox = (VBox) turnAlertNodes[0];
        return turnAlertBox;
    }

    public VBox createClosableAlertBox(String title, String text, boolean isTextCenter) {
        Text alertTitle = new Text(title);
        alertTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text alertErrorText = new Text(text);
        alertErrorText.getStyleClass().add("content-label");
        if (isTextCenter)
            alertErrorText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(20, alertTitle, alertErrorText);
        alertBox.setAlignment(Pos.CENTER);

        Node[] nodes = createCustomBox(alertBox, "blue", "OK", "alert");

        return (VBox) nodes[0];
    }

    public VBox createQuitAlertBox(String title, String text, boolean isTextCenter) {
        Text alertTitle = new Text(title);
        alertTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text alertErrorText = new Text(text);
        alertErrorText.getStyleClass().add("content-label");
        if (isTextCenter)
            alertErrorText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(20, alertTitle, alertErrorText);
        alertBox.setAlignment(Pos.CENTER);

        Node[] nodes = createCustomBox(alertBox, "red", "Quit Game", "alert");
        Button quitButton = (Button) nodes[1];
        quitButton.setOnAction(e -> {
            gameViewModel.quitGame();
            gameController.close();
            System.exit(0);
        });

        return (VBox) nodes[0];
    }

    private HBox createSettingBox() {
        HBox settings = null;

        if (isHost) {
            // Game server connection check
            Text gameServerTitle = new Text("Game Server Connection Test");
            gameServerTitle.setTextAlignment(TextAlignment.CENTER);
            gameServerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            Text contentLabel = new Text(
                    "The game server is responsible for checking whether\na word is legal in terms of the book dictionary.\nGame Server is uploaded and\npowered by Oracle Cloud\nUsing Ubuntu 22.04 VM");
            contentLabel.setFont(new Font(18));
            contentLabel.setTextAlignment(TextAlignment.CENTER);
            Button connectionButton = new Button("Check Connection");
            connectionButton.getStyleClass().add("green-button");
            VBox connectionField = new VBox();
            connectionField.setAlignment(Pos.CENTER);
            connectionField.setMinHeight(25);

            Task<Boolean> gameServerConnectionTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return gameViewModel.isGameServerConnect();
                }
            };
            gameServerConnectionTask.setOnSucceeded(e -> {
                Text connectionText = new Text("");
                connectionText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                connectionText.setTextAlignment(TextAlignment.CENTER);
                boolean isConnected = gameServerConnectionTask.getValue();
                connectionField.getChildren().clear();
                if (isConnected) {
                    connectionText.setFill(Color.GREEN);
                    connectionText.setText("Connected");
                } else {
                    connectionText.setFill(Color.RED);
                    connectionText.setText("Not Connected");
                }
                connectionField.getChildren().add(connectionText);
            });
            connectionButton.setOnAction(e -> {
                connectionButton.setDisable(true);
                connectionField.getChildren().add(progressIndicator);
                new Thread(gameServerConnectionTask).start();
            });

            VBox gameServerSettings = new VBox(10, gameServerTitle, contentLabel, connectionButton, connectionField);
            gameServerSettings.setAlignment(Pos.CENTER);

            // Custom port
            Text customPortTitle = new Text("Set Custom Port");
            customPortTitle.setTextAlignment(TextAlignment.CENTER);
            customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            Text customPortLabel = new Text(
                    "Host server runs by default on port 8040\n You can choose a custom port:");
            customPortLabel.setFont(new Font(18));
            customPortLabel.setTextAlignment(TextAlignment.CENTER);
            TextField portField = new TextField("");
            portField.getStyleClass().add("text-field");
            portField.setAlignment(Pos.CENTER);
            portField.setMaxWidth(120);
            Text invalidPortTxt = new Text("");
            invalidPortTxt.setTextAlignment(TextAlignment.CENTER);
            invalidPortTxt.setFont(Font.font("Arial", 18));
            Button setPortButton = new Button("Set port");
            setPortButton.getStyleClass().add("blue-button");
            portField.setOnMouseClicked(e -> invalidPortTxt.setText(""));
            setPortButton.setOnAction(e -> {
                String portText = portField.getText();
                if (gameViewModel.isValidPort(portText)) {
                    this.customPort = Integer.parseInt(portText);
                    // invalidPortTxt.setText(""); // Clear error message if valid
                    portField.setDisable(true);
                    setPortButton.setDisable(true);
                    invalidPortTxt.setText("Port is set!");
                    invalidPortTxt.setFill(Color.BLUE);
                } else {
                    invalidPortTxt.setText(
                            "Please enter a valid port number\nThe port should be a number with a maximum 5 digits");
                    invalidPortTxt.setFill(Color.CRIMSON);
                }
            });

            VBox customPortSettings = new VBox(10, customPortTitle, customPortLabel, portField, invalidPortTxt,
                    setPortButton);
            customPortSettings.setAlignment(Pos.CENTER);

            // Whats my IP
            Text myIpTitle = new Text("Share Your IP with Friends");
            myIpTitle.setTextAlignment(TextAlignment.CENTER);
            myIpTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            TextField myIpField = new TextField("");
            myIpField.setDisable(true);
            myIpField.getStyleClass().add("text-field");
            myIpField.setAlignment(Pos.CENTER);
            myIpField.setMaxWidth(230);

            // Create a "Copy" button
            Button copyButton = new Button(symbols.get("copy"));
            copyButton.getStyleClass().add("black-button");
            copyButton.setDisable(true);

            copyButton.setOnAction(e -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(myIpField.getText()); // Copy the text from the TextField
                clipboard.setContent(content);
            });

            HBox myIpBox = new HBox(10, myIpField, copyButton);
            myIpBox.setAlignment(Pos.CENTER);
            myIpBox.setPadding(new Insets(0, 0, 0, 0));

            Button myIpButton = new Button("What's My IP");
            myIpButton.getStyleClass().add("yellow-button");
            myIpButton.setStyle("-fx-font-size: 24px;");

            CheckBox localIpCheckBox = new CheckBox("We're playing on the same network");
            localIpCheckBox.setTextFill(Color.LIGHTGOLDENRODYELLOW);
            localIpCheckBox.setStyle("-fx-font-size: 18px;");
            localIpCheckBox.setSelected(true);
            localIpCheckBox.setOnAction(e -> {
                myIpField.setText("");
                myIpButton.setDisable(false);
                copyButton.setDisable(true);
                myIpField.setDisable(true);
            });

            myIpButton.setOnAction(e -> {
                myIpButton.setDisable(true);
                myIpField.setDisable(false);
                copyButton.setDisable(false);
                if (localIpCheckBox.isSelected()) {
                    try {
                        myIpField.setText(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // Use global IP retrieval logic here
                    myIpField.setText(gameViewModel.getPublicIpAddress());
                }
            });

            VBox myIpSettings = new VBox(25, myIpTitle, localIpCheckBox, myIpBox, myIpButton);
            myIpSettings.setAlignment(Pos.CENTER);

            // settings = new VBox(5, gameServerSettings, customPortSettings, myIpSettings);
            VBox[] switchList = { myIpSettings, customPortSettings, gameServerSettings };
            settings = createSwitchableHBox(switchList);

        } else {
            // Custom port
            Text customPortTitle = new Text("Set Custom Port");
            customPortTitle.setTextAlignment(TextAlignment.CENTER);
            customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            Text customPortLabel = new Text(descriptions.get("custom-port"));
            customPortLabel.getStyleClass().add("content-label");
            customPortLabel.setTextAlignment(TextAlignment.CENTER);
            TextField portField = new TextField("");
            portField.getStyleClass().add("text-field");
            portField.setAlignment(Pos.CENTER);
            portField.setMaxWidth(120);
            Text invalidPortTxt = new Text("");
            invalidPortTxt.setTextAlignment(TextAlignment.CENTER);
            invalidPortTxt.setFont(Font.font("Arial", 18));
            Button setPortButton = new Button("Set port");
            setPortButton.getStyleClass().add("blue-button");
            portField.setOnMouseClicked(e -> invalidPortTxt.setText(""));
            setPortButton.setOnAction(e -> {
                String portText = portField.getText();
                if (gameViewModel.isValidPort(portText)) {
                    this.customPort = Integer.parseInt(portText);
                    // invalidPortTxt.setText(""); // Clear error message if valid
                    portField.setDisable(true);
                    setPortButton.setDisable(true);
                    invalidPortTxt.setText("Port is set!");
                    invalidPortTxt.setFill(Color.BLUE);
                } else {
                    invalidPortTxt.setText(
                            "Please enter a valid port number.\nThe port should be a number with a maximum 5 digits");
                    invalidPortTxt.setFill(Color.BROWN);
                }
            });

            VBox box = new VBox(10, customPortTitle, customPortLabel, portField, invalidPortTxt, setPortButton);
            box.setAlignment(Pos.CENTER);
            settings = new HBox(box);

        }
        settings.setAlignment(Pos.CENTER);
        return settings;
    }

    public VBox createHostNetworkBox(boolean isGameServerError) {
        String error = "";
        if (isGameServerError) {
            error = descriptions.get("game-server-error");
        } else {
            error = descriptions.get("host-server-error");
        }

        VBox hostNetworkAlertBox = createQuitAlertBox("Network Error", error, true);

        return hostNetworkAlertBox;
    }

    public BorderPane createGameFlowBox() {
        BorderPane gameFlowBox = new BorderPane();

        // Create the sidebar
        VBox sidebar = createSidebar();
        gameFlowBox.setRight(sidebar);

        // Create the game board
        GridPane gameBoard = createGameBoard();
        HBox gameBoardContainer = new HBox(gameBoard);
        gameBoardContainer.setPadding(new Insets(0, 0, 0, 60));
        gameFlowBox.setCenter(gameBoardContainer);

        // Create the buttons
        VBox buttons = createButtons();
        gameFlowBox.setLeft(buttons);

        return gameFlowBox;
    }

    private VBox createSidebar() {

        VBox sideBar = new VBox(15);
        sideBar.setPrefWidth(375);
        sideBar.setAlignment(Pos.CENTER);

        // My Info Board
        Pane myInfoBoard = createPlayerInfoBoard(gameViewModel.myNameProperty().get(), null, false);
        sideBar.getChildren().add(myInfoBoard);

        // Other Player's Info Board
        VBox otherBox = new VBox(15);
        otherBox.setAlignment(Pos.CENTER);
        otherBox.setPadding(new Insets(20, 0, 20, 0));

        List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(gameViewModel.othersInfoProperty().entrySet());
        sortedEntries.sort((entry1, entry2) -> {
            String[] info1 = entry1.getValue().split(":");
            String[] info2 = entry2.getValue().split(":");
            int score1 = Integer.parseInt(info1[0]);
            int score2 = Integer.parseInt(info2[0]);

            return Integer.compare(score2, score1);
        });
        for (Map.Entry<String, String> entry : sortedEntries) {
            String playerName = entry.getKey();
            String playerInfo = entry.getValue();
            Pane otherInfoBoard = createPlayerInfoBoard(playerName, playerInfo, true);
            otherBox.getChildren().add(otherInfoBoard);
        }
        sideBar.getChildren().add(otherBox);

        gameViewModel.othersInfoProperty().addListener((MapChangeListener<String, String>) change -> {
            Platform.runLater(() -> {
                otherBox.getChildren().clear();
                List<Map.Entry<String, String>> updatedSortedEntries = new ArrayList<>(
                        gameViewModel.othersInfoProperty().entrySet());
                updatedSortedEntries.sort((entry1, entry2) -> {
                    String[] info1 = entry1.getValue().split(":");
                    String[] info2 = entry2.getValue().split(":");
                    int score1 = Integer.parseInt(info1[0]);
                    int score2 = Integer.parseInt(info2[0]);
                    return Integer.compare(score2, score1);
                });

                for (Map.Entry<String, String> entry : updatedSortedEntries) {
                    String playerName = entry.getKey();
                    String playerInfo = entry.getValue();
                    Pane otherInfoBoard = createPlayerInfoBoard(playerName, playerInfo, true);
                    otherBox.getChildren().add(otherInfoBoard);
                }
            });
        });

        // My Words
        VBox myWordsBox = new VBox();
        myWordsBox.setMinSize(280, 165);
        myWordsBox.setMaxSize(280, 165);
        myWordsBox.getStyleClass().add("my-words-box");

        ListView<String> wordsListView = new ListView<>();
        wordsListView.itemsProperty().bind(gameViewModel.myWordsProperty());
        wordsListView.setId("wordsListView");
        wordsListView.setMinSize(280, 160);
        wordsListView.setMaxSize(280, 160);
        wordsListView.setPrefSize(220, 100);
        Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
        wordsListView.setBackground(transparentBackground);

        Label wordsLabel = new Label("My Words");
        wordsLabel.setAlignment(Pos.CENTER);
        wordsLabel.getStyleClass().add("mode-label");

        Pane wordsListBox = new Pane(wordsListView);
        wordsListBox.getStyleClass().add("my-words-box");
        myWordsBox.getChildren().add(wordsListView);

        VBox wordsBox = new VBox(wordsLabel, myWordsBox);
        wordsBox.setAlignment(Pos.CENTER);
        wordsBox.setPadding(new Insets(20, 0, 0, 0));
        sideBar.getChildren().addAll(wordsBox);

        wordsListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setAlignment(Pos.CENTER);
                    setText(item);
                    setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
                    setTextFill(Color.WHITE); // Change the color to your desired color
                } else {
                    setText(null);
                }
            }
        });

        // Bag count
        Label bagValueLabel = new Label();
        bagValueLabel.textProperty().bind(gameViewModel.bagCountProperty().asString());
        bagValueLabel.getStyleClass().add("bag-label");
        ImageView bagIconIndicator = new ImageView(icons.get("bag"));
        bagIconIndicator.setFitWidth(60); // Set the desired width
        bagIconIndicator.setFitHeight(60);
        HBox bagCountBox = new HBox(bagIconIndicator, bagValueLabel);
        bagCountBox.getStyleClass().add("bag-image");
        bagCountBox.setAlignment(Pos.CENTER);

        // Game Books
        Button bookListButton = new Button("Game Books");
        bookListButton.getStyleClass().add("darkgreen-button");
        bookListButton.setOnAction(e -> gameController.showBookSelectionWindow(false));
        HBox gameBooksBox = new HBox(bookListButton);
        gameBooksBox.setAlignment(Pos.CENTER);

        VBox bookBagBox = new VBox(10, bagCountBox, gameBooksBox);
        bookBagBox.setPadding(new Insets(40, 0, 50, 0));

        sideBar.getChildren().add(bookBagBox);

        return sideBar;
    }

    private Pane createPlayerInfoBoard(String playerName, String playerInfo, boolean isOtherPlayer) {
        int playerScore = isOtherPlayer ? Integer.parseInt(playerInfo.split(":")[0]) : 0;
        boolean playerTurn = isOtherPlayer ? Boolean.parseBoolean(playerInfo.split(":")[1])
                : gameViewModel.myTurnProperty().get();

        // Player's Name
        Label nameLabel = new Label();
        if (isOtherPlayer) {
            nameLabel.setText(playerName);
        } else {
            nameLabel.textProperty().bind(gameViewModel.myNameProperty());
        }
        nameLabel.getStyleClass().add(isOtherPlayer ? "other-name-label" : "main-name-label");

        // Turn indicator
        ImageView turnIndicator = new ImageView(icons.get("turn"));
        turnIndicator.setFitWidth(isOtherPlayer ? 50 : 60);
        turnIndicator.setFitHeight(isOtherPlayer ? 50 : 60);

        HBox nameBox = new HBox(10);
        nameBox.setMinWidth(isOtherPlayer ? 190 : 160);
        nameBox.setAlignment(Pos.CENTER);
        nameBox.getChildren().addAll(nameLabel);

        // Player's Score
        Label scoreLabel = new Label();
        if (isOtherPlayer) {
            scoreLabel.setText(String.valueOf(playerScore));
            if (playerScore < 0) {
                scoreLabel.setStyle("-fx-text-fill: red");
            } else {
                scoreLabel.setStyle("");
            }
        } else {
            scoreLabel.textProperty().bind(gameViewModel.myScoreProperty().asString());
            // Negative score
            gameViewModel.myScoreProperty().addListener((observable, oldScore, newScore) -> {
                Platform.runLater(() -> {
                    if (newScore.intValue() < 0) {
                        scoreLabel.setStyle("-fx-text-fill: red");
                    } else {
                        scoreLabel.setStyle("");
                    }

                    // Score effect
                    String effect = newScore.intValue() > oldScore.intValue() ? "highlight" : "failed";
                    scoreLabel.getStyleClass().add("score-" + effect);
                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1)); // 1 second
                    pauseTransition.setOnFinished(event -> {
                        scoreLabel.getStyleClass().remove("score-" + effect);
                    });

                    pauseTransition.play();

                });
            });
        }
        scoreLabel.getStyleClass().add(isOtherPlayer ? "other-score-label" : "main-score-label");

        ImageView scoreIndicator = new ImageView(icons.get("star"));
        scoreIndicator.setFitWidth(isOtherPlayer ? 20 : 50);
        scoreIndicator.setFitHeight(isOtherPlayer ? 20 : 50);
        HBox scoreBox = new HBox(scoreIndicator, scoreLabel);
        scoreBox.setMinWidth(170);
        scoreBox.setAlignment(Pos.CENTER);
        // scoreBox.setStyle("-fx-border-color: yellow;");

        if (isOtherPlayer) {
            if (playerTurn) {
                nameBox.getChildren().add(0, turnIndicator);
            }
            HBox playerInfoBoard = new HBox(10, nameBox, scoreBox);
            // playerInfoBoard.getStyleClass().add("other-score-board");
            playerInfoBoard.setMinSize(230, 30);
            playerInfoBoard.setMaxSize(230, 30);
            playerInfoBoard.setAlignment(Pos.CENTER);
            if (playerTurn) {
                playerInfoBoard.getStyleClass().add("glow-button");
                nameLabel.getStyleClass().add("glow-button");
                scoreLabel.getStyleClass().add("glow-button");
            }

            return playerInfoBoard;
        } else {
            VBox turnBox = new VBox();
            turnBox.setAlignment(Pos.CENTER_LEFT);
            turnBox.getChildren().add(turnIndicator);
            turnBox.setVisible(false);

            this.scoreBox = scoreBox;
            VBox myInfoBox = new VBox(-10, nameBox, scoreBox);
            myInfoBox.getStyleClass().add("wood-score-board");
            myInfoBox.setMinSize(230, 130);
            myInfoBox.setMaxSize(230, 130);
            if (playerTurn) {
                turnBox.getStyleClass().add("glow-button");
                myInfoBox.getStyleClass().add("glow-button");
                turnBox.setVisible(true);
            }
            gameViewModel.myTurnProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (newValue) {
                        // nameBox.getChildren().add(0, turnIndicator);
                        turnBox.setVisible(true);
                        turnBox.getStyleClass().add("glow-button");
                        myInfoBox.getStyleClass().add("glow-button");
                    } else {
                        // nameBox.getChildren().remove(0);
                        turnBox.setVisible(false);
                        turnBox.getStyleClass().remove("glow-button");
                        myInfoBox.getStyleClass().remove("glow-button");
                    }
                });
            });

            HBox playerInfoBoard = new HBox(turnBox, myInfoBox);
            myInfoBox.setAlignment(Pos.CENTER);
            playerInfoBoard.setPadding(new Insets(0, 0, 0, 20));
            return playerInfoBoard;
        }
    }

    public VBox createDrawTilesBox(String drawTilesString) {
        Label drawTilesLabel = new Label("Who Goes First?");
        drawTilesLabel.getStyleClass().add("title");
        drawTilesLabel.setStyle("-fx-font-size: 60; -fx-text-fill: steelblue;");
        drawTilesLabel.setPadding(new Insets(10, 0, 20, 0));
        drawTilesLabel.getStyleClass().add("glow-button");
        ImageView drawIcon = new ImageView(icons.get("draw"));
        drawIcon.setFitWidth(90);
        drawIcon.setFitHeight(66);
        // drawIcon.getStyleClass().add("glow-button");
        VBox drawTitleBox = new VBox(drawIcon, drawTilesLabel);
        drawTitleBox.setAlignment(Pos.CENTER);

        String firstName = drawTilesString.split(":")[0].split("-")[0];
        HBox players = new HBox(30);
        players.setPadding(new Insets(30, 0, 30, 0));
        players.setAlignment(Pos.CENTER);
        for (String s : drawTilesString.split("_")) {
            // this.totalPlayersNum++;
            String name = s.split("-")[0];
            String letter = s.split("-")[1];
            Text nameText = new Text(name);
            nameText.getStyleClass().add("little-title");
            nameText.setFill(Color.WHITE);
            nameText.setTextAlignment(TextAlignment.CENTER);
            Button tileButton = new Button();
            tileButton.setPrefSize(55, 55);
            tileButton.getStyleClass().add("tile-button");
            tileButton.setStyle("-fx-background-image: url('tiles/" + letter + ".png');");
            VBox playerBox = new VBox(15, tileButton, nameText);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPrefSize(100, 120);
            if (name.equals(firstName)) {
                playerBox.getStyleClass().add("glow-button");
                ImageView arrowIcon = new ImageView(icons.get("turn"));
                arrowIcon.setFitHeight(70);
                arrowIcon.setFitWidth(70);
                VBox turnIconBox = new VBox(arrowIcon);
                turnIconBox.setPadding(new Insets(10, 0, 0, 0));
                arrowIcon.getStyleClass().add("glow-button");
                HBox startingPlayerBox = new HBox(turnIconBox, playerBox);
                startingPlayerBox.setAlignment(Pos.CENTER);
                players.getChildren().add(startingPlayerBox);
            } else {
                players.getChildren().add(playerBox);
            }
        }
        // First Player Text
        String firstString = firstName.equals(myName) ? "You play first!" : (firstName + " is playing first!");
        Text firstPlayerText = new Text(firstString);
        firstPlayerText.setTextAlignment(TextAlignment.CENTER);
        firstPlayerText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        firstPlayerText.setFill(Color.LIGHTBLUE);
        // Help Button
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");
        helpButton.setOnAction(e -> {
            gameController.showGameInstructionsWindow();
        });

        VBox box = new VBox(40, drawTitleBox, players, firstPlayerText, helpButton);
        box.setPadding(new Insets(0, 0, 0, 0));
        box.setAlignment(Pos.CENTER);

        Node[] drawTilesNodes = createCustomBox(box, "green", "Let's Go", "drawtiles");
        VBox drawTilesBox = (VBox) drawTilesNodes[0];
        Button letsGoButton = (Button) drawTilesNodes[1];
        letsGoButton.setStyle("-fx-font-size: 25;");

        return drawTilesBox;
    }

    private void createTileButtons(List<Tile> myTiles) {
        for (Tile tile : myTiles) {
            String letter = String.valueOf(tile.getLetter());
            Button tileButton = new Button();
            tileButton.setId(letter);
            tileButton.setPrefSize(45, 45);
            tileButton.getStyleClass().add("tile-button");
            tileButton.setStyle("-fx-background-image: url('tiles/" + letter + ".png')");

            // Tiles update effect
            tileButton.getStyleClass().add("glow-button");
            PauseTransition pauseTransition = new PauseTransition(Duration.millis(300)); // 1 second
            pauseTransition.setOnFinished(event -> {
                tileButton.getStyleClass().remove("glow-button");
            });

            pauseTransition.play();

            tileButton.setOnMouseEntered(ev -> tileButton.getStyleClass().add("tile-button-hover"));
            tileButton.setOnMouseExited(ev -> tileButton.getStyleClass().remove("tile-button-hover"));

            // On Tile Click
            tileButton.setOnAction(event -> {
                boolean myTurn = gameViewModel.myTurnProperty().get();
                if (!myTurn) {
                    gameController.showTurnAlert();
                }
                if (!gameController.getPlacementCells().isEmpty()) {
                    isTilePlaced = true;
                    Pane cellPane = gameController.getPlacementCells().pop();
                    cellPane.getStyleClass().add("character");
                    cellPane.getStyleClass().add("character-" + letter);
                    cellPane.setStyle("-fx-border-color: yellow; -fx-border-style: solid inside;");

                    // Disable the tile button upon selection
                    tileButton.setDisable(true);
                    resetTilesButton.setDisable(false);
                    sortTilesButton.setDisable(true);

                    // Add the tile value to the word
                    gameViewModel.addToWord(letter);

                    if (gameController.getPlacementCells().empty()) {
                        tryPlaceWordButton.setDisable(false);
                    }
                }
            });

            tileButtons.add(tileButton);
        }
    }

    public void closeProgressIndicator() {
        progressIndicator.setVisible(false);
    }

    private VBox createButtons() {
        VBox customRoot = new VBox(15);
        customRoot.setPadding(new Insets(20));
        customRoot.setAlignment(Pos.CENTER);

        boolean myTurn = gameViewModel.myTurnProperty().get();

        // Tile buttons
        FlowPane tileButtonsPane = new FlowPane(10, 10);
        tileButtonsPane.setAlignment(Pos.CENTER);

        List<Tile> myTiles = gameViewModel.myTilesProperty();
        tileButtons = new ArrayList<>();
        createTileButtons(myTiles);
        tileButtonsPane.getChildren().setAll(tileButtons);
        gameViewModel.myTilesProperty().addListener((obs, oldTiles, newTiles) -> {
            Platform.runLater(() -> {
                tileButtons.clear();
                createTileButtons(newTiles);
                tileButtonsPane.getChildren().setAll(tileButtons);
            });
        });

        // Sort Tiles Button
        sortTilesButton = new Button(symbols.get("sort"));
        sortTilesButton.getStyleClass().add("lightblue-button");
        sortTilesButton.setDisable(!myTurn);
        sortTilesButton.setOnAction(e -> {
            isTilesSorted = true;
            sortTilesButton.setDisable(true);
            Collections.sort(tileButtons, (b1, b2) -> b1.getId().compareTo(b2.getId()));
            tileButtonsPane.getChildren().setAll(tileButtons);

            // Tiles sort effect
            tileButtons.forEach(tileButton -> tileButton.getStyleClass().add("glow-button-sorted"));
            PauseTransition pauseTransition = new PauseTransition(Duration.millis(300));
            pauseTransition.setOnFinished(event -> {
                tileButtons.forEach(tileButton -> tileButton.getStyleClass().remove("glow-button-sorted"));
            });
            pauseTransition.play();
        });

        // Reset Tiles button
        resetTilesButton = new Button(symbols.get("reset"));
        resetTilesButton.getStyleClass().add("grey-button");
        resetTilesButton.setDisable(true);
        resetTilesButton.setOnAction(event -> {
            this.isTilePlaced = false;
            gameController.resetWordPlacement(false);
            tryPlaceWordButton.setDisable(true);
            resetTilesButton.setDisable(true);
            sortTilesButton.setDisable(isTilesSorted);
        });

        // Tile buttons BOX
        HBox buttonBox = new HBox(10, sortTilesButton, resetTilesButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox tileBox = new VBox(15, tileButtonsPane, buttonBox);
        tileBox.setAlignment(Pos.CENTER);
        tileBox.setPadding(new Insets(0, 0, 40, 0));

        // Pass turn Button
        passTurnButton = new Button("Pass Turn");
        passTurnButton.getStyleClass().add("yellow-button");
        passTurnButton.setStyle("-fx-font-size: 23px;");
        passTurnButton.setDisable(!myTurn);
        passTurnButton.setOnAction(e -> {
            isTilePlaced = false;
            gameViewModel.skipTurn();
        });

        // Challenge Button
        challengeButton = new Button("Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setStyle("-fx-font-size: 25px;");
        challengeButton.setDisable(true);
        challengeButton.setOnAction(event -> {
            isTilePlaced = false;
            challengeButton.setDisable(true);
            passTurnButton.setDisable(true);
            progressIndicator.setVisible(true);

            Task<Void> challengeTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // challenge
                    gameViewModel.challenge();
                    return null;
                }

            };
            new Thread(challengeTask).start();
        });

        // My Turn Listener
        gameViewModel.myTurnProperty().addListener((observable, oldTurn, newTurn) -> {
            Platform.runLater(() -> {
                isTilePlaced = false;
                isTilesSorted = false;
                passTurnButton.setDisable(!newTurn);
                sortTilesButton.setDisable(!newTurn);
                tryPlaceWordButton.setDisable(true);
                challengeButton.setDisable(true);
            });
        });

        // Quit game Button
        Button quitGameButton = new Button("Quit Game");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setStyle("-fx-font-size: 23px;");
        quitGameButton.setOnAction(e -> gameController.showQuitGameWindow());

        // Try place word Button
        tryPlaceWordButton = new Button("Try Place Word");
        tryPlaceWordButton.getStyleClass().add("blue-button");
        tryPlaceWordButton.setStyle("-fx-font-size: 26px;");
        tryPlaceWordButton.setDisable(true);
        tryPlaceWordButton.setOnAction(event -> {
            isTilePlaced = false;
            gameBoard.setDisable(true);
            resetTilesButton.setDisable(true);
            tryPlaceWordButton.setDisable(true);
            progressIndicator.setVisible(true);

            Task<Void> tryPlaceWordTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // try place word
                    String word = gameViewModel.getWord();
                    gameViewModel.tryPlaceWord(word);
                    return null;
                }

            };
            new Thread(tryPlaceWordTask).start();
        });

        VBox playButtonsBox = new VBox(15, progressIndicator, tryPlaceWordButton, challengeButton, passTurnButton,
                quitGameButton);
        playButtonsBox.setAlignment(Pos.CENTER);
        // playButtonsBox.setPadding(new Insets(0, 0, 0, 0));

        // Round Buttons
        HBox roundButtonsBox = createRoundButtons(true);
        roundButtonsBox.setPrefHeight(70);
        roundButtonsBox.setAlignment(Pos.BOTTOM_CENTER);
        // Help Button
        Button helpButton = (Button) roundButtonsBox.getChildren().get(0);
        helpButton.setOnAction(e -> gameController.showGameInstructionsWindow());
        // Messages Button
        Button messageButton = (Button) roundButtonsBox.getChildren().get(1);
        Node[] messagesNodes = createCustomBox(createMessagesBox(), "red", symbols.get("exit"), "message");
        VBox messagesBox = (VBox) messagesNodes[0];
        messageButton.setOnAction(e -> gameController.showCustomWindow(messagesBox, 700, 370));

        customRoot.getChildren().addAll(tileBox, playButtonsBox, roundButtonsBox);

        return customRoot;
    }

    public VBox createGameInstuctionsBox() {
        // How To Play
        VBox howToPlayBox = new VBox(15);
        howToPlayBox.setAlignment(Pos.CENTER);
        Label howToLabel = new Label("How To Play");
        howToLabel.getStyleClass().add("title");
        howToLabel.setStyle("-fx-font-size: 40; -fx-text-fill: black; ");
        howToLabel.setPadding(new Insets(0, 0, 30, 0));

        Label selectCellLabel = new Label("1. Select Cells:");
        selectCellLabel.getStyleClass().add("title");
        selectCellLabel.setStyle("-fx-font-size: 18;  -fx-text-fill: black;");
        Text selectCellsText = new Text(descriptions.get("select-cells"));
        selectCellsText.setFont(new Font(16));
        selectCellsText.setTextAlignment(TextAlignment.CENTER);
        ImageView selectCellsGif = new ImageView(new Image("instructions/select-cells.gif"));
        VBox firstBox = new VBox(20, selectCellLabel, selectCellsGif, selectCellsText);
        firstBox.setAlignment(Pos.CENTER);
        firstBox.setPadding(new Insets(15, 15, 15, 15));
        VBox.setMargin(selectCellLabel, new Insets(0, 0, 0, 15));

        Label selectTilesLabel = new Label("2. Select Tiles:");
        selectTilesLabel.getStyleClass().add("title");
        selectTilesLabel.setStyle("-fx-font-size: 18;  -fx-text-fill: black;");
        Text selectTilesText = new Text(descriptions.get("select-tiles"));
        selectTilesText.setFont(new Font(16));
        selectTilesText.setTextAlignment(TextAlignment.CENTER);
        ImageView selectTilesGif = new ImageView(new Image("instructions/select-tiles.gif"));
        VBox secondBox = new VBox(20, selectTilesLabel, selectTilesGif, selectTilesText);
        secondBox.setAlignment(Pos.CENTER);
        secondBox.setPadding(new Insets(15, 15, 15, 15));
        VBox.setMargin(selectTilesLabel, new Insets(0, 0, 0, 15));

        Label tryButtonLabel = new Label("3. Click the Try Place Button:");
        tryButtonLabel.getStyleClass().add("title");
        tryButtonLabel.setStyle("-fx-font-size: 18;  -fx-text-fill: black;");
        Text tryButtonText = new Text(descriptions.get("try-button"));
        tryButtonText.setFont(new Font(16));
        tryButtonText.setTextAlignment(TextAlignment.CENTER);
        ImageView tryPlaceGif = new ImageView(new Image("instructions/try-place.gif"));
        VBox thirdBox = new VBox(20, tryButtonLabel, tryPlaceGif, tryButtonText);
        thirdBox.setAlignment(Pos.CENTER);
        thirdBox.setPadding(new Insets(15, 15, 15, 15));
        VBox.setMargin(tryButtonLabel, new Insets(0, 0, 0, 15));

        Label placeWordLabel = new Label("4. Place Word:");
        placeWordLabel.getStyleClass().add("title");
        placeWordLabel.setStyle("-fx-font-size: 18; -fx-text-fill: black;");
        Text placeWordText = new Text(descriptions.get("place-word"));
        placeWordText.setFont(new Font(16));
        placeWordText.setTextAlignment(TextAlignment.CENTER);
        ImageView placeWordGif = new ImageView(new Image("instructions/place-word.gif"));
        VBox fourthBox = new VBox(20, placeWordLabel, placeWordGif, placeWordText);
        fourthBox.setAlignment(Pos.CENTER);
        fourthBox.setPadding(new Insets(15, 15, 15, 15));
        VBox.setMargin(placeWordLabel, new Insets(0, 0, 0, 15));

        VBox main = new VBox(10, firstBox, secondBox, thirdBox, fourthBox);
        main.setStyle("-fx-background-color: rgb(168, 79, 212);");
        main.setAlignment(Pos.CENTER);
        main.setPrefSize(700, 350);

        ScrollPane scrollPane = new ScrollPane(main);
        scrollPane.setMinViewportWidth(700); // Set the preferred width
        scrollPane.setMinViewportHeight(200); // Set the preferred height
        scrollPane.setMinHeight(350);
        scrollPane.setStyle("-fx-background-color: rgb(168, 79, 212);");

        howToPlayBox.getChildren().addAll(howToLabel, scrollPane);

        // Rules And Gameplay
        VBox instructionsBox = new VBox(15);
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setPrefSize(600, 200);
        Label instructionsLabel = new Label("Rules And Gameplay");
        instructionsLabel.getStyleClass().add("title");
        instructionsLabel.setStyle("-fx-font-size: 40; -fx-text-fill: black; ");
        instructionsLabel.setPadding(new Insets(0, 0, 30, 0));

        Text instructionsText = new Text(descriptions.get("game-flow-rules"));
        instructionsText.setFont(new Font(16));

        VBox main2 = new VBox(10, instructionsText);
        main2.setStyle("-fx-background-color: rgb(168, 79, 212);");
        main2.setAlignment(Pos.CENTER);
        main2.setPrefSize(700, 350);

        ScrollPane scrollPane2 = new ScrollPane(main2);
        scrollPane2.setMinViewportWidth(700); // Set the preferred width
        scrollPane2.setMinViewportHeight(200); // Set the preferred height
        scrollPane2.setMinHeight(350);
        scrollPane2.setStyle("-fx-background-color: rgb(168, 79, 212);");

        instructionsBox.getChildren().addAll(instructionsLabel, scrollPane2);

        VBox[] switchList = { howToPlayBox, instructionsBox };
        HBox gameInstructionsBox = createSwitchableHBox(switchList);
        Node[] helpBoxNodes = createCustomBox(gameInstructionsBox, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpBoxNodes[0];

        return helpBox;
    }

    public List<Button> getTileButtons() {
        return tileButtons;
    }

    private GridPane createGameBoard() {
        gameBoard = new GridPane();
        gameBoard.setMinSize(734, 734);
        gameBoard.setMaxSize(734, 734);
        gameBoard.getStyleClass().add("board-background");

        gameViewModel.currentBoardProperty().addListener((observable, oldBoard, newBoard) -> {
            Platform.runLater(() -> {
                if (progressIndicator.isVisible()) {
                    progressIndicator.setVisible(false);
                }
                gameBoard.setDisable(false);
                updateBoard(newBoard);
            });
        });

        // Initial setup
        initializeGameBoardCells();

        return gameBoard;
    }

    private void initializeGameBoardCells() {
        int boardSize = gameViewModel.currentBoardProperty().get().length;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Create cell
                Pane cellPane = new Pane();
                cellPane.getStyleClass().add("board-cell");
                cellPane.setPrefSize(60, 60);
                gameBoard.add(cellPane, col, row);

                cellPane.setOnMouseClicked(event -> {
                    // Handle cell click events
                    handleCellClick(cellPane);
                });
            }
        }
    }

    private void updateBoard(Tile[][] board) {

        if (!isTilePlaced) {
            gameController.resetWordPlacement(true);
            int boardSize = board.length;

            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Pane cellPane = (Pane) gameBoard.getChildren().get(row * boardSize + col);

                    if (board[row][col] != null) {
                        String letter = String.valueOf(board[row][col].getLetter());
                        // Update styling based on the letter
                        cellPane.getStyleClass().add("character");
                        cellPane.getStyleClass().add("character-" + letter);
                    } else {
                        cellPane.getStyleClass().removeIf(style -> style.startsWith("character-"));
                    }
                }
            }
        }
    }

    private void handleCellClick(Pane cell) {
        // Not your turn
        if (!gameViewModel.myTurnProperty().get()) {
            gameController.showTurnAlert();
        } else {

            // Player hasn't placed tiles yet
            if (!isTilePlaced) {

                if (gameController.getSelectedCells().size() == 0) {
                    // Add The Cell
                    gameController.getSelectedCells().add(cell);
                    cell.getStyleClass().add("selected");
                } else if (gameController.getSelectedCells().size() == 1) {
                    // Already selected
                    if (gameController.getSelectedCells().contains(cell)) {
                        gameController.getSelectedCells().remove(cell);
                        cell.getStyleClass().remove("selected");
                    } else {
                        int firstRow = GridPane.getRowIndex(gameController.getSelectedCells().get(0));
                        int firstCol = GridPane.getColumnIndex(gameController.getSelectedCells().get(0));
                        int lastRow = GridPane.getRowIndex(cell);
                        int lastCol = GridPane.getColumnIndex(cell);
                        // Same row/col only
                        if (firstRow == lastRow || firstCol == lastCol) {
                            gameController.getSelectedCells().add(cell);
                            cell.getStyleClass().add("selected");
                        }
                    }
                } else if (gameController.getSelectedCells().size() == 2) {
                    if (gameController.getSelectedCells().contains(cell)) {
                        // Deselect the clicked cell
                        gameController.getSelectedCells().remove(cell);
                        cell.getStyleClass().remove("selected");
                    } else {
                        gameController.getSelectedCells().forEach(c -> c.getStyleClass().remove("selected"));
                        gameController.getSelectedCells().clear();
                        // add the new one
                        gameController.getSelectedCells().add(cell);
                        cell.getStyleClass().add("selected");
                    }
                    // Clear The Word's Location Lists
                    gameController.getPlacementCells().clear();
                    gameController.getPlacementTileList().clear();
                }

                // Enable/disable buttons based on the number of selected cells
                boolean enableButtons = gameController.getSelectedCells().size() == 2;
                // tryPlaceWordButton.setDisable(!enableButtons);

                if (enableButtons) {
                    int firstRow = GridPane.getRowIndex(gameController.getSelectedCells().get(0));
                    int firstCol = GridPane.getColumnIndex(gameController.getSelectedCells().get(0));
                    int lastRow = GridPane.getRowIndex(gameController.getSelectedCells().get(1));
                    int lastCol = GridPane.getColumnIndex(gameController.getSelectedCells().get(1));

                    gameViewModel.setFirstSelectedCellRow(firstRow);
                    gameViewModel.setFirstSelectedCellCol(firstCol);
                    gameViewModel.setLastSelectedCellRow(lastRow);
                    gameViewModel.setLastSelectedCellCol(lastCol);

                    gameController.setPlacementCells();
                }
            }
        }
    }

    public Node getCellFromBoard(int row, int col) {
        for (Node node : gameBoard.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return node;
            }
        }
        return null;
    }

    private HBox createSwitchableHBox(VBox[] vBoxes) {
        HBox switchableHBox = new HBox();
        switchableHBox.setAlignment(Pos.CENTER);
        switchableHBox.setPadding(new Insets(0, 0, 20, 0));

        // Create buttons for switching
        Button firstButton = new Button(symbols.get("left"));
        firstButton.getStyleClass().add("lightblue-button");
        VBox prevBox = new VBox(firstButton);
        prevBox.setAlignment(Pos.CENTER);
        Button secondButton = new Button(symbols.get("right"));
        secondButton.getStyleClass().add("lightblue-button");
        VBox nextBox = new VBox(secondButton);
        nextBox.setAlignment(Pos.CENTER);

        // Initialize the index to 0
        int[] currentIndex = { 0 };

        // Create a StackPane to hold the VBox in the middle with a fixed size
        StackPane middleContainer = new StackPane();
        middleContainer.setPrefSize(450, 250); // Set your desired fixed size here
        middleContainer.setPadding(new Insets(0, 10, 0, 10));
        middleContainer.getChildren().add(vBoxes[currentIndex[0]]);

        // Handle switching to the previous VBox
        firstButton.setOnAction(event -> {
            middleContainer.getChildren().remove(vBoxes[currentIndex[0]]);
            currentIndex[0] = (currentIndex[0] - 1 + vBoxes.length) % vBoxes.length;
            middleContainer.getChildren().add(vBoxes[currentIndex[0]]);
        });

        // Handle switching to the next VBox
        secondButton.setOnAction(event -> {
            middleContainer.getChildren().remove(vBoxes[currentIndex[0]]);
            currentIndex[0] = (currentIndex[0] + 1) % vBoxes.length;
            middleContainer.getChildren().add(vBoxes[currentIndex[0]]);
        });

        switchableHBox.getChildren().addAll(prevBox, middleContainer, nextBox);
        return switchableHBox;
    }

    private String getHighlightStyle(HighlightOutcome outcome) {
        switch (outcome) {
            case SUCCESS:
                return "highlight";
            case FAILURE:
                return "failed";
            case CHALLENGE_SUCCESSFUL:
                return "challenge-successful";
            default:
                throw new IllegalArgumentException("Invalid highlight outcome");
        }
    }

    public enum HighlightOutcome {
        SUCCESS,
        FAILURE,
        CHALLENGE_SUCCESSFUL
    }

    public void highlightCellsForWords(List<Word> words, HighlightOutcome outcome) {
        List<Pane> panes = new ArrayList<>();

        for (Word word : words) {
            int row = word.getRow();
            int col = word.getCol();
            boolean isVertical = word.isVertical();

            for (Tile t : word.getTiles()) {
                Pane cellPane = (Pane) getCellFromBoard(row, col);
                if (cellPane != null) {
                    cellPane.getStyleClass().add(getHighlightStyle(outcome));
                    panes.add(cellPane);
                }

                if (isVertical) {
                    row++;
                } else {
                    col++;
                }
            }
        }

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1)); // 1 second
        pauseTransition.setOnFinished(event -> {
            panes.forEach(p -> p.getStyleClass().remove(getHighlightStyle(outcome)));
        });

        pauseTransition.play();
    }

    public void showDoubleScoreEffect() {
        // Double Score Label
        Label doubleScoreLabel = new Label("x2");
        doubleScoreLabel.getStyleClass().add("double-score-label");
        VBox doubleScoreBox = new VBox(doubleScoreLabel);
        doubleScoreBox.setAlignment(Pos.CENTER);

        // Add the doubleScoreBox to the center of myInfoBox
        scoreBox.getChildren().add(0, doubleScoreBox);

        // Create a scale animation to make the box larger
        ScaleTransition scaleAnimation = new ScaleTransition(Duration.seconds(0.5), doubleScoreBox);
        scaleAnimation.setToX(2); // Scale horizontally to 150%
        scaleAnimation.setToY(2); // Scale vertically to 150%

        // Create a fade animation to make the box fade out
        FadeTransition fadeOutAnimation = new FadeTransition(Duration.seconds(1), doubleScoreBox);
        fadeOutAnimation.setFromValue(1.0);
        fadeOutAnimation.setToValue(0.0);

        // Sequentially play the scale and fade animations
        scaleAnimation.setOnFinished(event -> {
            fadeOutAnimation.play();
        });

        // Remove the box after it's fully faded out
        fadeOutAnimation.setOnFinished(event -> {
            scoreBox.getChildren().remove(doubleScoreBox);
        });

        // Play the scale animation to start the effect
        scaleAnimation.play();
    }

    public VBox createMessagesBox() {
        VBox messagesBox = new VBox(30);

        HBox sendToBox = new HBox(10);

        Text sendText = new Text("Send Message to:");
        sendText.getStyleClass().add("title");
        // sendText.setStyle("-fx-fill: antiquewhite;");
        // sendText.setTextAlignment(TextAlignment.CENTER);
        ComboBox<String> players = new ComboBox<>();
        players.getStyleClass().add("text-field");
        for (String name : gameViewModel.othersInfoProperty().keySet()) {
            players.getItems().add(name);
        }
        if (gameViewModel.othersInfoProperty().keySet().size() > 1) {
            players.getItems().add("All");
            players.setValue("All");
        } else {
            players.setValue(gameViewModel.othersInfoProperty().keySet().iterator().next());

        }
        sendToBox.getChildren().addAll(sendText, players);
        sendToBox.setAlignment(Pos.CENTER);

        TextField replyField = new TextField();
        replyField.getStyleClass().add("text-field");
        replyField.setMinWidth(500);
        replyField.setMaxWidth(500);
        replyField.setOnMouseClicked(e -> replyField.setText(""));
        players.setOnMouseClicked(e -> replyField.setText(""));

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("lightgreen-button");
        sendButton.setStyle("-fx-font-size: 25px;");
        sendButton.setOnAction(e -> {
            gameController.closeCustomWindow();
            Platform.runLater(() -> {
                String to = players.getValue();
                String message = replyField.getText();
                String sanitizedMessage = message.replaceAll(",", "#");
                if (sanitizedMessage.length() > 0) {
                    if (to.equals("All")) {
                        gameViewModel.sendToAll(sanitizedMessage);
                    } else {
                        gameViewModel.sendTo(to, sanitizedMessage);
                    }
                }
            });
        });

        HBox inputBox = new HBox(10, replyField, sendButton);
        inputBox.setAlignment(Pos.CENTER);

        messagesBox.getChildren().addAll(sendToBox, inputBox);
        messagesBox.setAlignment(Pos.CENTER);

        return messagesBox;
    }

    public VBox createMessageAlertBox(String sender, String message, boolean toAll) {
        String title = sender + " Sent" + (toAll ? " to all:" : ":");

        String sanitizedMessage = message.replace("#", ",");

        // Title
        Text messageTitle = new Text(title);
        messageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        // Message
        Text messageText = new Text(sanitizedMessage);
        messageText.setFont(Font.font("Arial", 24));
        // Close Button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("blue-button");
        closeButton.setOnAction(e -> gameController.closeCustomWindow());
        // Reply Field
        TextField messageField = new TextField();
        messageField.getStyleClass().add("text-field");
        messageField.setMinWidth(500);
        messageField.setMaxWidth(500);
        // Send reply message
        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("lightgreen-button");
        sendButton.setStyle("-fx-font-size: 25px;");
        sendButton.setOnAction(e -> {
            String replyMsg = messageField.getText();
            String sanitizedReplyMsg = replyMsg.replaceAll(",", "#");
            if (sanitizedReplyMsg.length() > 0) {
                gameViewModel.sendTo(sender, sanitizedReplyMsg);
            }
            gameController.closeCustomWindow();
        });
        HBox replyBox = new HBox(10, messageField, sendButton);
        // replyBox.setPadding(new Insets(0, 0, 30, 0));
        replyBox.setAlignment(Pos.CENTER);
        replyBox.setVisible(false);
        // Reply Button
        Button replyButton = new Button("Reply");
        replyButton.getStyleClass().add("green-button");
        replyButton.setOnAction(e -> {
            replyButton.setDisable(true);
            replyBox.setVisible(true);
        });
        HBox buttonBox = new HBox(15, closeButton, replyButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox messageBox = new VBox(30, messageTitle, messageText, replyBox, buttonBox);
        messageBox.setAlignment(Pos.CENTER);

        Node[] messageNodes = createCustomBox(messageBox, null, null, "message");
        VBox messageWindowBox = (VBox) messageNodes[0];

        return messageWindowBox;
    }

    public VBox createChallengeBox(List<Word> illegalWords, boolean afterChallenge) {
        VBox box = null;
        String words = "";
        for (int i = 0; i < illegalWords.size(); i++) {
            words += illegalWords.get(i).toString();
            if (i != illegalWords.size() - 1) {
                words += ",";
            }
        }
        if (afterChallenge) {
            box = createTextAlertBox("Challenge Failed",
                    words + "\nOne of these words is not found in the game books\n\nYou lost 10 points.",
                    true);
        } else {
            box = createTextAlertBox(words,
                    "One of these words is not dictionary legal\nYou can try Challenge or Pass turn.", true);
            challengeButton.setDisable(false);
        }
        Node[] nodes = createCustomBox(box, !afterChallenge ? "blue" : null, "OK", "alert");
        Button okButton = (Button) nodes[1];

        VBox iligalWordsBox = (VBox) nodes[0];

        return iligalWordsBox;
    }

    public VBox createIllegalMoveBox() {
        VBox illegalMoveBox = createTextAlertBox("Illegal Word Placement", descriptions.get("not-board-legal"), true);
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");
        gameBoard.setDisable(false);
        helpButton.setOnAction(e -> {
            gameController.resetWordPlacement(true);
            gameController.showGameInstructionsWindow();
        });
        illegalMoveBox.getChildren().add(helpButton);

        Node[] nodes = createCustomBox(illegalMoveBox, "green", "Try Again", "alert");
        Button tryAgainButton = (Button) nodes[1];
        tryAgainButton.setOnAction(e -> {
            gameController.resetWordPlacement(true);
            gameController.closeCustomWindow();
        });

        return (VBox) nodes[0];
    }

    public VBox createEndGameBox(boolean isHostQuit) {
        VBox endGameBox = new VBox(10);
        endGameBox.setAlignment(Pos.CENTER);
        endGameBox.getStyleClass().add("night-sky-background");

        Label gameOverLabel = new Label("Game Over");
        gameOverLabel.getStyleClass().add("title");
        gameOverLabel.setStyle("-fx-font-size: 60; -fx-text-fill: dodgerblue; ");
        gameOverLabel.setPadding(new Insets(10, 0, 20, 0));
        endGameBox.getChildren().add(gameOverLabel);

        if (isHostQuit) {
            Label hostQuitLabel = new Label("The host has quit the game!");
            hostQuitLabel.setStyle("-fx-text-fill: red; -fx-font-size: 25;");
            hostQuitLabel.setPadding(new Insets(30, 0, 0, 0));
            hostQuitLabel.getStyleClass().add("little-title");
            endGameBox.getChildren().add(hostQuitLabel);
        }

        // Resaults List
        String yourPlayerName = gameViewModel.myNameProperty().get();
        int yourPlayerScore = gameViewModel.myScoreProperty().get();
        List<String> playerInfoList = new ArrayList<>();
        playerInfoList.add(yourPlayerName + ":" + yourPlayerScore);
        for (Map.Entry<String, String> entry : gameViewModel.othersInfoProperty().entrySet()) {
            String playerName = entry.getKey();
            String playerInfo = entry.getValue();

            // Split the playerInfo into score and turn values
            String[] playerData = playerInfo.split(":");
            int playerScore = Integer.parseInt(playerData[0]);

            playerInfoList.add(playerName + ":" + playerScore);
        }
        playerInfoList.sort((p1, p2) -> {
            int score1 = Integer.parseInt(p1.split(":")[1]);
            int score2 = Integer.parseInt(p2.split(":")[1]);
            if (score1 == score2) {
                String name1 = p1.split(":")[0];
                String name2 = p2.split(":")[0];
                return name1.compareTo(name2);
            } else {
                return Integer.compare(score2, score1);
            }
        });

        Label winnerLabel = new Label("The winner is");
        winnerLabel.setStyle("-fx-text-fill: burlywood; ");
        winnerLabel.setPadding(new Insets(50, 0, 0, 0));
        winnerLabel.getStyleClass().add("little-title");
        endGameBox.getChildren().add(winnerLabel);

        // Winner Player
        Label winnerNameLabel = new Label(playerInfoList.get(0).split(":")[0]);
        winnerNameLabel.getStyleClass().add("other-name-label");
        winnerNameLabel.getStyleClass().add("glow-button");
        winnerNameLabel.setStyle("-fx-font-size: 36px; ");
        ImageView trophyIcon = new ImageView(icons.get("trophy"));
        HBox winnerNameBox = new HBox(15, trophyIcon, winnerNameLabel);
        winnerNameBox.setMinWidth(160);
        winnerNameBox.setAlignment(Pos.CENTER);

        Label winnerScoreLabel = new Label(playerInfoList.get(0).split(":")[1]);
        winnerScoreLabel.getStyleClass().add("winner-score-label");
        winnerScoreLabel.getStyleClass().add("glow-button");

        ImageView winnerScoreIcon = new ImageView(icons.get("star"));
        winnerScoreIcon.setFitWidth(40);
        winnerScoreIcon.setFitHeight(40);
        HBox winnerScoreBox = new HBox(winnerScoreIcon, winnerScoreLabel);
        winnerScoreBox.setMinWidth(170);
        winnerScoreBox.setAlignment(Pos.CENTER);

        HBox winnerBox = new HBox(winnerNameBox, winnerScoreBox);
        winnerBox.setAlignment(Pos.CENTER);
        winnerBox.getStyleClass().add("glow-button");
        winnerBox.setPadding(new Insets(0, 0, 30, 0));
        endGameBox.getChildren().add(winnerBox);

        for (int i = 1; i < playerInfoList.size(); i++) {
            // Other Player's
            Label nameLabel = new Label(playerInfoList.get(i).split(":")[0]);
            nameLabel.getStyleClass().add("other-name-label");
            HBox nameBox = new HBox(nameLabel);
            nameBox.setMinWidth(160);
            nameBox.setAlignment(Pos.CENTER);

            Label scoreLabel = new Label();
            scoreLabel.setText(String.valueOf(playerInfoList.get(i).split(":")[1]));
            scoreLabel.getStyleClass().add("other-score-label");

            ImageView scoreIcon = new ImageView(icons.get("star"));
            scoreIcon.setFitWidth(20);
            scoreIcon.setFitHeight(20);
            HBox scoreBox = new HBox(scoreIcon, scoreLabel);
            scoreBox.setMinWidth(170);
            scoreBox.setAlignment(Pos.CENTER);

            HBox playerBox = new HBox(nameBox, scoreBox);
            playerBox.setAlignment(Pos.CENTER);
            endGameBox.getChildren().add(playerBox);
        }

        Button newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-font-size: 26px;");
        newGameButton.getStyleClass().add("green-button");
        newGameButton.setOnAction(e -> {
            gameController.close();
            gameController.showInitialWindow();
        });

        Button quitGameButton = new Button("Quit Game");
        quitGameButton.setStyle("-fx-font-size: 26px;");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setOnAction(e -> {
            gameController.close();
            System.exit(0);
        });

        HBox buttonsBox = new HBox(20, newGameButton, quitGameButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(80, 0, 0, 0));
        endGameBox.getChildren().add(buttonsBox);

        return endGameBox;
    }

    public VBox createNewGameBox(String title, String text) {
        VBox waitingRoomQuitBox = createTextAlertBox(title, text, true);
        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("darkgreen-button");
        newGameButton.setStyle("-fx-font-size: 23px;");
        newGameButton.setOnAction(e -> {
            gameViewModel.resetGame();
            gameViewModel.quitGame();
            gameController.close();
            gameController.gameLoginStage.close();
            gameController.showInitialWindow();
            isHost = false;
        });
        Button quitGameButton = new Button("Quit Game");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setStyle("-fx-font-size: 23px;");
        quitGameButton.setOnAction(e -> {
            // gameViewModel.quitGame();
            gameController.close();
            System.exit(0);
        });

        HBox buttonBox = new HBox(10, newGameButton, quitGameButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        waitingRoomQuitBox.getChildren().add(buttonBox);
        Node[] nodes = createCustomBox(waitingRoomQuitBox, null, null, "alert");
        VBox mainBox = (VBox) nodes[0];
        return mainBox;
    }

    public Image getGameIcon() {
        return icons.get("game");
    }

    public boolean isHost() {
        return isHost;
    }
}
