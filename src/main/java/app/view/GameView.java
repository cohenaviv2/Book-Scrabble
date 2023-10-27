package app.view;

import java.io.*;
import java.net.*;
import java.util.*;

import app.model.game.Tile;
import app.model.game.Word;
import app.model.host.HostModel;
import app.view_model.ViewModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.*;
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

public class GameView {
    //
    private ViewModel gameViewModel;
    private GameController gameController;
    private boolean isHost;
    // Styles
    private Map<String, String> descriptions;
    private Map<String, String> symbols;
    private Map<String, Image> icons;
    public final String styleSheet;
    //
    private GridPane gameBoard;
    private List<Button> tileButtons;

    private Button tryPlaceWordButton;
    private Button resetTilesButton;
    private Button sortTilesButton;
    //
    private HBox osBar;
    //
    public String myName;
    private int customPort = 0;
    private boolean isTilePlaced;

    public GameView(ViewModel viewModel, GameController gameController) {
        this.osBar = null;
        this.gameViewModel = viewModel;
        this.gameController = gameController;
        // Initialize and set up the initial UI components here.
        this.styleSheet = getClass().getResource("/style.css").toExternalForm();
        this.icons = new HashMap<>();
        icons.put("game", new Image("icons/game-icon.png"));
        icons.put("turn", new Image("icons/turn-icon.png"));
        icons.put("star", new Image("icons/star-icon.png"));
        icons.put("bag", new Image("icons/bag-icon.png"));
        icons.put("trophy", new Image("icons/trophy-icon.png"));
        icons.put("arrow", new Image("icons/arrow-icon.png"));
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
        this.descriptions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\descriptions.txt"))) {
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
        HBox headerBox = new HBox(headerLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(40, 0, 40, 0));

        // Game mode label
        Label modeLabel = new Label("Choose Game Mode");
        modeLabel.getStyleClass().add("mode-label");

        // Host and Guest Button pane
        HBox buttonPane = new HBox(10);
        buttonPane.setAlignment(Pos.CENTER);

        // Host Button
        Button hostButton = new Button("Host a Game");
        hostButton.getStyleClass().add("blue-button");
        hostButton.setStyle("-fx-font-size: 22px;");
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
        guestButton.setStyle("-fx-font-size: 22px;");
        guestButton.setMinSize(200, 80);
        guestButton.setMaxSize(200, 80);

        guestButton.setOnAction(event -> {
            this.isHost = false;
            this.gameViewModel.initialize(isHost);
            gameController.gameLoginStage.close();
            gameController.showLoginWindow(isHost);
        });

        buttonPane.getChildren().addAll(hostButton, guestButton);
        buttonPane.setPadding(new Insets(0, 0, 30, 0));

        // Help Button
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");

        Text hostModeTitle = new Text("Host Mode");
        hostModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text hostModeText = new Text(descriptions.get("game-mode-host"));
        hostModeText.getStyleClass().add("content-label");
        Text guestModeTitle = new Text("Guest Mode");
        guestModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text guestModeText = new Text(descriptions.get("game-mode-guest"));
        guestModeText.getStyleClass().add("content-label");

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
            checkHostConnection();
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
        Label nameLabel = new Label("My name:");
        nameLabel.getStyleClass().add("login-label");
        String tempName = isHost ? "Aviv" : "Moshe";
        TextField nameTextField = new TextField(tempName);
        nameTextField.getStyleClass().add("text-field");
        nameTextField.setAlignment(Pos.CENTER);
        nameTextField.setMaxWidth(200);
        nameTextField.setOnMouseClicked(e -> nameTextField.getStyleClass().remove("error-field"));
        textFields.add(nameTextField);
        loginFormBox.getChildren().addAll(nameLabel, nameTextField);

        // Select books
        Label selectBookLabel = new Label("Select Books:");
        selectBookLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> {
            booksButton.getStyleClass().remove("error-field");
            gameController.showBookSelectionWindow(true);
        });
        loginFormBox.getChildren().addAll(selectBookLabel, booksButton);

        // Round buttons pane
        HBox roundButtonsPane = createRoundButtons(false);
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        String mode = isHost ? "Host" : "Guest";
        Text modeTitle = new Text(mode + " Mode");
        modeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        String descMode = isHost ? "host" : "guest";
        Text modeText = new Text(descriptions.get("game-mode-" + descMode));
        modeText.getStyleClass().add("content-label");
        modeText.setTextAlignment(TextAlignment.CENTER);
        VBox modeExp = new VBox(10, modeTitle, modeText);
        modeExp.setAlignment(Pos.CENTER);
        Node[] helpNodes = createCustomBox(modeExp, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpNodes[0];
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 700, 400));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        HBox settings = createSettingBox();
        Node[] settingBoxNodes = createCustomBox(settings, "red", symbols.get("exit"), "settings");
        VBox settingsBox = (VBox) settingBoxNodes[0];
        settingsButton.setOnAction(e -> gameController.showCustomWindow(settingsBox, 600, 500));

        // ** HOST **
        if (isHost) {
            // Number of players
            Label numOfPlayersLabel = new Label("Number of Players:");
            numOfPlayersLabel.getStyleClass().add("login-label");
            ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
            numOfPlayersComboBox.setValue(2);
            numOfPlayersComboBox.getItems().addAll(2, 3, 4);
            numOfPlayersComboBox.getStyleClass().add("text-field");
            loginFormBox.getChildren().addAll(numOfPlayersLabel, numOfPlayersComboBox);

            loginFormBox.getChildren().add(roundButtonsPane);

            // Submit Buttons
            HBox submitButtons = createSubmitButtons("Start Game", 160);
            Button startGameButton = (Button) submitButtons.getChildren().get(1);
            startGameButton.setStyle("-fx-font-size: 22px;");
            loginFormBox.getChildren().add(submitButtons);

            startGameButton.setOnAction(event -> {
                checkHostConnection();
                boolean isEmpty = false;
                myName = nameTextField.getText();
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
                        startGameButton.setDisable(true);
                        gameViewModel.setTotalPlayersCount(numOfPlayersComboBox.getValue());
                        gameViewModel.connectMe(myName, "0", customPort);
                        gameViewModel.myBooksChoice(gameController.getSelectedBooks());
                        gameViewModel.ready();

                        loginFormBox.setDisable(true);
                        osBar.setDisable(true);

                        gameController.showCustomWindow(fullWaitingBox, 500, 350);
                    }
                }
            });

            // ** GUEST **
        } else {
            // Host's ip
            Label ipLabel = new Label("Host's IP:");
            ipLabel.getStyleClass().add("login-label");
            TextField ipTextField = new TextField();
            ipTextField.setMaxWidth(250);
            ipTextField.setText("localhost");
            ipTextField.setAlignment(Pos.CENTER);
            ipTextField.getStyleClass().add("text-field");
            ipTextField.setOnMouseClicked(e -> ipTextField.getStyleClass().remove("error-field"));
            textFields.add(ipTextField);
            loginFormBox.getChildren().addAll(ipLabel, ipTextField);

            loginFormBox.getChildren().add(roundButtonsPane);

            // Submit Buttons
            HBox submitButtons = createSubmitButtons("Join Game", 140);
            Button joinGameButton = (Button) submitButtons.getChildren().get(1);
            joinGameButton.setStyle("-fx-font-size: 22px;");
            loginFormBox.getChildren().add(submitButtons);

            joinGameButton.setOnAction(event -> {
                boolean isEmpty = false;
                myName = nameTextField.getText();
                String ip = ipTextField.getText();

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
                        if (customPort == 0) {
                            customPort = HostModel.HOST_SERVER_PORT;
                        }
                        gameViewModel.connectMe(myName, ip, customPort);

                        loginFormBox.setDisable(true);
                        osBar.setDisable(true);

                        if (!gameViewModel.isConnected()) {
                            VBox networkAlert = createTextAlertBox("Network Error",
                                    descriptions.get("guest-socket-error"),
                                    true);
                            Node[] p = createCustomBox(networkAlert, "red", "Exit Game", "alert");
                            VBox networkAlertBox = (VBox) p[0];
                            Button alertButton = (Button) p[1];
                            alertButton.setOnAction(e -> {
                                gameViewModel.quitGame();
                                gameController.close();
                                System.exit(0);
                            });
                            Platform.runLater(() -> gameController.showCustomWindow(networkAlertBox, 550, 350));
                        } else {
                            gameViewModel.myBooksChoice(gameController.getSelectedBooks());
                            gameViewModel.ready();

                            // loginFormBox.setDisable(true);
                            // osBar.setDisable(true);

                            // Waiting Window
                            gameController.showCustomWindow(fullWaitingBox, 500, 350);

                        }

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

        ObservableList<String> bookList = fullBookList ? ViewModel.getBookList()
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

    private HBox createSubmitButtons(String buttonText, double buttonWidth) {
        // Submit button
        Button connectButton = new Button(buttonText);
        connectButton.getStyleClass().add("blue-button");
        connectButton.setPrefHeight(50);
        connectButton.setPrefWidth(buttonWidth);

        // Go Back button
        Button goBackButton = new Button(symbols.get("back"));
        goBackButton.getStyleClass().add("green-button");
        goBackButton.setOnAction(e -> {
            gameController.close();
            if (isHost) {
                HostModel.get().stopHostServer();
            }
            gameController.gameLoginStage.close();
            gameController.showInitialWindow();
        });

        HBox submitButtons = new HBox(10, goBackButton, connectButton);
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

    private VBox createTextAlertBox(String title, String text, boolean isTextCenter) {
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
            Text gameServerTitle = new Text("Game server");
            gameServerTitle.setTextAlignment(TextAlignment.CENTER);
            gameServerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            Text contentLabel = new Text(
                    "The game server is responsible for checking whether\na word is legal in terms of the book dictionary.\nGame Server is uploaded and\npowered by Oracle Cloud\nOn Ubuntu 22.04 VM");
            contentLabel.setFont(new Font(18));
            contentLabel.setTextAlignment(TextAlignment.CENTER);
            Button connectionButton = new Button("Check Connection");
            connectionButton.getStyleClass().add("green-button");
            Text connectionField = new Text("");
            connectionField.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            connectionField.setTextAlignment(TextAlignment.CENTER);
            connectionButton.setOnAction(e -> {
                // HostModel hm = (HostModel) gameViewModel.gameModel;
                if (HostModel.get().isGameServerConnect()) {
                    connectionField.setFill(Color.GREEN);
                    connectionField.setText("Connected");
                    connectionButton.setDisable(true);
                } else {
                    connectionField.setFill(Color.RED);
                    connectionField.setText("Not Connected");
                }
            });

            VBox gameServerSettings = new VBox(10, gameServerTitle, contentLabel, connectionButton, connectionField);
            gameServerSettings.setAlignment(Pos.CENTER);

            // Custom port
            Text customPortTitle = new Text("Host server Port");
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
                            "Please enter a valid port number.\nThe port should be a number with a maximum 5 digits");
                    invalidPortTxt.setFill(Color.CRIMSON);
                }
            });

            VBox customPortSettings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton,
                    invalidPortTxt);
            customPortSettings.setAlignment(Pos.CENTER);

            // Whats my IP
            Text myIpTitle = new Text("My IP Address");
            myIpTitle.setTextAlignment(TextAlignment.CENTER);
            myIpTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            Text myIpText = new Text("Get your IP and send it to your friends");
            myIpText.setFont(new Font(18));
            myIpText.setTextAlignment(TextAlignment.CENTER);
            TextField myIpField = new TextField("");
            myIpField.setDisable(true);
            myIpField.getStyleClass().add("text-field");
            myIpField.setAlignment(Pos.CENTER);
            myIpField.setMaxWidth(220);

            // Create a "Copy" button
            Button copyButton = new Button(symbols.get("copy"));
            copyButton.getStyleClass().add("black-button");
            copyButton.setDisable(true);

            copyButton.setOnAction(e -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(myIpField.getText()); // Copy the text from the TextField
                clipboard.setContent(content);
                copyButton.setDisable(true);
            });

            HBox myIpBox = new HBox(10, myIpField, copyButton);
            myIpBox.setAlignment(Pos.CENTER);
            myIpBox.setPadding(new Insets(0, 0, 10, 0));

            CheckBox localIpCheckBox = new CheckBox("We're playing on the same network");
            localIpCheckBox.setTextFill(Color.DARKSLATEGREY);
            localIpCheckBox.setStyle("-fx-font-size: 18px;");
            localIpCheckBox.setSelected(true);
            localIpCheckBox.setOnAction(e -> {
                myIpField.setText("");
                myIpField.setDisable(true);
                copyButton.setDisable(true);
            });

            Button myIpButton = new Button("What's My IP");
            myIpButton.getStyleClass().add("yellow-button");

            myIpButton.setOnAction(e -> {
                myIpField.setDisable(false);
                // myIpField.setVisible(true);
                copyButton.setDisable(false);
                // copyButton.setVisible(true);
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

            VBox myIpSettings = new VBox(10, myIpTitle, myIpText, localIpCheckBox, myIpBox, myIpButton);
            myIpSettings.setAlignment(Pos.CENTER);

            // settings = new VBox(5, gameServerSettings, customPortSettings, myIpSettings);
            VBox[] switchList = { myIpSettings, customPortSettings, gameServerSettings };
            settings = createSwitchableHBox(switchList, false);

        } else {
            // Custom port
            Text customPortTitle = new Text("Host's Custom Port");
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

            VBox box = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton, invalidPortTxt);
            box.setAlignment(Pos.CENTER);
            settings = new HBox(box);

        }
        settings.setAlignment(Pos.CENTER);
        return settings;
    }

    private void checkHostConnection() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            String error = null;
            boolean connected = true;
            // Check for connection
            if (!HostModel.get().isGameServerConnect()) {
                error = descriptions.get("game-server-error");
                connected = false;
            } else if (!gameViewModel.isConnected()) {
                error = descriptions.get("host-server-error");
                connected = false;
            }

            if (!connected) {
                VBox networkAlertBox = createQuitAlertBox("Network Error", error, true);
                Platform.runLater(() -> gameController.showCustomWindow(networkAlertBox, 550, 350));
            }
        }).start();
    }

    public BorderPane createGameFlowBox() {
        BorderPane gameFlowBox = new BorderPane();

        // Create the sidebar
        VBox sidebar = createSidebar();
        gameFlowBox.setRight(sidebar);

        // Create the game board
        HBox gameBoardContainer = new HBox();
        GridPane boardGridPane = createGameBoard();
        boardGridPane.setMinSize(734, 734);
        boardGridPane.setMaxSize(734, 734);
        gameBoardContainer.getChildren().add(boardGridPane);
        gameBoardContainer.setPadding(new Insets(0, 0, 0, 60));

        // Create the buttons
        VBox buttons = createButtons();
        gameFlowBox.setLeft(buttons);

        gameFlowBox.setCenter(gameBoardContainer);

        return gameFlowBox;
    }

    private VBox createSidebar() {

        VBox sideBar = new VBox(15);
        // sideBar.setPadding(new Insets(15));
        sideBar.setPadding(new Insets(0, 40, 0, 0));
        sideBar.setAlignment(Pos.CENTER);

        // My Info Board
        Pane myInfoBoard = createPlayerInfoBoard(gameViewModel.myNameProperty().get(), null, false);
        sideBar.getChildren().add(myInfoBoard);

        // My Words
        VBox myWordsBox = new VBox();
        myWordsBox.setMinSize(280, 165);
        myWordsBox.setMaxSize(280, 165);
        myWordsBox.getStyleClass().add("my-words-box");

        Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
        ListView<String> wordsListView = new ListView<>();
        wordsListView.setMinSize(280, 160);
        wordsListView.setMaxSize(280, 160);
        wordsListView.setId("wordsListView");
        wordsListView.itemsProperty().bind(gameViewModel.myWordsProperty());
        wordsListView.setPrefSize(220, 100);
        wordsListView.setBackground(transparentBackground);

        Label wordsLabel = new Label("My Words");
        wordsLabel.setAlignment(Pos.CENTER);
        wordsLabel.getStyleClass().add("login-label");
        wordsLabel.setPadding(new Insets(40, 0, 0, 0));

        Pane wordsBox = new Pane(wordsListView);
        wordsBox.getStyleClass().add("my-words-box");
        myWordsBox.getChildren().add(wordsListView);
        sideBar.getChildren().addAll(wordsLabel, myWordsBox);

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

        // Other Player's Info Board
        VBox otherBox = new VBox(15);
        otherBox.setPadding(new Insets(15));
        otherBox.setAlignment(Pos.CENTER);

        for (String playerName : gameViewModel.othersInfoProperty().keySet()) {
            Pane otherInfoBoard = createPlayerInfoBoard(playerName,
                    gameViewModel.othersInfoProperty().get(playerName), true);
            otherBox.getChildren().add(otherInfoBoard);
        }

        sideBar.getChildren().add(otherBox);

        gameViewModel.othersInfoProperty().addListener((MapChangeListener<String, String>) change -> {
            Platform.runLater(() -> {
                otherBox.getChildren().clear();
                for (String playerName : gameViewModel.othersInfoProperty().keySet()) {
                    Pane otherInfoBoard = createPlayerInfoBoard(playerName,
                            gameViewModel.othersInfoProperty().get(playerName), true);
                    otherBox.getChildren().add(otherInfoBoard);
                }
                System.out.println("1. others info listener");
            });
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
        turnIndicator.setFitWidth(60);
        turnIndicator.setFitHeight(60);

        HBox nameBox = new HBox(10);
        nameBox.setMinWidth(160);
        nameBox.setAlignment(Pos.CENTER);
        if (playerTurn) {
            nameBox.getChildren().add(turnIndicator);
        }
        nameBox.getChildren().addAll(nameLabel);

        // Player's Score
        Label scoreLabel = new Label();
        if (isOtherPlayer) {
            scoreLabel.setText(String.valueOf(playerScore));
            if (playerScore < 0) {
                scoreLabel.setStyle("-fx-text-fill: red");
            } else {
                scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
            }
        } else {
            scoreLabel.textProperty().bind(gameViewModel.myScoreProperty().asString());
            // Negative score
            gameViewModel.myScoreProperty().addListener((observable, oldScore, newScore) -> {
                Platform.runLater(() -> {
                    if (newScore.intValue() < 0) {
                        scoreLabel.setStyle("-fx-text-fill: red");
                    } else {
                        scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
                        scoreLabel.getStyleClass()
                                .add("score-" + (newScore.intValue() > oldScore.intValue() ? "highlight" : "failed"));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                scoreLabel.getStyleClass().remove("score-"
                                        + (newScore.intValue() > oldScore.intValue() ? "highlight" : "failed"));
                            }

                        }, 1000);

                    }
                    // if (newValue.intValue() < 0) {
                    // // Animate the score change
                    // updatePlayerScoreWithAnimation(oldValue.intValue(), newValue.intValue());
                    // scoreLabel.setStyle("-fx-text-fill: red");
                    // } else {
                    // // Animate the score change
                    // updatePlayerScoreWithAnimation(oldValue.intValue(), newValue.intValue());
                    // scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
                    // }
                    System.out.println("2. score listener");
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

        if (isOtherPlayer) {
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
            VBox playerInfoBoard = new VBox(-10, nameBox, scoreBox);
            playerInfoBoard.getStyleClass().add("wood-score-board");
            playerInfoBoard.setMinSize(230, 130);
            playerInfoBoard.setMaxSize(230, 130);
            playerInfoBoard.setAlignment(Pos.CENTER);
            if (playerTurn) {
                playerInfoBoard.getStyleClass().add("glow-button");
            }
            gameViewModel.myTurnProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (newValue) {
                        nameBox.getChildren().add(0, turnIndicator);
                        playerInfoBoard.getStyleClass().add("glow-button");
                    } else {
                        nameBox.getChildren().remove(0);
                        playerInfoBoard.getStyleClass().remove("glow-button");
                    }
                    System.out.println("3. turn listener");
                });
            });

            return playerInfoBoard;
        }
    }

    public VBox createDrawTilesBox(String drawTilesString) {
        Label drawTilesLabel = new Label("Draw Tiles");
        drawTilesLabel.getStyleClass().add("title");
        drawTilesLabel.setStyle("-fx-font-size: 60; -fx-text-fill: mediumorchid; ");
        drawTilesLabel.setPadding(new Insets(10, 0, 20, 0));

        String firstName = drawTilesString.split(":")[0].split("-")[0];
        HBox players = new HBox();
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
            VBox playerBox = new VBox(15, nameText, tileButton);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPrefSize(100, 120);
            if (name.equals(firstName)) {
                playerBox.getStyleClass().add("glow-button");
                ImageView arrowIcon = new ImageView(icons.get("turn"));
                arrowIcon.setFitHeight(70);
                arrowIcon.setFitWidth(70);
                arrowIcon.getStyleClass().add("glow-button");
                HBox startingPlayerBox = new HBox(5, arrowIcon, playerBox);
                startingPlayerBox.setAlignment(Pos.BOTTOM_CENTER);
                players.getChildren().add(startingPlayerBox);
            } else {
                players.getChildren().add(playerBox);
            }
        }
        String firstString = firstName.equals(myName) ? "You play first!" : (firstName + " is playing first!");
        Text firstPlayerText = new Text(firstString);
        firstPlayerText.setTextAlignment(TextAlignment.CENTER);
        firstPlayerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        firstPlayerText.setFill(Color.LIGHTBLUE);
        VBox box = new VBox(40, drawTilesLabel, players, firstPlayerText);
        box.setPadding(new Insets(0, 0, 20, 0));
        box.setAlignment(Pos.CENTER);

        Node[] drawTilesNodes = createCustomBox(box, "green", "Let's Go", "drawtiles");
        VBox drawTilesBox = (VBox) drawTilesNodes[0];

        return drawTilesBox;
    }

    private List<Button> createTileButtons(List<Tile> tiles, boolean isSorted) {

        // Tile buttons
        List<Button> tileButtons = new ArrayList<>();

        if (isSorted) {
            Collections.sort(tiles, Comparator.comparing(Tile::getLetter));
        }
        for (Tile tile : tiles) {
            String letter = String.valueOf(tile.getLetter());
            Button tileButton = new Button();
            tileButton.setPrefSize(45, 45);
            tileButton.getStyleClass().add("tile-button");
            tileButton.setStyle("-fx-background-image: url('tiles/" + letter + ".png')");
            tileButton.getStyleClass().add("glow-button");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    tileButton.getStyleClass().remove("glow-button");
                }

            }, 300);

            tileButton.setOnAction(event -> {
                // Handle button click, similar to your existing code
                if (!gameController.getPlacementCelles().isEmpty()) {
                    isTilePlaced = true;
                    sortTilesButton.setDisable(true);
                    Pane cellPane = gameController.getPlacementCelles().pop();
                    cellPane.getStyleClass().add("character");
                    cellPane.getStyleClass().add("character-" + letter);
                    cellPane.setStyle("-fx-border-color: yellow; -fx-border-style: solid inside;");

                    // Disable the tile button upon selection
                    tileButton.setDisable(true);
                    resetTilesButton.setDisable(false);

                    // tilePane.getChildren().remove(tileButton);

                    // Add the tile value to the word
                    gameViewModel.addToWord(letter);
                }
            });

            tileButtons.add(tileButton);
        }

        return tileButtons;
    }

    private VBox createButtons() {
        VBox customRoot = new VBox(15);
        customRoot.setPadding(new Insets(20));
        customRoot.setAlignment(Pos.CENTER);

        // Tile buttons
        FlowPane tileButtonsPane = new FlowPane(10, 10);
        tileButtonsPane.setAlignment(Pos.CENTER);

        List<Tile> myTiles = gameViewModel.myTilesProperty();
        tileButtons = createTileButtons(myTiles, false);
        tileButtonsPane.getChildren().setAll(tileButtons);
        gameViewModel.myTilesProperty().addListener((obs, oldTiles, newTiles) -> {
            Platform.runLater(() -> {
                tileButtons = createTileButtons(gameViewModel.myTilesProperty(), false);
                tileButtonsPane.getChildren().setAll(tileButtons);
                System.err.println("4. tiles hanlder");
            });
        });

        boolean myTurn = gameViewModel.myTurnProperty().get();

        // Sort Tiles Button
        sortTilesButton = new Button(symbols.get("sort"));
        sortTilesButton.getStyleClass().add("lightblue-button");
        sortTilesButton.setDisable(!myTurn);
        sortTilesButton.setOnAction(e -> {
            tileButtons = createTileButtons(gameViewModel.myTilesProperty(), true);
            tileButtonsPane.getChildren().setAll(tileButtons);
        });

        // Reset Tiles button
        resetTilesButton = new Button(symbols.get("reset"));
        resetTilesButton.getStyleClass().add("grey-button");
        // resetTilesButton.setPrefWidth(60);
        // resetTilesButton.setPrefHeight(20);
        resetTilesButton.setDisable(true);
        resetTilesButton.setOnAction(event -> {
            this.isTilePlaced = false;
            sortTilesButton.setDisable(false);
            gameController.resetWordPlacement();
            resetTilesButton.setDisable(true);
        });

        HBox buttonBox = new HBox(10, sortTilesButton, resetTilesButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Tile buttons BOX
        VBox tileBox = new VBox(15, tileButtonsPane, buttonBox);
        tileBox.setAlignment(Pos.CENTER);

        // Pass turn Button
        Button passTurnButton = new Button("Pass Turn");
        passTurnButton.getStyleClass().add("yellow-button");
        passTurnButton.setStyle("-fx-font-size: 23px;");
        passTurnButton.setOnAction(e -> {
            gameViewModel.skipTurn();
            // gameBoard.setDisable(false);
        });

        // Challange Button
        Button challengeButton = new Button("Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setStyle("-fx-font-size: 23px;");
        challengeButton.setDisable(true);
        challengeButton.setOnAction(event -> {
            isTilePlaced = false;
            // gameBoard.setDisable(false);
            gameViewModel.challenge();
        });

        passTurnButton.setDisable(!myTurn);

        gameViewModel.myTurnProperty().addListener((observable, oldTurn, newTurn) -> {
            Platform.runLater(() -> {
                challengeButton.setDisable(true);
                passTurnButton.setDisable(!newTurn);
                resetTilesButton.setDisable(!newTurn);
                sortTilesButton.setDisable(!newTurn);
                System.out.println("5. turn button listener");
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
        tryPlaceWordButton.setOnAction(event -> {
            isTilePlaced = false;
            // gameBoard.setDisable(true);
            resetTilesButton.setDisable(true);
            sortTilesButton.setDisable(true);
            tryPlaceWordButton.setDisable(true);
            challengeButton.setDisable(false);

            String word = gameViewModel.getWord();

            gameViewModel.tryPlaceWord(word);

            // challengeButton.getStyleClass().add("glow-button");
            // passTurnButton.getStyleClass().add("glow-button");

            // gameViewModel.clearPlayerTiles();

            // Clear existing tile buttons
            // tilePane.getChildren().clear();
        });
        tryPlaceWordButton.setDisable(gameController.getPlacementCelles().isEmpty());

        VBox playButtonsBox = new VBox(15, tryPlaceWordButton, challengeButton, passTurnButton, quitGameButton);
        playButtonsBox.setAlignment(Pos.CENTER);
        playButtonsBox.setPadding(new Insets(60, 0, 20, 0));

        // Round Buttons
        HBox roundButtonsBox = createRoundButtons(true);
        roundButtonsBox.setPrefHeight(70);
        roundButtonsBox.setAlignment(Pos.BOTTOM_CENTER);

        // Round buttons pane
        Button helpButton = (Button) roundButtonsBox.getChildren().get(0);
        VBox helpBox = createGameInstuctionsBox();
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 900, 600));

        Button messageButton = (Button) roundButtonsBox.getChildren().get(1);
        Node[] messagesNodes = createCustomBox(createMessagesBox(), "red", symbols.get("exit"), "message");
        VBox messagesBox = (VBox) messagesNodes[0];
        messageButton.setOnAction(e -> gameController.showCustomWindow(messagesBox, 700, 370));

        // messageButton.setOnAction(e -> showMessageWindow("All"));

        customRoot.getChildren().addAll(tileBox, playButtonsBox, roundButtonsBox);

        return customRoot;
    }

    public VBox createGameInstuctionsBox() {
        VBox instructions1 = createTextAlertBox("Rules And Gameplay", descriptions.get("game-flow-rules"), false);
        VBox instructions2 = createTextAlertBox("How To Play", descriptions.get("game-flow-how-to-play"), false);
        /**************************** */
        VBox howToPlayBox = new VBox(15);
        howToPlayBox.setAlignment(Pos.CENTER);
        Label howToLabel = new Label("How To Play");
        howToLabel.getStyleClass().add("title");
        howToLabel.setStyle("-fx-font-size: 40; -fx-text-fill: black; ");
        // howToLabel.setPadding(new Insets(0, 0, 30, 0));

        Label selectCellLabel = new Label("Select Cells");
        selectCellLabel.getStyleClass().add("title");
        selectCellLabel.setStyle("-fx-font-size: 22;  -fx-text-fill: black;");
        ImageView selectCellsGif = new ImageView(new Image("instructions/select-cells.gif"));
        VBox firstBox = new VBox(20, selectCellLabel, selectCellsGif);
        firstBox.setAlignment(Pos.CENTER);
        firstBox.setPadding(new Insets(15, 15, 15, 15));

        Label selectTilesLabel = new Label("Select Tiles");
        selectTilesLabel.getStyleClass().add("title");
        selectTilesLabel.setStyle("-fx-font-size: 22;  -fx-text-fill: black;");
        ImageView selectTilesGif = new ImageView(new Image("instructions/select-tiles.gif"));
        VBox secondBox = new VBox(20, selectTilesLabel, selectTilesGif);
        secondBox.setAlignment(Pos.CENTER);
        secondBox.setPadding(new Insets(15, 15, 15, 15));

        Label tryButtonLabel = new Label("Click the Try Place Button");
        tryButtonLabel.getStyleClass().add("title");
        tryButtonLabel.setStyle("-fx-font-size: 22;  -fx-text-fill: black;");
        ImageView tryPlaceGif = new ImageView(new Image("instructions/try-place.gif"));
        VBox thirdBox = new VBox(20, tryButtonLabel, tryPlaceGif);
        thirdBox.setAlignment(Pos.CENTER);
        thirdBox.setPadding(new Insets(15, 15, 15, 15));

        Label placeWordLabel = new Label("Place Word");
        placeWordLabel.getStyleClass().add("title");
        placeWordLabel.setStyle("-fx-font-size: 22; -fx-text-fill: black;");
        ImageView placeWordGif = new ImageView(new Image("instructions/place-word.gif"));
        VBox fourthBox = new VBox(20, placeWordLabel, placeWordGif);
        fourthBox.setAlignment(Pos.CENTER);
        fourthBox.setPadding(new Insets(15, 15, 15, 15));

        VBox main = new VBox(10, firstBox, secondBox, thirdBox, fourthBox);
        main.setStyle("-fx-background-color: rgb(168, 79, 212);");
        main.setAlignment(Pos.CENTER);
        main.setPrefSize(600, 270);

        ScrollPane scrollPane = new ScrollPane(main);
        scrollPane.setMinViewportWidth(600); // Set the preferred width
        scrollPane.setMinViewportHeight(300); // Set the preferred height
        scrollPane.setStyle("-fx-background-color: rgb(168, 79, 212);");

        VBox[] list = { firstBox, secondBox, thirdBox, fourthBox };
        howToPlayBox.getChildren().addAll(howToLabel, scrollPane);

        VBox instructionsBox = new VBox(15);
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setPrefSize(600, 300);
        Label instructionsLabel = new Label("Rules And Gameplay");
        instructionsLabel.getStyleClass().add("title");
        instructionsLabel.setStyle("-fx-font-size: 40; -fx-text-fill: black; ");

        Text instructionsText = new Text(descriptions.get("game-flow-rules"));
        instructionsText.setFill(Color.WHITE);
        instructionsText.setFont(new Font(25));

        VBox main2 = new VBox(10, instructionsText);
        main2.setStyle("-fx-background-color: rgb(168, 79, 212);");
        main2.setAlignment(Pos.CENTER);
        main2.setPrefSize(600, 300);

        ScrollPane scrollPane2 = new ScrollPane(main2);
        scrollPane.setMinViewportWidth(600); // Set the preferred width
        scrollPane.setMinViewportHeight(300); // Set the preferred height
        scrollPane.setStyle("-fx-background-color: rgb(168, 79, 212);");

        instructionsBox.getChildren().addAll(instructionsLabel, scrollPane2);

        /**************************** */
        VBox[] switchList = { howToPlayBox, instructionsBox };
        HBox gameInstructionsBox = createSwitchableHBox(switchList, false);
        Node[] helpBoxNodes = createCustomBox(gameInstructionsBox, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpBoxNodes[0];

        return helpBox;
    }

    public List<Button> getTileButtons() {
        return tileButtons;
    }

    private GridPane createGameBoard() {
        gameBoard = new GridPane();
        gameBoard.getStyleClass().add("board-background");

        gameViewModel.currentBoardProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                gameController.resetWordPlacement();
                updateBoard(newValue);
                System.out.println("6. board listener");
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
                // cellPane.setStyle("-fx-border-color: yellow;");
                cellPane.setMinSize(48.5, 48.5);
                cellPane.setMaxSize(48.5, 48.5);
                // cellPane.setPrefSize(70, 70);
                gameBoard.add(cellPane, col, row);

                cellPane.setOnMouseClicked(event -> {
                    // Handle cell click events
                    handleCellClick(cellPane);
                });
            }
        }
    }

    private void updateBoard(Tile[][] board) {
        gameController.resetWordPlacement();
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

    private void handleCellClick(Pane cell) {
        // Not your turn
        if (!gameViewModel.myTurnProperty().get()) {
            VBox turnAlert = createTextAlertBox("It's Not Your Turn", "Wait for your turn to play", true);
            Node[] turnAlertNodes = createCustomBox(turnAlert, "blue", "OK", "alert");
            VBox turnAlertBox = (VBox) turnAlertNodes[0];
            gameController.showCustomWindow(turnAlertBox, 500, 250);
        } else {

            // Player's hasnt placed tiles yet
            if (!isTilePlaced) {
                gameController.getPlacementCelles().clear();
                gameController.getPlacementList().clear();
                //

                if (gameController.getSelectedCells().size() == 0) {
                    gameController.resetWordPlacement();
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
                        gameController.getSelectedCells().remove(cell);
                        cell.getStyleClass().remove("selected");
                    } else {
                        // clear all panes
                        gameController.resetWordPlacement();
                        // for (Pane cell : gameController.getSelectedCells()) {
                        // cell.getStyleClass().remove("selected");
                        // }
                        // gameController.getSelectedCells().clear();

                        // add the new one
                        gameController.getSelectedCells().add(cell);
                        cell.getStyleClass().add("selected");
                    }
                }

                // Enable/disable buttons based on the number of selected cells
                boolean enableButtons = gameController.getSelectedCells().size() == 2;
                tryPlaceWordButton.setDisable(!enableButtons);

                if (enableButtons) {
                    for (Button b : tileButtons) {
                        b.setDisable(false);
                        b.setOnMouseEntered(e -> b.getStyleClass().add("tile-button-hover"));
                        b.setOnMouseExited(e -> b.getStyleClass().remove("tile-button-hover"));
                    }
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
                // else {
                // gameViewModel.clearSelectedCells();
                // }
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

    private HBox createSwitchableHBox(VBox[] vBoxes, boolean isVertical) {
        HBox switchableHBox = new HBox();
        switchableHBox.setAlignment(Pos.CENTER);
        String buttonColor = isVertical ? "lightblue-button" : "brown-button";
        String firstButtonSymbol = isVertical ? symbols.get("up") : symbols.get("left");
        String secondButtonSymbol = isVertical ? symbols.get("down") : symbols.get("right");

        // Create buttons for switching
        Button firstButton = new Button(firstButtonSymbol);
        firstButton.getStyleClass().add(buttonColor);
        VBox prevBox = new VBox(firstButton);
        prevBox.setAlignment(Pos.CENTER);
        Button secondButton = new Button(secondButtonSymbol);
        secondButton.getStyleClass().add(buttonColor);
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

    public void highlightCellsForWords(List<Word> words, boolean success) {
        List<Pane> panes = new ArrayList<>();
        int cnt = 0;
        for (Word word : words) {
            int row = word.getRow();
            int col = word.getCol();
            boolean isVertical = word.isVertical();

            for (Tile t : word.getTiles()) {
                cnt++;
                Pane cellPane = (Pane) getCellFromBoard(row, col);
                if (cellPane != null) {
                    cellPane.getStyleClass().add(success ? "highlight" : "failed");
                    panes.add(cellPane);
                }

                if (isVertical) {
                    row++;
                } else {
                    col++;
                }
            }
        }
        System.out.println("highlight cells: " + cnt);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                panes.forEach(p -> p.getStyleClass().remove(success ? "highlight" : "failed"));
            }

        }, success ? 1000 : 3000);
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

        TextField messageField = new TextField();
        messageField.getStyleClass().add("text-field");
        messageField.setMinWidth(500);
        messageField.setMaxWidth(500);
        messageField.setOnMouseClicked(e -> messageField.setText(""));
        players.setOnMouseClicked(e -> messageField.setText(""));

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("darkgreen-button");
        sendButton.setOnAction(e -> {
            gameController.closeCustomWindow();
            String to = players.getValue();
            String message = messageField.getText();
            if (message.length() > 0) {
                if (to.equals("All")) {
                    gameViewModel.sendToAll(message);
                } else {
                    gameViewModel.sendTo(to, message);
                }
            }
        });

        HBox inputBox = new HBox(10, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);

        messagesBox.getChildren().addAll(sendToBox, inputBox);
        messagesBox.setAlignment(Pos.CENTER);

        return messagesBox;
    }

    public VBox createMessageAlertBox(String sender, String message, boolean toAll) {
        String title = sender + " Sent" + (toAll ? " to all:" : ":");

        Text messageTitle = new Text(title);
        messageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        // messageTitle.setStyle("-fx-fill: antiquewhite;");

        Text messageText = new Text(message);
        messageText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        // messageText.setStyle("-fx-fill: antiquewhite;");
        // messageText.setTextAlignment(TextAlignment.CENTER);

        VBox messageBox = new VBox(20, messageTitle, messageText);
        messageBox.setAlignment(Pos.CENTER);

        Node[] messageNodes = createCustomBox(messageBox, "blue", "close", "message");
        VBox messageWindowBox = (VBox) messageNodes[0];

        return messageWindowBox;
    }

    public VBox createChallangeBox(List<Word> illegalWords, boolean isChallange) {
        VBox box = null;
        String words = "";
        for (int i = 0; i < illegalWords.size(); i++) {
            words += illegalWords.get(i).toString();
            if (i != illegalWords.size() - 1) {
                words += ",";
            }
        }
        if (isChallange) {
            box = createTextAlertBox("Challange Failed",
                    "One of these words: " + words + "\nis not found in the game books\n\nYou lost 10 points.",
                    true);
        } else {
            box = createTextAlertBox(words,
                    "One of these words is not dictionary legal\nYou can try Challange or Pass turn.", true);
        }
        Node[] nodes = createCustomBox(box, "blue", "OK", "alert");
        VBox iligalWordsBox = (VBox) nodes[0];

        return iligalWordsBox;
    }

    public VBox createIllegalMoveBox() {
        VBox illegalMoveBox = createTextAlertBox("Illegal Word Placement", descriptions.get("not-board-legal"), true);
        Button helpButton = new Button(symbols.get("help"));
        helpButton.getStyleClass().add("purple-button");
        helpButton.setOnAction(e -> {
            gameController.resetWordPlacement();
            gameController.showCustomWindow(createGameInstuctionsBox(), 600, 600);
        });
        illegalMoveBox.getChildren().add(helpButton);

        Node[] nodes = createCustomBox(illegalMoveBox, "green", "Try Again", "alert");
        Button tryAgainButton = (Button) nodes[1];
        tryAgainButton.setOnAction(e -> {
            gameController.resetWordPlacement();
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
            Label hostQuitLabel = new Label("The host has quit the game");
            hostQuitLabel.setStyle("-fx-text-fill: red; ");
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
        winnerLabel.setPadding(new Insets(80, 0, 0, 0));
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
        winnerScoreIcon.setFitWidth(50);
        winnerScoreIcon.setFitHeight(50);
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
            gameViewModel.quitGame();
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

    public Image getGameIcon() {
        return icons.get("game");
    }

    public void updatePlayerScoreWithAnimation(int oldScore, int newScore) {
        int scoreChange = newScore - oldScore;

        if (scoreChange != 0) {
            // Create a Timeline for the animation
            Timeline timeline = new Timeline();

            // Define the duration for the animation (e.g., 1 second)
            Duration duration = Duration.seconds(0);

            // Create a KeyValue for the score property
            KeyValue keyValue = new KeyValue(gameViewModel.myScoreProperty(), oldScore);

            // Create a KeyFrame with the duration and key value
            KeyFrame keyFrame = new KeyFrame(duration, keyValue);

            // Add the KeyFrame to the Timeline
            timeline.getKeyFrames().add(keyFrame);

            // Set up an event handler to update the score label during animation
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // The animation has finished
                    // You don't need to update the label explicitly; it will be updated
                    // automatically due to the binding
                }
            });

            // Play the animation
            timeline.play();
        }
        // No need to update the label explicitly, as it will be updated automatically
        // through the binding
    }

    public boolean isHost() {
        return isHost;
    }
}
