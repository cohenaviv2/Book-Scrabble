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
    public final String styleSheet;
    public final Image gameIcon;
    public final Image turnIcon;
    public final Image scoreIcon;
    public final Image bagIcon;
    //
    private GridPane gameBoard;
    private List<Button> tileButtons;
    private List<String> selectedBooks;
    private List<Word> turnWords = new ArrayList<>();

    private Button tryPlaceWordButton;
    private Button resetTilesButton;
    private Button sortTilesButton;
    //
    private HBox osBar;
    //
    private String myName;
    private int customPort = 0;

    public GameView(ViewModel viewModel, GameController gameController) {
        this.gameViewModel = viewModel;
        this.gameController = gameController;
        // Initialize and set up the initial UI components here.
        this.selectedBooks = new ArrayList<>();
        this.styleSheet = getClass().getResource("/style.css").toExternalForm();
        this.gameIcon = new Image("icons/game-icon.png");
        this.turnIcon = new Image("icons/arrow-icon2.png");
        this.scoreIcon = new Image("icons/star-icon.png");
        this.bagIcon = new Image("icons/bag-icon.png");
        this.symbols = new HashMap<>();
        symbols.put("exit", "\uD83D\uDDD9");
        symbols.put("minimize", "\uD83D\uDDD5");
        symbols.put("settings", "\uD83D\uDD27");
        symbols.put("help", "❓");
        symbols.put("messages", "\uD83D\uDCE8");
        symbols.put("back", "\u2B05");
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
        // osBar.getStyleClass().add("os-bar");

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
        Button helpBoxButton = (Button) helpBoxNodes[1];
        helpBoxButton.setOnAction(e -> gameController.closeCustomWindow());
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
        String tempName = generateRandomChar() + generateRandomChar() + generateRandomChar();
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
        Button helpBoxButton = (Button) helpNodes[1];
        helpBoxButton.setOnAction(e -> gameController.closeCustomWindow());
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 700, 400));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        VBox settings = createSettingBox();
        Node[] settingBoxNodes = createCustomBox(settings, "red", symbols.get("exit"), "settings");
        VBox settingsBox = (VBox) settingBoxNodes[0];
        Button settingsBoxButton = (Button) settingBoxNodes[1];
        settingsBoxButton.setOnAction(e -> gameController.closeCustomWindow());
        double height = isHost ? 850 : 550;
        settingsButton.setOnAction(e -> gameController.showCustomWindow(settingsBox, 600, height));

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
                    if (selectedBooks.size() == 0) {
                        booksButton.getStyleClass().add("error-field");
                    } else {
                        startGameButton.setDisable(true);
                        gameViewModel.setTotalPlayersCount(numOfPlayersComboBox.getValue());
                        gameViewModel.connectMe(myName, "0", customPort);
                        gameViewModel.myBooksChoice(selectedBooks);
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
            HBox submitButtons = createSubmitButtons("Connect", 140);
            Button connectButton = (Button) submitButtons.getChildren().get(1);
            loginFormBox.getChildren().add(submitButtons);

            connectButton.setOnAction(event -> {
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
                    if (selectedBooks.size() == 0) {
                        booksButton.getStyleClass().add("error-field");
                    } else {
                        connectButton.setDisable(true);
                        if (customPort == 0) {
                            customPort = HostModel.HOST_SERVER_PORT;
                        }
                        gameViewModel.connectMe(myName, ip, customPort);

                        loginFormBox.setDisable(true);
                        osBar.setDisable(true);

                        if (!gameViewModel.isConnected()) {
                            VBox networkAlert = createAlertBox("Network Error", descriptions.get("guest-socket-error"));
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
                            gameViewModel.myBooksChoice(selectedBooks);
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

    public VBox createBookBox(boolean fullBookList) {
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
                if (selectedBooks.contains(book)) {
                    imageView.getStyleClass().add("selected-book-image");
                }
                bookBox.setOnMouseClicked(event -> {
                    if (selectedBooks.contains(book)) {
                        selectedBooks.remove(book);
                        imageView.getStyleClass().remove("selected-book-image");
                    } else {
                        selectedBooks.add(book);
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
            // VBox initialWindowBox = createInitialWindow();
            // Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);
            // initialWindowBox.setCursor(Cursor.HAND);
            // initialWindowBox.getStylesheets().add(CSS_STYLESHEET);
            // this.primaryStage.setScene(initialWindowScene);
            gameController.gameSetupStage.close();
            gameController.showInitialWindow();
        });

        HBox submitButtons = new HBox(10, goBackButton, connectButton);
        submitButtons.setPadding(new Insets(30, 0, 10, 0));
        submitButtons.setAlignment(Pos.CENTER);

        return submitButtons;
    }

    protected VBox createQuitBox() {
        VBox quitAlertBox = createAlertBox("Quit Game", "Are you sure you want to quit game?");

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
                Button alerButton = (Button) boxNodes[1];
                alerButton.setOnAction(e -> gameController.closeCustomWindow());
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
                gameController.customStage.close();
            });

            StackPane buttonPane = new StackPane(button);
            BorderPane.setAlignment(buttonPane, Pos.TOP_CENTER);
            buttonPane.setPadding(new Insets(30, 0, 0, 0)); // Add padding to the top

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

    private VBox createAlertBox(String title, String error) {
        Text alertTitle = new Text(title);
        alertTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text alertErrorText = new Text(error);
        alertErrorText.getStyleClass().add("content-label");
        alertErrorText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(20, alertTitle, alertErrorText);
        alertBox.setAlignment(Pos.CENTER);

        return alertBox;
    }

    private VBox createSettingBox() {
        VBox settings = null;

        if (isHost) {
            // Game server connection check
            Text gameServerTitle = new Text("Game server");
            gameServerTitle.setTextAlignment(TextAlignment.CENTER);
            gameServerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            Text contentLabel = new Text(
                    "The game server is responsible for checking whether\na word is legal in terms of the book dictionary.\nGame Server is uploaded and\npowered by Oracle Cloud\nOn Ubuntu 22.04 VM");
            contentLabel.setFont(new Font(16));
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
            customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            Text customPortLabel = new Text(
                    "Host server runs by default on port 8040\n You can choose a custom port:");
            customPortLabel.setFont(new Font(16));
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

            VBox customPortSettings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton,
                    invalidPortTxt);
            customPortSettings.setAlignment(Pos.CENTER);

            // Whats my IP
            Text myIpTitle = new Text("What's My IP?");
            myIpTitle.setTextAlignment(TextAlignment.CENTER);
            myIpTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            Text myIpText = new Text("Get your IP address and send it to your friends");
            myIpText.setFont(new Font(16));
            myIpText.setTextAlignment(TextAlignment.CENTER);
            TextField myIpField = new TextField("");
            myIpField.setDisable(true);
            myIpField.getStyleClass().add("text-field");
            myIpField.setAlignment(Pos.CENTER);
            myIpField.setMaxWidth(220);

            CheckBox localIpCheckBox = new CheckBox("We're playing on the same network");
            localIpCheckBox.setTextFill(Color.GREEN);
            localIpCheckBox.setStyle("-fx-font-size: 14px;");
            localIpCheckBox.setSelected(true);
            localIpCheckBox.setOnAction(e -> {
                myIpField.setText("");
                myIpField.setDisable(true);
            });

            Button myIpButton = new Button("Get My IP");
            myIpButton.getStyleClass().add("yellow-button");

            myIpButton.setOnAction(e -> {
                myIpField.setDisable(false);
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

            VBox myIpSettings = new VBox(10, myIpTitle, myIpText, localIpCheckBox, myIpButton, myIpField);
            myIpSettings.setAlignment(Pos.CENTER);

            settings = new VBox(5, gameServerSettings, customPortSettings, myIpSettings);

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

            settings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton, invalidPortTxt);

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
                VBox networkAlert = createAlertBox("Network Error", error);
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

        // root.setBackground(gameBackground);
        // gameFlowBox.getStyleClass().add("game-flow-background");

        // Create the sidebar
        VBox sidebar = createSidebar();
        gameFlowBox.setRight(sidebar);

        // Create the game board
        GridPane boardGridPane = createBoard();
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
        Pane myInfoBoard = createMyInfoBoard(gameViewModel.myNameProperty(),
                gameViewModel.myScoreProperty(), gameViewModel.myTurnProperty());
        sideBar.getChildren().add(myInfoBoard);

        // My Words
        Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
        ListView<String> wordsListView = new ListView<>();
        wordsListView.itemsProperty().bind(gameViewModel.myWordsProperty());
        wordsListView.setPrefSize(220, 190);
        wordsListView.setBackground(transparentBackground);

        Label wordsLabel = new Label("My Words:");
        wordsLabel.setAlignment(Pos.CENTER);
        wordsLabel.getStyleClass().add("login-label");

        Pane wordsBox = new Pane(wordsListView);
        wordsBox.getStyleClass().add("my-words-box");
        sideBar.getChildren().addAll(wordsLabel, wordsBox);

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

        // Other players info
        Label othersScoreLabel = new Label("Other Players:");
        othersScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox otherBox = new VBox(15);
        otherBox.setPadding(new Insets(15));
        otherBox.setAlignment(Pos.CENTER);

        for (String playerName : gameViewModel.othersInfoProperty().keySet()) {
            Pane otherInfoBoard = createOtherInfoBoard(playerName,
                    gameViewModel.othersInfoProperty().get(playerName));
            otherBox.getChildren().add(otherInfoBoard);
        }

        sideBar.getChildren().add(otherBox);

        gameViewModel.othersInfoProperty().addListener((MapChangeListener<String, String>) change -> {
            Platform.runLater(() -> {
                // if (change.wasRemoved()) {
                // // A player was removed, find and remove their info board
                // String playerName = change.getKey();

                // // Find and remove the existing info board with the player's name as an ID
                // ObservableList<Node> children = sideBar.getChildren();
                // for (int i = 0; i < children.size(); i++) {
                // Node child = children.get(i);
                // if (child instanceof Pane && child.getId() != null &&
                // child.getId().equals(playerName)) {
                // children.remove(i);
                // break;
                // }
                // }
                // } else {
                // // An existing player's score or turn was updated or a new player was added
                // String playerName = change.getKey();
                // String playerInfo = change.getValueAdded(); // Assuming the info format is
                // still "score:turn"
                // Pane otherInfoBoard = createOtherInfoBoard(playerName, playerInfo);

                // // Find and replace the existing info board with the updated one
                // ObservableList<Node> children = sideBar.getChildren();
                // for (int i = 0; i < children.size(); i++) {
                // Node child = children.get(i);
                // if (child instanceof Pane && child.getId() != null &&
                // child.getId().equals(playerName)) {
                // children.set(i, otherInfoBoard);
                // return;
                // }
                // }

                // // If the player is not found in the side bar, add the new info board
                // sideBar.getChildren().add(otherInfoBoard);
                // }
                otherBox.getChildren().clear();
                for (String playerName : gameViewModel.othersInfoProperty().keySet()) {
                    Pane otherInfoBoard = createOtherInfoBoard(playerName,
                            gameViewModel.othersInfoProperty().get(playerName));
                    otherBox.getChildren().add(otherInfoBoard);
                }
                System.out.println("others info listener");
            });
        });

        // ListView<String> othersScoreListView = new ListView<>();
        // othersScoreListView.setItems(gameViewModel.getOthersInfoProperty());
        // for (String info : gameViewModel.getOthersInfoProperty()) {
        // String[] p = info.split(":");
        // ObservableValue<String> name = new SimpleStringProperty(p[0]);
        // ObservableValue<String> score = new SimpleStringProperty(p[1]);
        // boolean turn = p[2].equals("true");
        // Pane playerInfoBoard = createMyInfoBoard(name, score, turn, true);
        // sidebar.getChildren().add(playerInfoBoard);
        // }

        // Bag count
        // Label bagLabel = new Label("Bag:");
        // bagLabel.getStyleClass().add("login-label");
        // bagLabel.setAlignment(Pos.CENTER_LEFT);
        Label bagValueLabel = new Label();
        bagValueLabel.textProperty().bind(gameViewModel.bagCountProperty().asString());
        bagValueLabel.getStyleClass().add("bag-label");
        ImageView bagIconIndicator = new ImageView(bagIcon);
        bagIconIndicator.setFitWidth(60); // Set the desired width
        bagIconIndicator.setFitHeight(60);
        HBox bagCountBox = new HBox(bagIconIndicator, bagValueLabel);
        // bagCountBox.getStyleClass().add("bag-image");
        bagCountBox.setAlignment(Pos.CENTER);

        // Game Books
        Button bookListButton = new Button("Game Books");
        bookListButton.getStyleClass().add("darkgreen-button");
        bookListButton.setOnAction(e -> gameController.showBookSelectionWindow(false));
        HBox gameBooksBox = new HBox(bookListButton);
        gameBooksBox.setAlignment(Pos.CENTER);

        VBox bookBagBox = new VBox(10, bagCountBox, gameBooksBox);
        bookBagBox.setPadding(new Insets(40, 0, 50, 0)); // 20 units of padding at top and bottom

        sideBar.getChildren().add(bookBagBox);

        return sideBar;
    }

    private Pane createMyInfoBoard(StringProperty myName, IntegerProperty myScore, BooleanProperty myTurn) {

        // My Name
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(myName);
        nameValueLabel.getStyleClass().add("my-name-label");

        // Turn indicator
        ImageView turnIndicator = new ImageView(turnIcon);
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
                System.out.println("score listener");
            });
        });
        ///
        ImageView scoreIndicator = new ImageView(scoreIcon);
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
                System.out.println("turn listener");
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
        ImageView turnIndicator = new ImageView(turnIcon);
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

        ImageView scoreIndicator = new ImageView(scoreIcon);
        scoreIndicator.setFitWidth(20);
        scoreIndicator.setFitHeight(20);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIndicator, scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        HBox otherInfoBoard = new HBox(10, nameBox, scoreBox);
        otherInfoBoard.setId(playerName);
        otherInfoBoard.getStyleClass().add("other-score-board");
        if (playerTurn) {
            otherInfoBoard.getStyleClass().add("glow-button");
        }
        otherInfoBoard.setMinSize(230, 60);
        otherInfoBoard.setMaxSize(230, 60);
        otherInfoBoard.setAlignment(Pos.CENTER);

        return otherInfoBoard;
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
                if (!gameController.placementCelles.isEmpty()) {
                    Pane cellPane = gameController.placementCelles.pop();
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

        // Message board
        Text messageLabel = new Text(MessageReader.getMsg());
        messageLabel.setFill(Color.WHITE);
        messageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox messageBox = new HBox(messageLabel);
        messageBox.setPrefSize(150, 100);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.getStyleClass().add("message-box");

        // Tile buttons
        FlowPane tileButtonsPane = new FlowPane(10, 10);
        tileButtonsPane.setAlignment(Pos.CENTER);

        tileButtons = createTileButtons(gameViewModel.myTilesProperty(), false);
        tileButtonsPane.getChildren().setAll(tileButtons);

        gameViewModel.myTilesProperty().addListener((obs, oldTiles, newTiles) -> {
            Platform.runLater(() -> {
                tileButtons = createTileButtons(gameViewModel.myTilesProperty(), false);
                tileButtonsPane.getChildren().setAll(tileButtons);
                System.out.println("tiles listener");
            });
        });

        // Sort Tiles Button
        sortTilesButton = new Button("⭰");
        sortTilesButton.getStyleClass().add("lightblue-button");

        // Reset Tiles button
        resetTilesButton = new Button("\u2B6F"); // \u2B6F ⟲ ⭯
        resetTilesButton.getStyleClass().add("grey-button");
        resetTilesButton.setPrefWidth(60);
        resetTilesButton.setPrefHeight(20);
        resetTilesButton.setDisable(true);
        resetTilesButton.setOnAction(event -> {
            for (Button tb : tileButtons) {
                tb.setDisable(true);
            }
            for (Pane cell : gameController.placementList) {
                // Clear all added style classes
                cell.getStyleClass().clear();
                cell.getStyleClass().remove("selected");
                cell.getStyleClass().add("board-cell");
                // Clear inline styles
                cell.setStyle("");
                // Add the default style
                cell.getStyleClass().add("board-cell");
            }
            for (Pane c : gameController.selectedCells) {
                c.getStyleClass().remove("selected");
            }
            gameViewModel.clearWord();
            gameController.placementCelles.clear();
            gameController.placementList.clear();
            gameController.selectedCells.clear();
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
        passTurnButton.setOnAction(e -> gameViewModel.skipTurn());

        // Challange Button
        Button challengeButton = new Button("Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setDisable(false);
        challengeButton.setOnAction(event -> gameViewModel.challenge());

        boolean myTurn = gameViewModel.myTurnProperty().get();
        challengeButton.setDisable(!myTurn);
        passTurnButton.setDisable(!myTurn);

        gameViewModel.myTurnProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                challengeButton.setDisable(!newValue);
                passTurnButton.setDisable(!newValue);
                System.out.println("turn button listener");
            });
        });

        // Quit game Button
        Button quitGameButton = new Button("Quit Game");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setOnAction(e -> gameController.showQuitGameWindow());

        // Try place word Button
        tryPlaceWordButton = new Button("Try Place Word");
        tryPlaceWordButton.getStyleClass().add("blue-button");
        tryPlaceWordButton.setOnAction(event -> {
            tryPlaceWordButton.setDisable(true);
            challengeButton.setDisable(false);

            String word = gameViewModel.getWord();

            gameViewModel.tryPlaceWord(word);

            // challengeButton.getStyleClass().add("glow-button");
            // passTurnButton.getStyleClass().add("glow-button");

            // gameViewModel.clearPlayerTiles();
            gameController.resetWordPlacement();

            // Clear existing tile buttons
            // tilePane.getChildren().clear();
        });
        tryPlaceWordButton.setDisable(gameController.placementCelles.isEmpty());

        VBox playButtonsBox = new VBox(15, tryPlaceWordButton, challengeButton, passTurnButton, quitGameButton);
        playButtonsBox.setAlignment(Pos.CENTER);

        // Round Buttons
        HBox roundButtonsBox = createRoundButtons(true);
        roundButtonsBox.setPrefHeight(120);
        roundButtonsBox.setAlignment(Pos.BOTTOM_CENTER);

        // Round buttons pane
        Button helpButton = (Button) roundButtonsBox.getChildren().get(0);
        Text gameInstruText = new Text("Guest Mode");
        gameInstruText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text explanationText = new Text(descriptions.get("game-mode-guest"));
        explanationText.getStyleClass().add("content-label");
        explanationText.setTextAlignment(TextAlignment.CENTER);
        VBox guestModeExp = new VBox(10, gameInstruText, explanationText);
        guestModeExp.setAlignment(Pos.CENTER);
        Node[] helpBoxNodes = createCustomBox(guestModeExp, "red", symbols.get("exit"), "help");
        VBox helpBox = (VBox) helpBoxNodes[0];
        Button helpBoxButton = (Button) helpBoxNodes[1];
        helpBoxButton.setOnAction(e -> gameController.closeCustomWindow());
        helpButton.setOnAction(e -> gameController.showCustomWindow(helpBox, 700, 400));

        Button messageButton = (Button) roundButtonsBox.getChildren().get(1);

        // messageButton.setOnAction(e -> showMessageWindow("All"));

        customRoot.getChildren().addAll(tileBox, playButtonsBox, roundButtonsBox);

        return customRoot;
    }

    public List<Button> getTileButtons() {
        return tileButtons;
    }

    private GridPane createBoard() {
        gameBoard = new GridPane();
        gameBoard.getStyleClass().add("board-background");

        gameViewModel.currentBoardProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                updateGameBoard(newValue);
                System.out.println("board listener");
            });

        });

        // Initial setup
        updateGameBoard(gameViewModel.currentBoardProperty().get());

        return gameBoard;
    }

    private void updateGameBoard(Tile[][] board) {
        int boardSize = board.length;

        // Create board of cells(Pane)
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Create cell
                Pane cellPane = new Pane();
                cellPane.getStyleClass().add("board-cell");
                cellPane.setPrefSize(60, 60);

                if (board[row][col] != null) {
                    String letter = String.valueOf(board[row][col].getLetter());
                    cellPane.getStyleClass().add("character");
                    cellPane.getStyleClass().add("character-" + letter);
                    // cellPane.setStyle("-fx-background-image: url('tiles/" + letter + ".png');");
                }

                // On cell click
                cellPane.setOnMouseClicked(event -> {
                    //
                    gameController.placementCelles.clear();
                    gameController.placementList.clear();
                    //
                    if (gameViewModel.myTurnProperty().get()) {
                        // for (Button bt : tileButtons) {
                        // bt.setDisable(true);
                        // }
                        if (gameController.selectedCells.size() == 0) {
                            gameController.resetWordPlacement();
                            gameController.selectedCells.add(cellPane);
                            cellPane.getStyleClass().add("selected");
                        } else if (gameController.selectedCells.size() == 1) {
                            if (gameController.selectedCells.contains(cellPane)) {
                                gameController.selectedCells.remove(cellPane);
                                cellPane.getStyleClass().remove("selected");
                            } else {
                                int firstRow = GridPane.getRowIndex(gameController.selectedCells.get(0));
                                int firstCol = GridPane.getColumnIndex(gameController.selectedCells.get(0));
                                int lastRow = GridPane.getRowIndex(cellPane);
                                int lastCol = GridPane.getColumnIndex(cellPane);
                                // Same row/col only
                                if (firstRow == lastRow || firstCol == lastCol) {
                                    gameController.selectedCells.add(cellPane);
                                    cellPane.getStyleClass().add("selected");
                                }
                            }
                        } else if (gameController.selectedCells.size() == 2) {
                            if (gameController.selectedCells.contains(cellPane)) {
                                gameController.selectedCells.remove(cellPane);
                                cellPane.getStyleClass().remove("selected");
                            } else {
                                // clear all panes
                                for (Pane cell : gameController.selectedCells) {
                                    cell.getStyleClass().remove("selected");
                                }
                                gameController.selectedCells.clear();

                                // add the new one
                                gameController.selectedCells.add(cellPane);
                                cellPane.getStyleClass().add("selected");
                            }
                        }

                        // Enable/disable buttons based on the number of selected cells
                        boolean enableButtons = gameController.selectedCells.size() == 2;
                        tryPlaceWordButton.setDisable(!enableButtons);

                        if (enableButtons) {
                            for (Button b : tileButtons) {
                                b.setDisable(false);
                                b.setOnMouseEntered(e -> {
                                    b.getStyleClass().add("tile-button-hover");
                                });
                                // Remove hover effect when mouse exits the button
                                b.setOnMouseExited(e -> {
                                    b.getStyleClass().remove("tile-button-hover");
                                });
                            }
                            int firstRow = GridPane.getRowIndex(gameController.selectedCells.get(0));
                            int firstCol = GridPane.getColumnIndex(gameController.selectedCells.get(0));
                            int lastRow = GridPane.getRowIndex(gameController.selectedCells.get(1));
                            int lastCol = GridPane.getColumnIndex(gameController.selectedCells.get(1));

                            gameViewModel.setFirstSelectedCellRow(firstRow);
                            gameViewModel.setFirstSelectedCellCol(firstCol);
                            gameViewModel.setLastSelectedCellRow(lastRow);
                            gameViewModel.setLastSelectedCellCol(lastCol);

                            gameController.setPlacementCells();

                        }
                        // else {
                        // gameViewModel.clearSelectedCells();
                        // }

                    } else {
                        VBox turnAlert = createAlertBox("Not Your Turn", "It's not your turn to play");
                        Node[] turnAlertNodes = createCustomBox(turnAlert, "blue", "ok", "alert");
                        VBox turnAlertBox = (VBox) turnAlertNodes[0];
                        Button alertBoxButton = (Button) turnAlertNodes[1];
                        alertBoxButton.setOnAction(e -> gameController.closeCustomWindow());
                        gameController.showCustomWindow(turnAlertBox, 400, 200);
                    }
                });

                // Add the cell to the GridPane
                gameBoard.add(cellPane, col, row);
            }
        }

        if (turnWords.size() > 0) {
            highlightCellsForWords(turnWords);
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

    private void highlightCellsForWords(List<Word> words) {
        System.out.println("highlight cells, words: " + words.size());
        List<Pane> panes = new ArrayList<>();
        for (Word word : words) {
            int row = word.getRow();
            int col = word.getCol();
            boolean isVertical = word.isVertical();

            for (Tile t : word.getTiles()) {
                Pane cellPane = (Pane) getCellFromBoard(row, col);
                if (cellPane != null) {
                    cellPane.getStyleClass().add("marked");
                    panes.add(cellPane);
                }

                if (isVertical) {
                    row++;
                } else {
                    col++;
                }
            }
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                panes.forEach(p -> p.getStyleClass().remove("marked"));
            }

        }, 1000);
    }

    public String generateRandomChar() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        int randomIndex = random.nextInt(characters.length());
        return String.valueOf(characters.charAt(randomIndex));
    }
}
