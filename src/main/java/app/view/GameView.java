package app.view;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import app.model.game.Tile;
import app.model.game.Word;
import app.model.host.HostModel;
import app.view_model.MessageReader;
import app.view_model.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
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
        icons.put("turn", new Image("icons/arrow-icon2.png"));
        icons.put("score", new Image("icons/star-icon.png"));
        icons.put("bag", new Image("icons/bag-icon.png"));
        this.symbols = new HashMap<>();
        symbols.put("exit", "\uD83D\uDDD9");
        symbols.put("minimize", "\uD83D\uDDD5");
        symbols.put("settings", "\uD83D\uDD27");
        symbols.put("help", "❓");
        symbols.put("messages", "\uD83D\uDCE8");
        symbols.put("back", "\u2B05");
        symbols.put("prev", "⮜");
        symbols.put("next", "⮞");
        symbols.put("copy", "📋");
        symbols.put("sort", "⭰");
        symbols.put("reset", "\u2B6F"); // \u2B6F ⟲ ⭯
        this.descriptions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\explanations.txt"))) {
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
            gameController.gameSetupStage.close();
            gameController.showLoginForm(isHost);
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
            gameController.gameSetupStage.close();
            gameController.showLoginForm(isHost);
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
        Text guestModeTitle = new Text(mode + " Mode");
        guestModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        String descMode = isHost ? "host" : "guest";
        Text guestModeText = new Text(descriptions.get("game-mode-" + descMode));
        guestModeText.getStyleClass().add("content-label");
        guestModeText.setTextAlignment(TextAlignment.CENTER);
        VBox guestModeExp = new VBox(10, guestModeTitle, guestModeText);
        guestModeExp.setAlignment(Pos.CENTER);
        Node[] helpNodes = createCustomBox(guestModeExp, "red", symbols.get("exit"), "help");
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
                            VBox networkAlert = createAlertBox("Network Error", descriptions.get("guest-socket-error"),
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
        buttonPane.setPadding(new Insets(0, 0, 15, 0));

        rootContainer.getChildren().addAll(scrollPane, buttonPane);

        return rootContainer;
    }

    private HBox createRoundButtons(boolean isGameFlow) {
        HBox roundButtonsPane = new HBox(10);
        roundButtonsPane.setAlignment(Pos.CENTER);
        roundButtonsPane.setPadding(new Insets(30, 0, 0, 0));
        roundButtonsPane.setMinHeight(70);
        roundButtonsPane.setPrefHeight(70);

        // Settings/Messages
        String symbol = isGameFlow ? symbols.get("messages") : symbols.get("settings");
        String color = isGameFlow ? "olive" : "grey";
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
            if (isHost) {
                HostModel.get().stopHostServer();
            }
            gameController.gameSetupStage.close();
            gameController.showInitialWindow();
        });

        HBox submitButtons = new HBox(10, goBackButton, connectButton);
        submitButtons.setPadding(new Insets(30, 0, 10, 0));
        submitButtons.setAlignment(Pos.CENTER);

        return submitButtons;
    }

    protected VBox createQuitBox() {
        VBox quitAlertBox = createAlertBox("Quit Game", "Are you sure you want to quit game?", true);

        Button yesButton = new Button("Yes");
        yesButton.getStyleClass().add("blue-button");
        yesButton.setOnAction(event -> {
            gameViewModel.quitGame();
            gameController.closeCustomWindow();

            if (!gameViewModel.isGameEnd()) {
                gameController.close();
                System.exit(0);
            } else {
                gameController.closeCustomWindow();
                VBox waitingBox = createWaitingBox("Waiting for all players to disconnect...");
                Node[] boxNodes = createCustomBox(waitingBox, "blue", "OK", "alert");
                VBox waitingAlertBox = (VBox) boxNodes[0];
                gameController.showCustomWindow(waitingAlertBox, 500, 350);
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

    public HBox getOsBar() {
        return osBar;
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

    public VBox createAlertBox(String title, String error, boolean isTextCenter) {
        Text alertTitle = new Text(title);
        alertTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text alertErrorText = new Text(error);
        alertErrorText.getStyleClass().add("content-label");
        if (isTextCenter)
            alertErrorText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(20, alertTitle, alertErrorText);
        alertBox.setAlignment(Pos.CENTER);

        return alertBox;
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

            VBox myIpSettings = new VBox(10, myIpTitle, myIpText, myIpBox, localIpCheckBox, myIpButton);
            myIpSettings.setAlignment(Pos.CENTER);

            // settings = new VBox(5, gameServerSettings, customPortSettings, myIpSettings);
            VBox[] switchList = { myIpSettings, customPortSettings, gameServerSettings };
            settings = createSwitchableHBox(switchList);

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
                error = descriptions.get("game-server-error");
                connected = false;
            }

            if (!connected) {
                VBox networkAlert = createAlertBox("Network Error", error, true);
                Node[] nodes = createCustomBox(networkAlert, "red", "Exit Game", "alert");
                VBox networkAlertBox = (VBox) nodes[0];
                Button alertButton = (Button) nodes[1];
                alertButton.setOnAction(e -> {
                    gameViewModel.quitGame();
                    gameController.close();
                    System.exit(0);
                });
                Platform.runLater(() -> gameController.showCustomWindow(networkAlertBox, 550, 350));

            }
        }).start();
    }

    public BorderPane createGameFlowBox() {
        BorderPane gameFlowBox = new BorderPane();

        // Create the sidebar
        VBox sidebar = createSidebar();
        gameFlowBox.setRight(sidebar);

        sidebar.setPadding(new Insets(0, 50, 0, 0));

        // Create the game board
        GridPane boardGridPane = createGameBoard();
        boardGridPane.setMinSize(734, 734); // Set the desired minimum size
        boardGridPane.setMaxSize(734, 734); // Set the desired maximum size

        gameFlowBox.setCenter(boardGridPane);

        // Create the buttons at the bottom
        VBox buttons = createButtons();

        gameFlowBox.setLeft(buttons);

        return gameFlowBox;
    }

    private VBox createSidebar() {

        VBox sideBar = new VBox(15);
        sideBar.setPadding(new Insets(15));
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
        bookListButton.getStyleClass().add("gold-button");
        bookListButton.setOnAction(e -> gameController.showBookSelectionWindow(false));
        HBox gameBooksBox = new HBox(bookListButton);
        gameBooksBox.setAlignment(Pos.CENTER);

        VBox bookBagBox = new VBox(10, bagCountBox, gameBooksBox);
        bookBagBox.setPadding(new Insets(40, 0, 50, 0));

        sideBar.getChildren().add(bookBagBox);

        return sideBar;
    }

    private Pane createMyInfoBoard(StringProperty myName, IntegerProperty myScore, BooleanProperty myTurn) {

        // My Name
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(myName);
        nameValueLabel.getStyleClass().add("my-name-label");

        // Turn indicator
        ImageView turnIndicator = new ImageView(icons.get("turn"));
        turnIndicator.setFitWidth(60);
        turnIndicator.setFitHeight(60);
        // turnIndicator.setVisible(myTurn.get());

        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER);
        if (myTurn.get()) {
            nameBox.getChildren().add(turnIndicator);
        }
        nameBox.getChildren().addAll(nameValueLabel);

        // My Score
        Label scoreLabel = new Label();
        scoreLabel.textProperty().bind(gameViewModel.myScoreProperty().asString());
        scoreLabel.getStyleClass().add("score-label");
        scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
        // Negative score
        myScore.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue.intValue() < 0) {
                    scoreLabel.setStyle("-fx-text-fill: red");
                }
                System.out.println("2. score listener");
            });
        });
        ///
        ImageView scoreIndicator = new ImageView(icons.get("score"));
        scoreIndicator.setFitWidth(50);
        scoreIndicator.setFitHeight(50);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIndicator, scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        VBox myInfoBoard = new VBox(-10, nameBox, scoreBox);
        myInfoBoard.getStyleClass().add("wood-score-board");
        myInfoBoard.setMinSize(230, 130);
        myInfoBoard.setMaxSize(230, 130);
        myInfoBoard.setAlignment(Pos.CENTER);

        if (myTurn.get()) {
            myInfoBoard.getStyleClass().add("glow-button");
        }

        myTurn.addListener((observable, oldValue, newValue) -> {
            // System.out.println("\nmy turn listener\n");
            Platform.runLater(() -> {
                if (newValue) {
                    // turnIndicator.setVisible(true);
                    nameBox.getChildren().add(0, turnIndicator);
                    myInfoBoard.getStyleClass().add("glow-button");
                } else {
                    // turnIndicator.setVisible(false);
                    nameBox.getChildren().remove(0);
                }
                System.out.println("3. turn listener");
            });
        });

        return myInfoBoard;
    }

    private Pane createOtherInfoBoard(String playerName, String playerInfo) {
        int playerScore = Integer.parseInt(playerInfo.split(":")[0]);
        boolean playerTurn = Boolean.parseBoolean(playerInfo.split(":")[1]);

        // Player's Name
        Label nameValueLabel = new Label(playerName);
        nameValueLabel.getStyleClass().add("other-name-label");

        // Turn indicator
        ImageView turnIndicator = new ImageView(icons.get("turn"));
        turnIndicator.setFitWidth(60); // Set the desired width
        turnIndicator.setFitHeight(60);
        turnIndicator.setVisible(playerTurn);

        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER);
        nameBox.getChildren().addAll(turnIndicator, nameValueLabel);

        // Player Score
        Label scoreLabel = new Label(String.valueOf(playerScore));
        scoreLabel.getStyleClass().add("other-score-label");
        if (playerScore < 0) {
            scoreLabel.setStyle("-fx-text-fill: red");
        } else {
            scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
        }

        ImageView scoreIndicator = new ImageView(icons.get("score"));
        scoreIndicator.setFitWidth(20);
        scoreIndicator.setFitHeight(20);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIndicator, scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        HBox otherInfoBoard = new HBox(10, nameBox, scoreBox);
        otherInfoBoard.getStyleClass().add("other-score-board");
        if (playerTurn) {
            otherInfoBoard.getStyleClass().add("glow-button");
        }
        otherInfoBoard.setMinSize(230, 60);
        otherInfoBoard.setMaxSize(230, 60);
        otherInfoBoard.setAlignment(Pos.CENTER);

        return otherInfoBoard;
    }

    private Pane createPlayerInfoBoard(String playerName, String playerInfo, boolean isOtherPlayer) {
        int playerScore = isOtherPlayer ? Integer.parseInt(playerInfo.split(":")[0]) : 0;
        boolean playerTurn = isOtherPlayer ? Boolean.parseBoolean(playerInfo.split(":")[1])
                : gameViewModel.myTurnProperty().get();

        // Player's Name
        Label nameLabel = new Label(playerName);
        nameLabel.getStyleClass().add(isOtherPlayer ? "other-name-label" : "my-name-label");

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
            scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
            // Negative score
            gameViewModel.myScoreProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (newValue.intValue() < 0) {
                        scoreLabel.setStyle("-fx-text-fill: red");
                    } else {
                        scoreLabel.setStyle("-fx-text-fill: rgb(0, 174, 255);");
                    }
                    System.out.println("2. score listener");
                });
            });
        }
        scoreLabel.getStyleClass().add(isOtherPlayer ? "other-score-label" : "score-label");

        ImageView scoreIndicator = new ImageView(icons.get("score"));
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
                    }
                    System.out.println("3. turn listener");
                });
            });

            return playerInfoBoard;
        }
    }

    public VBox createDrawTilesBox(String drawTilesString) {
        Text drawTilesTitle = new Text("Draw Tiles");
        drawTilesTitle.getStyleClass().add("title");
        String firstName = drawTilesString.split(":")[0].split("-")[0];
        HBox players = new HBox();
        players.setAlignment(Pos.CENTER);
        for (String s : drawTilesString.split("_")) {
            // this.totalPlayersNum++;
            String name = s.split("-")[0];
            String letter = s.split("-")[1];
            Text nameText = new Text(name);
            nameText.getStyleClass().add("little-title");
            nameText.setFill(Color.BROWN);
            nameText.setTextAlignment(TextAlignment.CENTER);
            Button tileButton = new Button();
            tileButton.setPrefSize(45, 45);
            tileButton.getStyleClass().add("tile-button");
            tileButton.setStyle("-fx-background-image: url('tiles/" + letter + ".png');");
            VBox playerBox = new VBox(15, nameText, tileButton);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPrefSize(100, 100);
            players.getChildren().add(playerBox);
        }
        String firstString = firstName.equals(myName) ? "You play first!" : (firstName + " is playing first!");
        Text firstPlayerText = new Text(firstString);
        firstPlayerText.setTextAlignment(TextAlignment.CENTER);
        firstPlayerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        VBox box = new VBox(40, drawTilesTitle, players, firstPlayerText);
        box.setAlignment(Pos.CENTER);

        Node[] drawTilesNodes = createCustomBox(box, "darkgreen", "Let's Go", "drawtiles");
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
        passTurnButton.setOnAction(e -> gameViewModel.skipTurn());

        // Challange Button
        Button challengeButton = new Button("Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setStyle("-fx-font-size: 23px;");
        challengeButton.setDisable(true);
        challengeButton.setOnAction(event -> {
            isTilePlaced = false;
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
        VBox instructions1 = createAlertBox("Rules And Gameplay", descriptions.get("game-flow-rules"), false);
        VBox instructions2 = createAlertBox("How To Play", descriptions.get("game-flow-how-to-play"), true);
        // VBox instructionsBox = new VBox(10, instructions1, instructions2);
        VBox[] switchList = { instructions1, instructions2 };
        HBox instructionsBox = createSwitchableHBox(switchList);
        // helpBox.setAlignment(Pos.CENTER);
        Node[] helpBoxNodes = createCustomBox(instructionsBox, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpBoxNodes[0];
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 1100, 900));

        Button messageButton = (Button) roundButtonsBox.getChildren().get(1);
        Node[] messagesNodes = createCustomBox(createMessagesBox(), "red", symbols.get("exit"), "message");
        VBox messagesBox = (VBox) messagesNodes[0];
        messageButton.setOnAction(e -> gameController.showCustomWindow(messagesBox, 700, 370));

        // messageButton.setOnAction(e -> showMessageWindow("All"));

        customRoot.getChildren().addAll(tileBox, playButtonsBox, roundButtonsBox);

        return customRoot;
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

        HBox box = new HBox(gameBoard);
        box.setPadding(new Insets(0, 50, 0, 0));

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
    
        if (gameController.getTurnWords().size() > 0) {
            highlightCellsForWords(gameController.getTurnWords());
        }
    }

    private void handleCellClick(Pane cell) {
            // Not your turn
                    if (!gameViewModel.myTurnProperty().get()) {
                        VBox turnAlert = createAlertBox("Not Your Turn", "Wait for your turn to play", true);
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

    private HBox createSwitchableHBox(VBox[] vBoxes) {
        HBox switchableHBox = new HBox();
        switchableHBox.setAlignment(Pos.CENTER);

        // Create buttons for switching
        Button prevButton = new Button(symbols.get("prev"));
        prevButton.getStyleClass().add("brown-button");
        VBox prevBox = new VBox(prevButton);
        prevBox.setAlignment(Pos.CENTER);
        Button nextButton = new Button(symbols.get("next"));
        nextButton.getStyleClass().add("brown-button");
        VBox nextBox = new VBox(nextButton);
        nextBox.setAlignment(Pos.CENTER);

        // Initialize the index to 0
        int[] currentIndex = { 0 };

        // Create a StackPane to hold the VBox in the middle with a fixed size
        StackPane middleContainer = new StackPane();
        middleContainer.setPrefSize(450, 250); // Set your desired fixed size here
        middleContainer.getChildren().add(vBoxes[currentIndex[0]]);

        // Handle switching to the previous VBox
        prevButton.setOnAction(event -> {
            middleContainer.getChildren().remove(vBoxes[currentIndex[0]]);
            currentIndex[0] = (currentIndex[0] - 1 + vBoxes.length) % vBoxes.length;
            middleContainer.getChildren().add(vBoxes[currentIndex[0]]);
        });

        // Handle switching to the next VBox
        nextButton.setOnAction(event -> {
            middleContainer.getChildren().remove(vBoxes[currentIndex[0]]);
            currentIndex[0] = (currentIndex[0] + 1) % vBoxes.length;
            middleContainer.getChildren().add(vBoxes[currentIndex[0]]);
        });

        switchableHBox.getChildren().addAll(prevBox, middleContainer, nextBox);
        return switchableHBox;
    }

    public void highlightCellsForWords(List<Word> words) {
        Platform.runLater(()->{
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
                        cellPane.getStyleClass().add("highlight");
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
                    panes.forEach(p -> p.getStyleClass().remove("highlight"));
                }

            }, 1000);
        });
    }

    public VBox createMessagesBox() {
        VBox messagesBox = new VBox(30);

        HBox sendToBox = new HBox(10);

        Text sendText = new Text("Send Message to:");
        sendText.getStyleClass().add("title");
        sendText.setStyle("-fx-fill: antiquewhite;");
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

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("darkgreen-button");
        sendButton.setOnAction(e -> {
            gameController.closeCustomWindow();
            String to = players.getValue();
            if (to.equals("All")) {
                gameViewModel.sendToAll(messageField.getText());
            } else {
                gameViewModel.sendTo(to, messageField.getText());
            }
        });

        HBox inputBox = new HBox(10, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);

        messagesBox.getChildren().addAll(sendToBox, inputBox);
        messagesBox.setAlignment(Pos.CENTER);

        return messagesBox;
    }

    public VBox createMessageAlert(String sender, String message, boolean toAll) {
        String title = sender + " Sent" + (toAll ? " to all:" : ":");

        Text messageTitle = new Text(title);
        messageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        messageTitle.setStyle("-fx-fill: antiquewhite;");

        Text messageText = new Text(message);
        messageText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        messageText.setStyle("-fx-fill: antiquewhite;");
        // messageText.setTextAlignment(TextAlignment.CENTER);

        VBox messageBox = new VBox(20, messageTitle, messageText);
        messageBox.setAlignment(Pos.CENTER);

        Node[] messageNodes = createCustomBox(messageBox, "blue", "close", "message");
        VBox messageWindowBox = (VBox) messageNodes[0];

        return messageWindowBox;
    }

    public VBox createIlegalWordBox(String iligalWords) {
        VBox box = null;
        if (iligalWords == null || iligalWords.equals("")) {
            box = createAlertBox("Challange Failed", "You lost 10 points.", true);
        } else {
            box = createAlertBox(iligalWords,
                    "One of these words is not dictionary legal\nYou can try Challange or Pass turn.", true);
        }
        Node[] nodes = createCustomBox(box, "blue", "OK", "alert");
        VBox iligalWordsBox = (VBox) nodes[0];

        return iligalWordsBox;
    }

    public Image getGameIcon() {
        return icons.get("game");
    }
}
