package app.view;

import app.model.GetMethod;
import app.model.game.*;
import app.model.host.HostModel;
import java.io.*;
import java.net.*;
import java.util.*;
import app.view_model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

public class WindowController {

    private GameViewModel gameViewModel;
    private Stage primaryStage;
    private Stage alertStage;
    private String MY_NAME;
    private String GAME_MODE;
    private int CUSTOM_PORT = 0;
    String msg = MessageReader.getMsg();

    private GridPane gameBoard;
    private Scene gameFlowScene;
    private Map<String, String> txtExplanations;
    private List<String> selectedBooks;
    private String MY_BOOKS_SERILIZED;
    private List<Pane> selectedCells;
    private Stack<Pane> placementCelles;
    private List<Pane> placementList;
    private Button tryPlaceWordButton;
    private ObservableList<Button> tileButtons;

    public final String CSS_STYLESHEET = getClass().getResource("/style.css").toExternalForm();
    private final Font EMOJI_FONT = Font.loadFont(getClass().getResourceAsStream("src\\main\\resources\\seguiemj.ttf"),
            24);
    private final String X = "\u2718";

    private double xOffset = 0;
    private double yOffset = 0;
    HBox osBar;

    public WindowController(Stage primaryStage, GameViewModel gameViewModel) {
        this.primaryStage = primaryStage;
        this.gameViewModel = gameViewModel;
        this.selectedBooks = new ArrayList<>();
        this.selectedCells = new ArrayList<>();
        this.placementCelles = new Stack<>();
        this.placementList = new ArrayList<>();
        this.txtExplanations = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\explanations.txt"))) {
            String line;
            String currentTitle = null;
            StringBuilder explanationBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    if (currentTitle != null) {
                        txtExplanations.put(currentTitle, explanationBuilder.toString());
                    }

                    String[] parts = line.split("=", 2);
                    currentTitle = parts[0].trim();
                    explanationBuilder = new StringBuilder(parts[1].trim());
                } else {
                    explanationBuilder.append("\n").append(line.trim());
                }
            }

            if (currentTitle != null) {
                txtExplanations.put(currentTitle, explanationBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // OS BAR

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

    private HBox createOsBar(Boolean gameIsRunning) {
        osBar = new HBox();
        osBar.setSpacing(10);
        osBar.getStyleClass().add("os-bar");
        osBar.setMinHeight(30);

        Button closeButton = new Button("\uD83D\uDDD9");
        closeButton.setFont(EMOJI_FONT);
        closeButton.getStyleClass().add("red-button");
        // closeButton.setMinHeight(40);
        // closeButton.setPrefHeight(40);

        closeButton.setOnAction(event -> {
            if (gameIsRunning)
                showQuitGameWindow();
            else {
                Stage stage = (Stage) primaryStage.getScene().getWindow();
                stage.close();
                System.exit(0);
            }
        });

        Button minimizeButton = new Button("\uD83D\uDDD5");
        minimizeButton.getStyleClass().add("green-button");
        minimizeButton.setFont(EMOJI_FONT);

        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });

        osBar.getChildren().addAll(closeButton, minimizeButton);
        osBar.setOnMousePressed(this::handleMousePressed);
        osBar.setOnMouseDragged(this::handleMouseDragged);

        return osBar;
    }

    // MAIN WINDOWS

    public VBox createInitialWindow() {
        VBox initialWindowBox = new VBox(10);
        initialWindowBox.setPadding(new Insets(20));
        initialWindowBox.setAlignment(Pos.CENTER);
        initialWindowBox.getStyleClass().add("wood-background");

        this.MY_NAME = "";
        this.MY_BOOKS_SERILIZED = "";
        this.CUSTOM_PORT = 0;

        // Styled header label
        Text headerLabel = new Text("Book Scrabble");
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

        Text hostModeTitle = new Text("Host Mode");
        hostModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text hostModeText = new Text(txtExplanations.get("game-mode-host"));
        hostModeText.getStyleClass().add("content-label");
        Text guestModeTitle = new Text("Guest Mode");
        guestModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        Text guestModeText = new Text(txtExplanations.get("game-mode-guest"));
        guestModeText.getStyleClass().add("content-label");

        VBox centerContent = new VBox(10, hostModeTitle, hostModeText, guestModeTitle, guestModeText);
        centerContent.setAlignment(Pos.CENTER);

        helpButton.setOnAction(e -> openCustomWindow(centerContent, "red", this.X, 720, 500));

        hostButton.setOnAction(event -> {
            GAME_MODE = "H";
            gameViewModel.setGameMode(GAME_MODE);
            showHostLoginForm();
        });

        // Event handler for guestButton
        guestButton.setOnAction(event -> {
            GAME_MODE = "G";
            gameViewModel.setGameMode(GAME_MODE);
            showGuestLoginForm();
        });

        Label justLabel = new Label("");
        Label justLabel2 = new Label("");

        HBox bar = createOsBar(false);
        initialWindowBox.getChildren().addAll(bar, headerLabel, justLabel, modeLabel, buttonPane, justLabel2,
                helpButton);
        return initialWindowBox;
    }

    private void showBookSelectionWindow(boolean fullBookList) {
        Stage bookSelectionStage = new Stage();
        String buttonText = fullBookList ? "Done" : "Got it!";
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

        Button doneButton = new Button(buttonText);
        doneButton.getStyleClass().add("green-button");
        doneButton.setPrefHeight(60);
        doneButton.setPrefWidth(120);
        doneButton.setOnAction(event -> {
            // Handle the action with the selected books
            // e.g., pass the selectedBooks list to the next screen or perform an action
            try {
                MY_BOOKS_SERILIZED = ObjectSerializer.serializeObject(this.selectedBooks);
            } catch (IOException e) {
            }

            bookSelectionStage.close();
        });

        buttonPane.getChildren().add(doneButton);

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
        loginFormBox.setSpacing(15);

        new Thread(() -> {
            // Check for connection
            if (!HostModel.get().isGameServerConnect()) {
                showConnectionAlert(txtExplanations.get("game-server-error"));
            } else if (!gameViewModel.isConnected()) {
                showConnectionAlert(txtExplanations.get("host-server-error"));
            }
        }).start();

        // OS Bar
        HBox osBar = createOsBar(true);

        // My name
        Label nameLabel = new Label("My name:");
        nameLabel.getStyleClass().add("login-label");
        TextField nameTextField = new TextField();
        nameTextField.setOnMouseClicked(e -> nameTextField.getStyleClass().remove("error-field"));
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
        Label selectBookLabel = new Label("Select Books:");
        selectBookLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> {
            booksButton.getStyleClass().remove("error-field");
            showBookSelectionWindow(true);
        });

        Label waitingLabel = new Label("");

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
            HostModel hm = (HostModel) gameViewModel.gameModel;
            if (hm.isGameServerConnect()) {
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
                this.CUSTOM_PORT = Integer.parseInt(portText);
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
        myIpField.getStyleClass().add("text-field");
        myIpField.setAlignment(Pos.CENTER);
        myIpField.setMaxWidth(220);

        CheckBox localIpCheckBox = new CheckBox("If you're playing on the same network, show local IP");
        localIpCheckBox.setTextFill(Color.BLACK);
        Button myIpButton = new Button("What's My IP");
        myIpButton.getStyleClass().add("yellow-button");

        myIpButton.setOnAction(e -> {
            if (localIpCheckBox.isSelected()) {
                try {
                    myIpField.setText(InetAddress.getLocalHost().getHostAddress());
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Use global IP retrieval logic here
                myIpField.setText(gameViewModel.getLocalIpAddress());
            }
        });

        VBox myIpSettings = new VBox(10, myIpTitle, myIpText, myIpButton, localIpCheckBox, myIpField);
        myIpSettings.setAlignment(Pos.CENTER);

        VBox settings = new VBox(5, gameServerSettings, customPortSettings, myIpSettings);
        settings.setAlignment(Pos.CENTER);

        HBox roundButtonsPane = createButtonsPane();
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        Text hostModeTitle = new Text("Host Mode");
        hostModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text hostModeText = new Text(txtExplanations.get("game-mode-host"));
        hostModeText.getStyleClass().add("content-label");
        hostModeText.setTextAlignment(TextAlignment.CENTER);
        VBox hostModeExp = new VBox(10, hostModeTitle, hostModeText);
        hostModeExp.setAlignment(Pos.CENTER);
        helpButton.setOnAction(e -> openCustomWindow(hostModeExp, "red", this.X, 700, 400));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        settingsButton.setOnAction(e -> openCustomWindow(settings, "red", this.X, 600, 850));

        // Submit buttons
        HBox submitButtons = createSubmitButtons("Start game", 160);
        Button startGameButton = (Button) submitButtons.getChildren().get(1);

        // CONNECT ME
        startGameButton.setOnAction(event -> {
            MY_NAME = nameTextField.getText();
            if (MY_NAME.isEmpty()) {
                nameTextField.getStyleClass().add("error-field");
            } else if (selectedBooks.size() == 0) {
                booksButton.getStyleClass().add("error-field");
            } else {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                gameViewModel.setTotalPlayersCount(numOfPlayersComboBox.getValue());
                gameViewModel.connectMe(MY_NAME, "0", CUSTOM_PORT);
                gameViewModel.myBookChoice(MY_BOOKS_SERILIZED);
                gameViewModel.ready();

                loginFormBox.setDisable(true);
                osBar.setDisable(true);

                if (gameViewModel.isConnected()) {
                    // Waiting Window
                    showWaitingWindow("Preparing the game...\nWaiting for all players to connect...", true);
                }

            }
        });

        VBox root = new VBox(osBar, loginFormBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(22);
        root.getStyleClass().add("wood-background");

        loginFormBox.getChildren().addAll(nameLabel,
                nameTextField, selectBookLabel, booksButton, numOfPlayersLabel, numOfPlayersComboBox, roundButtonsPane,
                waitingLabel,
                submitButtons);

        Scene hostLoginFormScene = new Scene(root, 400, 600);
        hostLoginFormScene.getStylesheets().add(CSS_STYLESHEET);
        hostLoginFormScene.setCursor(Cursor.HAND);
        primaryStage.setScene(hostLoginFormScene);
    }

    private void showGuestLoginForm() {
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.setSpacing(15);

        // OS Bar
        HBox osBar = createOsBar(false);

        // My name
        Label nameLabel = new Label("My name:");
        nameLabel.getStyleClass().add("login-label");
        TextField nameTextField = new TextField();
        nameTextField.setOnMouseClicked(e -> nameTextField.getStyleClass().remove("error-field"));
        //
        nameTextField.setText("Moshe");
        //
        nameTextField.setMaxWidth(200);
        nameTextField.setAlignment(Pos.CENTER);
        nameTextField.getStyleClass().add("text-field");

        // Select books
        Label booksLabel = new Label("Select Books:");
        booksLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> {
            booksButton.getStyleClass().remove("error-field");
            showBookSelectionWindow(true);
        });

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

        // Custom port
        Text customPortTitle = new Text("Host's Custom Port");
        customPortTitle.setTextAlignment(TextAlignment.CENTER);
        customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text customPortLabel = new Text(txtExplanations.get("custom-port"));
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
                this.CUSTOM_PORT = Integer.parseInt(portText);
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

        VBox settings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton, invalidPortTxt);
        settings.setAlignment(Pos.CENTER);

        // Round buttons pane
        HBox roundButtonsPane = createButtonsPane();
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        Text guestModeTitle = new Text("Guest Mode");
        guestModeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text guestModeText = new Text(txtExplanations.get("game-mode-guest"));
        guestModeText.getStyleClass().add("content-label");
        guestModeText.setTextAlignment(TextAlignment.CENTER);
        VBox guestModeExp = new VBox(10, guestModeTitle, guestModeText);
        guestModeExp.setAlignment(Pos.CENTER);
        helpButton.setOnAction(e -> openCustomWindow(guestModeExp, "red", this.X, 700, 400));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        settingsButton.setOnAction(e -> openCustomWindow(settings, "red", this.X, 500, 500));

        Label waintingLabel = new Label("");

        // Submit buttons
        HBox submitButtons = createSubmitButtons("Connect", 140);
        Button connectButton = (Button) submitButtons.getChildren().get(1);

        // SET GAME MODE
        // gameViewModel.setGameMode(GAME_MODE);

        connectButton.setOnAction(event -> {
            connectButton.setDisable(true);
            MY_NAME = nameTextField.getText();
            String ip = ipTextField.getText();
            // int port = Integer.parseInt(portTextField.getText());

            if (MY_NAME.isEmpty()) {
                nameTextField.getStyleClass().add("error-field");
            } else if (selectedBooks.size() == 0) {
                booksButton.getStyleClass().add("error-field");
            } else {
                if (CUSTOM_PORT == 0) {
                    CUSTOM_PORT = HostModel.HOST_SERVER_PORT;
                }
                gameViewModel.connectMe(MY_NAME, ip, CUSTOM_PORT);
                // Check host server connection
                if (!gameViewModel.isConnected()) {
                    showConnectionAlert(txtExplanations.get("guest-socket-error"));
                }
                gameViewModel.myBookChoice(MY_BOOKS_SERILIZED);
                gameViewModel.ready();

                loginFormBox.setDisable(true);
                osBar.setDisable(true);

                // Waiting Window
                showWaitingWindow("Preparing the game...\nWaiting for all players to connect...", true);

            }
        });

        VBox root = new VBox(osBar, loginFormBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(22);
        root.getStyleClass().add("wood-background");

        loginFormBox.getChildren().addAll(
                nameLabel,
                nameTextField,
                booksLabel,
                booksButton,
                ipLabel,
                ipTextField,
                roundButtonsPane,
                waintingLabel,
                submitButtons);

        Scene guestLoginFormScene = new Scene(root, 400, 600);
        guestLoginFormScene.getStylesheets().add(CSS_STYLESHEET);
        guestLoginFormScene.setCursor(Cursor.HAND);
        primaryStage.setScene(guestLoginFormScene);
    }

    public void showGameFlowWindow() {
        // Implement the code to display the game flow window
        // You can use a new Scene and a different layout container to represent the
        // game flow window
        Platform.runLater(() -> {

            alertStage.close();

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

            primaryStage.setScene(gameFlowScene);
            primaryStage.setWidth(1620); // Set the width of the stage
            primaryStage.setHeight(840); // Set the height of the stage

            // Calculate the centered x and y coordinates
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            double centerX = (screenWidth - primaryStage.getWidth()) / 2;
            double centerY = (screenHeight - primaryStage.getHeight()) / 2;

            // Set the stage position
            primaryStage.setX(centerX);
            primaryStage.setY(centerY);

            primaryStage.show();

            checkForMessage();
        });
    }

    // ALERT WINDOWS

    private Button openCustomWindow(Node content, String btnColor, String btnText, double width, double height) {
        alertStage = new Stage();
        alertStage.initStyle(StageStyle.UNDECORATED);
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initOwner(primaryStage); // Set the owner stage (main window)

        // Calculate the center coordinates of the main stage
        double mainStageX = primaryStage.getX();
        double mainStageY = primaryStage.getY();
        double mainStageWidth = primaryStage.getWidth();
        double mainStageHeight = primaryStage.getHeight();

        double customStageWidth = width; // Adjust as needed
        double customStageHeight = height; // Adjust as needed

        double customStageX = mainStageX + (mainStageWidth - customStageWidth) / 2;
        double customStageY = mainStageY + (mainStageHeight - customStageHeight) / 2;

        alertStage.setX(customStageX);
        alertStage.setY(customStageY);

        Scene customScene;
        VBox customRoot = new VBox(30);
        customRoot.setAlignment(Pos.CENTER);
        customRoot.getStyleClass().addAll("content-background");

        customRoot.getChildren().add(content);

        Button button = null;

        if (btnColor != null) {
            button = new Button(btnText);
            button.getStyleClass().add(btnColor + "-button");
            button.setOnAction(event -> {
                alertStage.close();
            });

            StackPane buttonPane = new StackPane(button);
            BorderPane.setAlignment(buttonPane, Pos.TOP_CENTER);
            // buttonPane.setPadding(new Insets(30, 0, 0, 0)); // Add padding to the top

            customRoot.getChildren().add(buttonPane);
        }

        customScene = new Scene(customRoot, customStageWidth, customStageHeight);

        customScene.getStylesheets().add(CSS_STYLESHEET);
        customScene.setCursor(Cursor.HAND);
        alertStage.setScene(customScene);
        alertStage.show();

        return button;

    }

    public Button showAlert(String alert) {
        Text message = new Text(alert);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        VBox textBox = new VBox(10, message);
        textBox.setAlignment(Pos.CENTER);
        return openCustomWindow(textBox, "blue", "OK", 550, 250);
    }

    private void showWaitingWindow(String text, boolean isQuitButton) {
        // Waiting Window
        Text waitingText = new Text(text);
        waitingText.getStyleClass().add("content-label");
        waitingText.setTextAlignment(TextAlignment.CENTER);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.getStyleClass().add("progress-indicator");
        VBox waitingBox = new VBox(10, waitingText, progressIndicator);
        waitingBox.setAlignment(Pos.CENTER);
        String buttonColor = isQuitButton ? "red" : null;
        Button waitingQuitButton = openCustomWindow(waitingBox, buttonColor, "Quit Game", 500, 350);
        if (isQuitButton) {
            waitingQuitButton.setOnAction(e -> {
                this.gameViewModel.quitGame();
                alertStage.close();
                Stage stage = (Stage) primaryStage.getScene().getWindow();
                stage.close();
                System.exit(0);
            });
        }
    }

    private void showConnectionAlert(String errorText) {
        // CreatecheckAndShowConnectionAlert a new Timer
        Timer timer = new Timer();

        // Schedule a TimerTask to run after a 2-second delay
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Check if gameViewModel is not connected after 2 seconds
                // Run on the JavaFX Application Thread
                Platform.runLater(() -> {
                    Text errorTitle = new Text("Network Error");
                    errorTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                    Text cantConnectText = new Text(errorText);
                    cantConnectText.getStyleClass().add("content-label");
                    cantConnectText.setTextAlignment(TextAlignment.CENTER);
                    VBox waitingBox = new VBox(10, errorTitle, cantConnectText);
                    waitingBox.setAlignment(Pos.CENTER);
                    Button waitingQuitButton = openCustomWindow(waitingBox, "red", "Exit Game", 550, 350);
                    waitingQuitButton.setOnAction(e -> {
                        gameViewModel.quitGame();
                        alertStage.close();
                        Stage stage = (Stage) primaryStage.getScene().getWindow();
                        stage.close();
                        System.exit(0);
                    });
                });
                // Cancel the Timer
                timer.cancel();
            }
        }, 1000); // 1 seconds delay
    }

    private void checkForMessage() {
        if (gameViewModel.isMessage()) {
            String message = gameViewModel.getMessage();

            // Guest player quit the game
            if (message.startsWith(GetMethod.quitGame)) {
                String playerName = message.split(",")[1];
                int playersCount = Integer.parseInt(message.split(",")[2]);
                // Host left alone in the game
                if (playersCount == 1 && this.GAME_MODE.equals("H")) {
                    showGameEndWindow("All the players left the game");
                } else {
                    showAlert(playerName + " has quit the game!").setOnAction(e -> alertStage.close());
                }
            }

            // Game ends
            else if (message.startsWith(GetMethod.endGame)) {
                String name = message.split(",")[1];

                // Host quit the game
                if (name.equals("HOST")) {

                    if (this.GAME_MODE.equals("G")) {
                        gameViewModel.quitGame();
                        showAlert("The host left the game!").setOnAction(e -> {
                            alertStage.close();
                            Stage stage = (Stage) primaryStage.getScene().getWindow();
                            stage.close();
                            System.exit(0);
                        });
                    }

                }

                // Game ends properly
                else {
                    String score = message.split(",")[2];
                    showGameEndWindow(name + " win! with " + score + "points!");
                }
            }

            // Challange words
            else if (message.startsWith(GetMethod.tryPlaceWord)) {
                // System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n");
                String params = message.split(",")[1];
                String[] turnWords = params.split(":");
                String alert = "";
                for (int i = 1; i < turnWords.length; i++) {
                    alert += turnWords[i];
                    if (i != turnWords.length - 1)
                        alert += ",";
                }
                alert += "\n\nOne of these words is not dictionary legal\nYou can try Challange or Pass turn.";
                showAlert(alert);
            }
        }
        // else System.out.println("\n\n\n\n\n\n DONT GOT MESSAGE\n\n\n\n\n\n");
    }

    private void showHostQuitWindow() {
        Text hostQuitText = new Text("The host has quit the game!");
        hostQuitText.getStyleClass().add("content-label");
        hostQuitText.setTextAlignment(TextAlignment.CENTER);
        VBox alertBox = new VBox(10, hostQuitText);
        alertBox.setAlignment(Pos.CENTER);
        Button waitingQuitButton = openCustomWindow(alertBox, "red", "Exit Game", 500, 350);
        waitingQuitButton.setOnAction(e -> {
            gameViewModel.quitGame();
            alertStage.close();
            Stage stage = (Stage) primaryStage.getScene().getWindow();
            stage.close();
            System.exit(0);
        });

    }

    private void showQuitGameWindow() {
        VBox customRoot = new VBox(30);
        customRoot.setAlignment(Pos.CENTER);

        Text quitGameText = new Text("Are you sure you want to quit game?");
        quitGameText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        quitGameText.setTextAlignment(TextAlignment.CENTER);

        Button yesButton = new Button("Yes");
        yesButton.getStyleClass().add("blue-button");
        yesButton.setOnAction(event -> {
            alertStage.close();
            if (GAME_MODE.equals("H")) {
                showWaitingWindow("Waiting for all players to disconnect...", false);
            }
            this.gameViewModel.quitGame();
            Stage stage = (Stage) primaryStage.getScene().getWindow();
            stage.close();
            System.exit(0);
        });

        Button noButton = new Button("No");
        noButton.getStyleClass().add("red-button");
        noButton.setOnAction(event -> alertStage.close());

        HBox buttons = new HBox(10, yesButton, noButton);
        buttons.setAlignment(Pos.CENTER);

        customRoot.getChildren().addAll(quitGameText, buttons);

        openCustomWindow(customRoot, null, null, 550, 300);
    }

    private void showGameEndWindow(String text) {
        VBox customRoot = new VBox(30);
        customRoot.setAlignment(Pos.CENTER);

        Text quitGameText = new Text(text);
        quitGameText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        quitGameText.setTextAlignment(TextAlignment.CENTER);

        Button quitButton = new Button("Quit Game");
        quitButton.getStyleClass().add("red-button");
        quitButton.setOnAction(event -> {
            alertStage.close();
            this.gameViewModel.quitGame();
            Stage stage = (Stage) primaryStage.getScene().getWindow();
            stage.close();
            System.exit(0);
        });

        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("green-button");
        newGameButton.setOnAction(event -> alertStage.close());

        HBox buttons = new HBox(10, newGameButton, quitButton);
        buttons.setAlignment(Pos.CENTER);

        customRoot.getChildren().addAll(quitGameText, buttons);

        openCustomWindow(customRoot, null, null, 550, 300);
    }

    // CREATE COMPENENTS

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));

        // // Player Name
        // Label nameLabel = new Label("My Name:");
        // Label nameValueLabel = new Label();
        // nameValueLabel.textProperty().bind(gameViewModel.getPlayerNameProperty());
        // nameLabel.getStyleClass().add("login-label");
        // nameValueLabel.getStyleClass().add("my-name-label");

        // ImageView turnIcon = new ImageView(new Image("turn-icon.png"));
        // turnIcon.setFitWidth(40); // Set the desired width
        // turnIcon.setFitHeight(40);

        // HBox nameBox = new HBox();
        // if(gameViewModel.isMyTurn()){
        // nameBox.getChildren().add(turnIcon);
        // }
        // nameBox.getChildren().add(nameValueLabel);
        // nameBox.setAlignment(Pos.CENTER);

        // // Player Score
        // Label scoreLabel = new Label("Score:");
        // scoreLabel.getStyleClass().add("login-label");
        // Label scoreValueLabel = new Label();
        // scoreValueLabel.textProperty().bind(gameViewModel.getPlayerScoreProperty());
        // scoreValueLabel.getStyleClass().add("score-label");
        // ///
        // ImageView scoreIcon = new ImageView(new Image("star-icon.png"));
        // scoreIcon.setFitWidth(40); // Set the desired width
        // scoreIcon.setFitHeight(40);
        // // iconImageView.setPreserveRatio(true);
        // HBox scoreBox = new HBox(scoreIcon, scoreValueLabel);
        // scoreBox.setAlignment(Pos.CENTER);

        // // Player Turn
        // Text turnLabel = new Text();
        // turnLabel.textProperty().bind(gameViewModel.getMyTurnView());
        // turnLabel.getStyleClass().add("turn-label");
        // if (gameViewModel.isMyTurn())
        // turnLabel.setFill(Color.GREENYELLOW);
        // else
        // turnLabel.setFill(Color.RED);
        // ImageView turnIcon = new ImageView(new Image("turn-icon.png"));
        // turnIcon.setFitWidth(40); // Set the desired width
        // turnIcon.setFitHeight(40);
        // // iconImageView.setPreserveRatio(true);
        // HBox turnBox = new HBox(turnIcon, turnLabel);
        // turnBox.setAlignment(Pos.CENTER);

        //
        // VBox myInfoBoard = new VBox(nameBox, scoreBox);
        // myInfoBoard.getStyleClass().add("wood-score-board");
        // myInfoBoard.setMinSize(250, 120);
        // myInfoBoard.setMaxSize(250, 120);
        // myInfoBoard.setAlignment(Pos.CENTER);

        // My Name & Score & Turn

        Pane myInfoBoard = createInfoBoard(gameViewModel.getPlayerNameProperty(),
                gameViewModel.getPlayerScoreProperty(), gameViewModel.isMyTurn(), false);
        sidebar.getChildren().add(myInfoBoard);

        // // Player Turn
        // Label turnLabel = new Label("My Turn:");
        // turnLabel.getStyleClass().add("login-label");
        // Label turnValueLabel = new Label();

        // StringBinding turnBinding = Bindings.createStringBinding(() -> {
        // String turn = gameViewModel.getPlayerTurnProperty().getValue();
        // if ("true".equals(turn)) {
        // turnValueLabel.setTextFill(Color.GREEN);
        // } else {
        // turnValueLabel.setTextFill(Color.RED);
        // }
        // return turn;
        // }, gameViewModel.getPlayerTurnProperty());

        // turnValueLabel.textProperty().bind(turnBinding);
        // turnLabel.setStyle("-fx-font-size: 14px;");
        // turnValueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        // HBox turnBox = new HBox(10, turnLabel, turnValueLabel);
        // turnBox.setAlignment(Pos.CENTER_LEFT);

        // for(String info : gameViewModel.getOthersInfoProperty()){
        // System.out.println(info);
        // }

        // Player Words
        //
        // StackPane stackPane = new StackPane();
        // stackPane.setStyle("-fx-background-color: transparent;"); // Set the
        // background color of the StackPane

        // ImageView backgroundImage = new ImageView(new Image("open-book.png"));
        // backgroundImage.fitWidthProperty().bind(stackPane.widthProperty());
        // backgroundImage.fitHeightProperty().bind(stackPane.heightProperty());
        // //
        // // VBox myWordsBox = new VBox();
        // // myWordsBox.getStyleClass().add("my-words-box");
        // Label wordsLabel = new Label("My Words:");
        // Create a transparent background

        // My Words
        Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));

        ListView<String> wordsListView = new ListView<>(gameViewModel.getPlayerWordsProperty());
        wordsListView.setPrefSize(150, 130);
        wordsListView.setBackground(transparentBackground);

        Label wordsLabel = new Label("My Words:");
        wordsLabel.getStyleClass().add("login-label");

        // Create a Pane to hold the ListView
        Pane wordsBox = new Pane(wordsListView);
        // wordsBox.getStyleClass().add("my-words-box");
        sidebar.getChildren().addAll(wordsLabel, wordsBox);

        wordsListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setAlignment(Pos.CENTER);
                    setText(item);
                    setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
                    setTextFill(Color.DARKGREEN); // Change the color to your desired color
                } else {
                    setText(null);
                }
            }
        });

        // // myWordsBox.getChildren().addAll(wordsLabel, wordsListView);
        // stackPane.getChildren().addAll(backgroundImage);
        // sidebar.getChildren().add(stackPane);

        // // Set custom cell factory to style cells with transparent background
        // wordsListView.setCellFactory(param -> new ListCell<String>() {
        // @Override
        // protected void updateItem(String item, boolean empty) {
        // super.updateItem(item, empty);
        // if (empty || item == null) {
        // setText(null);
        // setStyle("-fx-background-color: transparent; -fx-text-fill: black");
        // } else {
        // setText(item);
        // setStyle("-fx-background-color: transparent; -fx-text-fill: black");
        // }
        // }
        // });

        // Other players info
        Label othersScoreLabel = new Label("Other Players:");
        othersScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        ListView<String> othersScoreListView = new ListView<>();
        othersScoreListView.setItems(gameViewModel.getOthersInfoProperty());
        for (String info : gameViewModel.getOthersInfoProperty()) {
            String[] p = info.split(":");
            ObservableValue<String> name = new SimpleStringProperty(p[0]);
            ObservableValue<String> score = new SimpleStringProperty(p[1]);
            boolean turn = p[2].equals("true");
            Pane playerInfoBoard = createInfoBoard(name, score, turn, true);
            sidebar.getChildren().add(playerInfoBoard);
        }

        // othersScoreListView.setCellFactory(listView -> new ListCell<String>() {
        // @Override
        // protected void updateItem(String item, boolean empty) {
        // super.updateItem(item, empty);
        // if (empty || item == null) {
        // setText(null);
        // setGraphic(null);
        // } else {
        // String[] info = item.split(":");
        // String text = info[0] + ": " + info[1];
        // if (info[2].equals("true"))
        // text = "\u27A1 " + text;
        // setText(text);
        // setStyle(
        // "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill:
        // white;-fx-background-color: darkblue;");
        // setPadding(new Insets(5));
        // }
        // }
        // });
        // Bag count
        Label bagLabel = new Label("Bag:");
        bagLabel.getStyleClass().add("login-label");
        bagLabel.setAlignment(Pos.CENTER_LEFT);
        Label bagValueLabel = new Label();
        bagValueLabel.textProperty().bind(gameViewModel.getBagCountProperty());
        bagValueLabel.getStyleClass().add("bag-label");
        ImageView bagIcon = new ImageView(new Image("bag-icon.png"));
        bagIcon.setFitWidth(60); // Set the desired width
        bagIcon.setFitHeight(60);
        HBox bagCountBox = new HBox(bagIcon, bagValueLabel);
        // bagCountBox.getStyleClass().add("bag-image");
        bagCountBox.setAlignment(Pos.CENTER);

        // Game Books
        // Label booksLabel = new Label("Game Books:");
        // ListView<String> bookListView = new ListView<>();
        // bookListView.setItems(gameViewModel.getGameBooksProperty());
        Button bookListButton = new Button("Game Books");
        bookListButton.getStyleClass().add("darkgreen-button");
        // bookListButton.setPrefHeight(60);
        // bookListButton.setPrefWidth(180);
        bookListButton.setOnAction(event -> showBookSelectionWindow(false));
        HBox gameBooksBox = new HBox(bookListButton);
        gameBooksBox.setAlignment(Pos.CENTER);

        VBox bookBagBox = new VBox(10, bagCountBox, gameBooksBox);
        bookBagBox.setPadding(new Insets(40, 0, 50, 0)); // 20 units of padding at top and bottom

        sidebar.getChildren().add(bookBagBox);

        return sidebar;
    }

    private GridPane createBoardGridPane() {
        gameBoard = new GridPane();
        gameBoard.getStyleClass().add("board-background");
        // gameBoard.setPrefSize(732, 733); // Set the desired size of the grid

        Tile[][] board = gameViewModel.getCurrentBoard();
        int boardSize = board.length;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Pane cellPane = new Pane();
                cellPane.getStyleClass().add("cell");
                cellPane.setPrefSize(60, 60);

                if (board[row][col] != null) {
                    String letter = String.valueOf(board[row][col].getLetter());
                    cellPane.getStyleClass().add("character");
                    cellPane.setStyle("-fx-background-image: url('tiles/" + letter + ".png');");
                }

                cellPane.setOnMouseClicked(event -> {
                    //
                    placementCelles.clear();
                    placementList.clear();
                    //
                    if (gameViewModel.isMyTurn()) {
                        for (Button bt : tileButtons) {
                            bt.setDisable(true);
                        }
                        if (selectedCells.size() == 0) {
                            resetWordPlacement();
                            selectedCells.add(cellPane);
                            cellPane.getStyleClass().add("selected");
                        } else if (selectedCells.size() == 1) {
                            if (selectedCells.contains(cellPane)) {
                                selectedCells.remove(cellPane);
                                cellPane.getStyleClass().remove("selected");
                            } else {
                                int firstRow = GridPane.getRowIndex(selectedCells.get(0));
                                int firstCol = GridPane.getColumnIndex(selectedCells.get(0));
                                int lastRow = GridPane.getRowIndex(cellPane);
                                int lastCol = GridPane.getColumnIndex(cellPane);
                                // Same row/col only
                                if (firstRow == lastRow || firstCol == lastCol) {
                                    selectedCells.add(cellPane);
                                    cellPane.getStyleClass().add("selected");
                                }
                            }
                        } else if (selectedCells.size() == 2) {
                            if (selectedCells.contains(cellPane)) {
                                selectedCells.remove(cellPane);
                                cellPane.getStyleClass().remove("selected");
                            } else {
                                // clear all panes
                                for (Pane cell : selectedCells) {
                                    cell.getStyleClass().remove("selected");
                                }
                                selectedCells.clear();

                                // add the new one
                                selectedCells.add(cellPane);
                                cellPane.getStyleClass().add("selected");
                            }
                        }

                        // Enable/disable buttons based on the number of selected cells
                        boolean enableButtons = selectedCells.size() == 2;
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
                            int firstRow = GridPane.getRowIndex(selectedCells.get(0));
                            int firstCol = GridPane.getColumnIndex(selectedCells.get(0));
                            int lastRow = GridPane.getRowIndex(selectedCells.get(1));
                            int lastCol = GridPane.getColumnIndex(selectedCells.get(1));

                            gameViewModel.setFirstSelectedCellRow(firstRow);
                            gameViewModel.setFirstSelectedCellCol(firstCol);
                            gameViewModel.setLastSelectedCellRow(lastRow);
                            gameViewModel.setLastSelectedCellCol(lastCol);

                            setPlacementCells();

                        } else {
                            gameViewModel.clearSelectedCells();
                        }

                    } else {
                        Text turnText = new Text("It's not your turn to play");
                        turnText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                        turnText.setTextAlignment(TextAlignment.CENTER);
                        openCustomWindow(turnText, "blue", "OK", 400, 200);
                    }
                });

                gameBoard.add(cellPane, col, row);
            }
        }

        return gameBoard;
    }

    private Pane createInfoBoard(ObservableValue<String> name, ObservableValue<String> score, boolean turn,
            boolean otherPlayer) {

        double prefH, prefW;
        prefH = otherPlayer ? 70 : 130;
        prefW = 230;

        // Player Name
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(name);
        String nameStyle = otherPlayer ? "other-name-label" : "my-name-label";
        nameValueLabel.getStyleClass().add(nameStyle);

        ImageView turnIcon = new ImageView(new Image("arrow-icon2.png"));
        turnIcon.setFitWidth(60); // Set the desired width
        turnIcon.setFitHeight(60);

        HBox nameBox = new HBox(10);
        if (turn) {
            nameBox.getChildren().add(turnIcon);
        }
        nameBox.getChildren().add(nameValueLabel);
        nameBox.setAlignment(Pos.CENTER);

        // Player Score
        Label scoreLabel = new Label("Score:");
        scoreLabel.getStyleClass().add("login-label");
        Label scoreValueLabel = new Label();
        scoreValueLabel.textProperty().bind(score);
        String scoreStyle = otherPlayer ? "other-score-label" : "score-label";
        scoreValueLabel.getStyleClass().add(scoreStyle);
        ///
        ImageView scoreIcon = new ImageView(new Image("star-icon.png"));
        double size = otherPlayer ? 25 : 50;
        scoreIcon.setFitWidth(size); // Set the desired width
        scoreIcon.setFitHeight(size);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIcon, scoreValueLabel);
        scoreBox.setAlignment(Pos.CENTER);

        String backgroundStyle = otherPlayer ? "other-score-board" : "wood-score-board";

        if (otherPlayer) {
            HBox myInfoBoard = new HBox(10, nameBox, scoreBox);
            myInfoBoard.getStyleClass().add(backgroundStyle);
            myInfoBoard.setMinSize(prefW, prefH);
            myInfoBoard.setMaxSize(prefW, prefH);
            myInfoBoard.setAlignment(Pos.CENTER);

            return myInfoBoard;
        } else {
            VBox myInfoBoard = new VBox(-10, nameBox, scoreBox);
            myInfoBoard.getStyleClass().add(backgroundStyle);
            myInfoBoard.setMinSize(prefW, prefH);
            myInfoBoard.setMaxSize(prefW, prefH);
            myInfoBoard.setAlignment(Pos.CENTER);

            return myInfoBoard;
        }

    }

    private VBox createButtons() {
        VBox customRoot = new VBox(15);
        customRoot.setPadding(new Insets(20));
        customRoot.setAlignment(Pos.CENTER);

        // Message board
        Label messageLabel = new Label(MessageReader.getMsg());
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Tiles button
        FlowPane tilePane = new FlowPane(10, 10);
        tilePane.setAlignment(Pos.CENTER);

        tilePane.getChildren().clear();

        Button reserTilesButton = new Button("\u2B8C"); // \u2B6F ⟲ ⭯

        this.tileButtons = gameViewModel.getButtonTilesProperty();
        for (Button tileButton : tileButtons) {
            String letter = tileButton.getText();
            tileButton.setPrefSize(45, 45);
            tileButton.getStyleClass().add("tile-button");
            tileButton.setStyle("-fx-background-image: url('tiles/" + letter + ".png');");

            tileButton.setText(null);
            if (!gameViewModel.isMyTurn()) {
                tileButton.setDisable(true);
            }

            tileButton.setOnAction(event -> {
                if (!placementCelles.isEmpty()) {
                    Pane cellPane = placementCelles.pop();
                    cellPane.getStyleClass().add("character");
                    cellPane.setStyle("-fx-background-image: url('tiles/" + letter + ".png');"
                            + "-fx-border-color: yellow; -fx-border-style: solid inside;");

                    // Disable the tile button upon selection
                    tileButton.setDisable(true);
                    reserTilesButton.setDisable(false);

                    // tilePane.getChildren().remove(tileButton);

                    // Add the tile value to the word
                    gameViewModel.addToWord(letter);
                }
            });

            tilePane.getChildren().add(tileButton);
        }

        // Reset button
        reserTilesButton.getStyleClass().add("grey-button");
        reserTilesButton.setDisable(true);
        reserTilesButton.setOnAction(event -> {
            for (Button tb : tileButtons) {
                tb.setDisable(true);
            }
            for (Pane cell : placementList) {
                // Clear all added style classes
                cell.getStyleClass().clear();
                // Clear inline styles
                cell.setStyle("");
                // Add the default style
                cell.getStyleClass().add("cell");
            }
            gameViewModel.clearWord();
            placementCelles.clear();
            placementList.clear();
            selectedCells.clear();
            reserTilesButton.setDisable(true);
        });
        // Set the preferred width and height of the button
        reserTilesButton.setPrefWidth(60); // Adjust the width as desired
        reserTilesButton.setPrefHeight(20); // Adjust the height as desired

        VBox tileBox = new VBox(15, tilePane, reserTilesButton);
        tileBox.setAlignment(Pos.CENTER);

        // Pass turn Button
        Button passTurnButton = new Button(
                "Pass Turn");
        passTurnButton.getStyleClass().add("yellow-button");
        passTurnButton.setOnAction(event -> gameViewModel.skipTurn());

        // Challange Button
        Button challengeButton = new Button(
                "Challenge");
        challengeButton.getStyleClass().add("green-button");
        challengeButton.setDisable(true);
        challengeButton.setOnAction(event -> gameViewModel.challenge());

        if (!gameViewModel.isMyTurn()) {
            challengeButton.setDisable(true);
            passTurnButton.setDisable(true);
        }

        // Quit game Button
        Button quitGameButton = new Button(
                "Quit Game");
        quitGameButton.getStyleClass().add("red-button");
        quitGameButton.setOnAction(event -> showQuitGameWindow());

        // Try place word Button
        tryPlaceWordButton = new Button("Try Place Word");
        tryPlaceWordButton.getStyleClass().add("blue-button");
        tryPlaceWordButton.setOnAction(event -> {
            tryPlaceWordButton.setDisable(true);
            challengeButton.setDisable(false);

            String word = gameViewModel.getWord();

            // Call the method to handle the "Try Place Word" action with the collected data

            gameViewModel.tryPlaceWord(word);

            // challengeButton.getStyleClass().add("glow-button");
            // passTurnButton.getStyleClass().add("glow-button");

            // gameViewModel.clearPlayerTiles();
            resetWordPlacement();

            // Clear existing tile buttons
            // tilePane.getChildren().clear();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            checkForMessage();
        });
        tryPlaceWordButton.setDisable(placementCelles.isEmpty());

        VBox buttonBox = new VBox(15, tryPlaceWordButton, challengeButton, passTurnButton, quitGameButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Round Buttons
        HBox roundButtonsBox = createButtonsPane();
        roundButtonsBox.setPrefHeight(120);
        roundButtonsBox.setAlignment(Pos.BOTTOM_CENTER);
        // .....................
        Button sortButton = new Button("Sort");
        sortButton.getStyleClass().add("lightblue-button");
        sortButton.setOnAction(event -> {
            // Sort the tileButtons based on their letter labels
            Collections.sort(tileButtons, Comparator.comparing(Button::getText));
            for (Button b : tileButtons) {
                System.out.println(b.getText());
            }

            // Remove the existing tileButtons from the tilePane
            tilePane.getChildren().clear();

            // Add the sorted tileButtons back to the tilePane
            tilePane.getChildren().addAll(tileButtons);
        });

        customRoot.getChildren().addAll(messageLabel,tileBox, buttonBox, roundButtonsBox);

        return customRoot;
    }

    private void setPlacementCells() {
        Tile[][] board = gameViewModel.getCurrentBoard();
        int size = gameViewModel.getWordLength();
        boolean isVer = gameViewModel.isWordVertical();
        int lastRow = gameViewModel.getLastSelectedCellRow();
        int lastCol = gameViewModel.getLastSelectedCellCol();

        if (isVer) {
            for (int i = size; i > 0; i--) {
                if (board[lastRow][lastCol] == null) {
                    Pane cell = (Pane) getCellFromBoard(lastRow, lastCol);
                    placementCelles.push(cell);
                    placementList.add(cell);
                }
                lastRow--;
            }
        } else {
            for (int i = size; i > 0; i--) {
                if (board[lastRow][lastCol] == null) {
                    Pane cell = (Pane) getCellFromBoard(lastRow, lastCol);
                    placementCelles.push(cell);
                    placementList.add(cell);
                }
                lastCol--;
            }
        }
    }

    private void resetWordPlacement() {
        for (Button tb : tileButtons) {
            tb.setDisable(true);
        }
        for (Pane cell : placementList) {
            // Clear all added style classes
            cell.getStyleClass().clear();
            // Clear inline styles
            cell.setStyle("");
            // Add the default style
            cell.getStyleClass().add("cell");
        }
        gameViewModel.clearWord();
        placementCelles.clear();
        placementList.clear();
        selectedCells.clear();
    }

    private HBox createButtonsPane() {
        HBox roundButtonsPane = new HBox(10);
        roundButtonsPane.setAlignment(Pos.CENTER);
        double preferredHeight = 70; // Set your desired height here
        roundButtonsPane.setMinHeight(preferredHeight);
        roundButtonsPane.setPrefHeight(preferredHeight);

        Button settingButton = new Button("\uD83D\uDD27"); // Unicode for the wrench symbol
        settingButton.setFont(EMOJI_FONT);
        settingButton.getStyleClass().add("grey-button");

        Button helpButton = new Button("❓"); // Unicode for the help symbol
        helpButton.getStyleClass().add("purple-button");

        roundButtonsPane.getChildren().addAll(helpButton, settingButton);

        return roundButtonsPane;
    }

    private HBox createSubmitButtons(String buttonText, double buttonWidth) {
        // Submit button
        Button connectButton = new Button(buttonText);
        connectButton.getStyleClass().add("blue-button");
        connectButton.setPrefHeight(90);
        connectButton.setPrefWidth(buttonWidth);

        // Go Back button
        Button goBackButton = new Button("\u2B05");
        goBackButton.getStyleClass().add("green-button");
        goBackButton.setFont(EMOJI_FONT);
        goBackButton.setOnAction(e -> {
            VBox initialWindowBox = createInitialWindow();
            Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);
            initialWindowBox.setCursor(Cursor.HAND);
            initialWindowBox.getStylesheets().add(CSS_STYLESHEET);
            this.primaryStage.setScene(initialWindowScene);
        });

        HBox submitButtons = new HBox(10, goBackButton, connectButton);
        submitButtons.setAlignment(Pos.CENTER);

        return submitButtons;
    }

    private Node getCellFromBoard(int row, int col) {
        for (Node node : gameBoard.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return node;
            }
        }
        return null;
    }

}
