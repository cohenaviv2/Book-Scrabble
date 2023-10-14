package app.view;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

import app.model.host.HostModel;
import app.view_model.GameViewModel;
import app.view_model.ViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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
    //
    private ListView<String> myWordsListView;
    //
    private HBox osBar;
    //
    private List<String> selectedBooks;
    private String myName;
    private int customPort = 0;

    public GameView(GameController gameController) {
        this.gameController = gameController;
        // Initialize and set up the initial UI components here.
        this.selectedBooks = new ArrayList<>();
        this.styleSheet = getClass().getResource("/style.css").toExternalForm();
        this.gameIcon = new Image("icons/game-icon.png");
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
        Button hostButton = new Button("Host Mode");
        hostButton.getStyleClass().add("red-button");
        hostButton.setMinSize(200, 80);
        hostButton.setMaxSize(200, 80);

        hostButton.setOnAction(event -> {
            this.isHost = true;
            this.gameViewModel = new ViewModel(isHost);
            gameController.gameSetupStage.close();
            gameController.showLoginForm(isHost);
        });

        // Guest Button
        Button guestButton = new Button("Guest Mode");
        guestButton.getStyleClass().add("blue-button");
        guestButton.setMinSize(200, 80);
        guestButton.setMaxSize(200, 80);

        guestButton.setOnAction(event -> {
            this.isHost = false;
            this.gameViewModel = new ViewModel(isHost);
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

    public VBox createLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setSpacing(15);

        if (isHost) {
            checkHostConnection();
        }

        List<TextField> textFields = new ArrayList<>();
        // Waiting Box
        VBox waitingBox = createWaitingBox();
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
        TextField nameTextField = new TextField();
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
        HBox roundButtonsPane = createButtonsPane(false);
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
        Node[] settingBoxNodes = createCustomBox(settings , "red", symbols.get("exit"), "settings");
        VBox settingsBox = (VBox) settingBoxNodes[0];
        Button settingsBoxButton = (Button) settingBoxNodes[1];
        settingsBoxButton.setOnAction(e->gameController.closeCustomWindow());
        double height = isHost ? 850 : 550;
        settingsButton.setOnAction(e->gameController.showCustomWindow(settingsBox, 600, height));

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

                        if (gameViewModel.isConnected()) {
                            gameController.showCustomWindow(fullWaitingBox, 500, 350);
                        }
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
                            gameController.showCustomWindow(waitingBox, 500, 350);

                        }

                    }
                }

            });
        }

        return loginFormBox;
    }

    private HBox createButtonsPane(boolean gameFlowWindow) {
        HBox roundButtonsPane = new HBox(10);
        roundButtonsPane.setAlignment(Pos.CENTER);
        roundButtonsPane.setPadding(new Insets(30, 0, 0, 0));
        roundButtonsPane.setMinHeight(70);
        roundButtonsPane.setPrefHeight(70);

        // Settings/Messages
        String symbol = gameFlowWindow ? symbols.get("messages") : symbols.get("settings");
        String color = gameFlowWindow ? "gold" : "grey";
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

        ObservableList<String> bookList = fullBookList ? GameViewModel.getBookList()
                : FXCollections.observableArrayList(gameViewModel.getGameBooksProperty());

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
        Button doneButton = new Button(buttonText);
        doneButton.getStyleClass().add("green-button");
        doneButton.setPrefHeight(60);
        doneButton.setPrefWidth(120);
        doneButton.setOnAction(event -> gameController.customStage.close());

        buttonPane.getChildren().add(doneButton);

        rootContainer.getChildren().addAll(scrollPane, buttonPane);

        return rootContainer;
    }

    public HBox createOSBar(Stage currentStage, Boolean gameIsRunning) {
        osBar = new HBox();
        osBar.setSpacing(12);
        osBar.setPadding(new Insets(15, 0, 0, 15));
        // osBar.getStyleClass().add("os-bar");

        // Exit Button
        Button exitButton = new Button(symbols.get("exit"));
        exitButton.getStyleClass().add("red-button");
        exitButton.setOnAction(event -> {
            if (gameIsRunning) {
                // showQuitGameWindow();
                System.out.println("\n\n h \n\n");
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

    private VBox createWaitingBox() {
        Text waitingText = new Text("Preparing the game...\nWaiting for all players to connect...");
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
        VBox alertBox = new VBox(10, alertTitle, alertErrorText);
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
            Text myIpTitle = new Text("My IP");
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
            Button myIpButton = new Button("What's My IP");
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

            VBox myIpSettings = new VBox(10, myIpTitle, myIpText, localIpCheckBox,myIpButton, myIpField);
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
}
