package app.view;

import app.model.game.*;
import app.model.host.HostModel;
import java.io.*;
import java.net.*;
import java.util.*;
import app.view_model.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
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

    private final String CSS_STYLESHEET = getClass().getResource("/style.css").toExternalForm();
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

        Button closeButton = new Button(this.X);
        closeButton.setFont(EMOJI_FONT);
        closeButton.getStyleClass().add("red-button");
        // closeButton.setMinHeight(40);
        // closeButton.setPrefHeight(40);

        closeButton.setOnAction(event -> {
            if (gameIsRunning)
                openCustomWindow(null, "", "", 450, 200, true);
            else {
                Stage stage = (Stage) primaryStage.getScene().getWindow();
                stage.close();
                System.exit(0);
            }
        });

        Button minimizeButton = new Button("_");
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

    private Button openCustomWindow(Node content, String buttonColor, String ButtonText, double customWidth,
            double customHeight, boolean isQuitGame) {
        Stage customStage = new Stage();
        customStage.initStyle(StageStyle.UNDECORATED);

        // Calculate the center coordinates of the main stage
        double mainStageX = primaryStage.getX();
        double mainStageY = primaryStage.getY();
        double mainStageWidth = primaryStage.getWidth();
        double mainStageHeight = primaryStage.getHeight();

        double customStageWidth = customWidth; // Adjust as needed
        double customStageHeight = customHeight; // Adjust as needed

        double customStageX = mainStageX + (mainStageWidth - customStageWidth) / 2;
        double customStageY = mainStageY + (mainStageHeight - customStageHeight) / 2;

        customStage.setX(customStageX);
        customStage.setY(customStageY);

        Scene customScene;
        VBox customRoot = new VBox(30);
        customRoot.setAlignment(Pos.CENTER);
        customRoot.getStyleClass().addAll("content-background");

        Button button;

        if (isQuitGame) {
            Button yesButton = new Button("Yes");
            yesButton.getStyleClass().add("blue-button");
            yesButton.setOnAction(event -> {
                this.gameViewModel.quitGame();
                customStage.close();
                Stage stage = (Stage) primaryStage.getScene().getWindow();
                stage.close();
                System.exit(0);
            });

            button = new Button("No");
            button.getStyleClass().add("red-button");
            button.setOnAction(event -> customStage.close());

            HBox buttons = new HBox(10, yesButton, button);
            buttons.setAlignment(Pos.CENTER);

            Text quitGameText = new Text("Are you sure you want to quit game?");
            quitGameText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            quitGameText.setTextAlignment(TextAlignment.CENTER);

            customRoot.getChildren().addAll(quitGameText, buttons);

            customScene = new Scene(customRoot, customStageWidth, customStageHeight);
        } else {
            button = new Button(ButtonText);
            button.getStyleClass().add(buttonColor + "-button");
            button.setOnAction(event -> {
                customStage.close();
            });

            StackPane buttonPane = new StackPane(button);
            BorderPane.setAlignment(buttonPane, Pos.TOP_CENTER);
            // buttonPane.setPadding(new Insets(30, 0, 0, 0)); // Add padding to the top

            customRoot.getChildren().addAll(content, buttonPane);

            customScene = new Scene(customRoot, customStageWidth, customStageHeight);
        }

        customScene.getStylesheets().add(CSS_STYLESHEET);
        customScene.setCursor(Cursor.HAND);
        customStage.setScene(customScene);
        customStage.show();

        return button;
    }

    public VBox createInitialWindow() {
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

        helpButton.setOnAction(e -> openCustomWindow(centerContent, "red", this.X, 650, 400, false));

        // Event handler for hostButton
        hostButton.setOnAction(event -> {
            GAME_MODE = "H";
            showHostLoginForm();
        });

        // Event handler for guestButton
        guestButton.setOnAction(event -> {
            GAME_MODE = "G";
            gameViewModel.setGameMode(GAME_MODE, 0);
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

            bookBox.setOnMouseClicked(event -> {
                if (selectedBooks.contains(book)) {
                    selectedBooks.remove(book);
                    imageView.getStyleClass().remove("selected-book-image");
                } else {
                    selectedBooks.add(book);
                    imageView.getStyleClass().add("selected-book-image");
                }
            });

            bookContainer.getChildren().add(bookBox);
        }

        scrollPane.setContent(bookContainer);

        HBox buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);

        Button doneButton = new Button("Done");
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

        // OS Bar
        HBox osBar = createOsBar(false);

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
        Label selectBookLabel = new Label("Select Books:");
        selectBookLabel.getStyleClass().add("login-label");
        Button booksButton = new Button("Select Books");
        booksButton.getStyleClass().add("red-button");
        booksButton.setOnAction(e -> showBookSelectionWindow());

        Label waintingLabel = new Label("");

        // Game server connection check
        Text gameServerTitle = new Text("Game server");
        gameServerTitle.setTextAlignment(TextAlignment.CENTER);
        gameServerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Text contentLabel = new Text(
                "The game server is responsible for checking whether\nthe word is legal in terms of the book dictionary.\nGame Server is uploaded and\npowered by Oracle Cloud\nOn Ubuntu 22.04 VM");
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
        customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
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
        invalidPortTxt.setFont(Font.font("Arial", 14));
        Button setPortButton = new Button("Set port");
        setPortButton.getStyleClass().add("blue-button");
        portField.setOnMouseClicked(e -> invalidPortTxt.setText(""));
        setPortButton.setOnAction(e -> {
            String portText = portField.getText();
            if (isValidPort(portText)) {
                this.CUSTOM_PORT = Integer.parseInt(portText);
                // invalidPortTxt.setText(""); // Clear error message if valid
                portField.setDisable(true);
                setPortButton.setDisable(true);
                invalidPortTxt.setText("Port is set!");
                invalidPortTxt.setFill(Color.BLUE);
            } else {
                invalidPortTxt.setText(
                        "Please enter a valid port number.\nThe port should be a number with a maximum 5 digits");
                invalidPortTxt.setFill(Color.RED);
            }
        });

        VBox customPortSettings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton,
                invalidPortTxt);
        customPortSettings.setAlignment(Pos.CENTER);

        // Whats my IP
        Text myIpText = new Text("Get your IP address and send it to your friend");
        myIpText.setFont(new Font(16));
        myIpText.setTextAlignment(TextAlignment.CENTER);
        TextField myIpField = new TextField("");
        myIpField.getStyleClass().add("text-field");
        myIpField.setAlignment(Pos.CENTER);
        myIpField.setMaxWidth(200);
        Button myIpButton = new Button("What's My IP");
        myIpButton.getStyleClass().add("yellow-button");
        myIpButton.setOnAction(e -> {
            try {
                myIpField.setText(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e1) {
            }
        });

        VBox myIpSettings = new VBox(10, myIpText, myIpButton, myIpField);
        myIpSettings.setAlignment(Pos.CENTER);

        VBox settings = new VBox(10, gameServerSettings, customPortSettings, myIpSettings);
        settings.setAlignment(Pos.CENTER);

        HBox roundButtonsPane = createButtonsPane();
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        Text hostModeExp = new Text(txtExplanations.get("host-mode"));
        hostModeExp.setTextAlignment(TextAlignment.CENTER);
        // hostModeExp.setFont(new Font(14));
        hostModeExp.getStyleClass().add("content-label");
        helpButton.setOnAction(e -> openCustomWindow(hostModeExp, "red", this.X, 650, 500, false));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        settingsButton.setOnAction(e -> openCustomWindow(settings, "red", this.X, 500, 750, false));

        // SET GAME MODE
        gameViewModel.setGameMode(GAME_MODE, numOfPlayersComboBox.getValue());

        // Submit buttons
        HBox submitButtons = createSubmitButtons("Start game", 160);
        Button nextButton = (Button) submitButtons.getChildren().get(1);

        // CONNECT ME
        nextButton.setOnAction(event -> {
            MY_NAME = nameTextField.getText();
            if (selectedBooks.size() != 0) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                try {
                    gameViewModel.connectMe(MY_NAME, "0", CUSTOM_PORT);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } // 0 = default - ORACLE_GAME_SERVER
                gameViewModel.myBookChoice(MY_BOOKS_SERILIZED);
                gameViewModel.ready();

                loginFormBox.setDisable(true);

                // Disable form elements
                // numOfPlayersComboBox.setDisable(true);
                // booksButton.setDisable(true);
                // nameLabel.setDisable(true);
                // gameServerlabel.setText("");
                // gameServerlabel.setDisable(true);
                // nameTextField.setDisable(true);
                // numOfPlayersLabel.setDisable(true);
                // waintingLabel.setText("Waiting for all players to connect");
                // waintingLabel
                // .setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill:
                // lightgoldenrodyellow;");
                // waintingLabel.setTextFill(Color.BLACK);
                // nameTextField.setDisable(true);
                // nextButton.setDisable(true);

            }
        });

        VBox root = new VBox(osBar, loginFormBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(22);
        root.getStyleClass().add("wood-background");

        loginFormBox.getChildren().addAll(nameLabel,
                nameTextField, selectBookLabel, booksButton, numOfPlayersLabel, numOfPlayersComboBox, roundButtonsPane,
                waintingLabel,
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

        // Custom port
        Text customPortTitle = new Text("Hosts Custom Port");
        customPortTitle.setTextAlignment(TextAlignment.CENTER);
        customPortTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Text customPortLabel = new Text(txtExplanations.get("custom-port"));
        customPortLabel.setFont(new Font(14));
        customPortLabel.setTextAlignment(TextAlignment.CENTER);
        TextField portField = new TextField("");
        portField.getStyleClass().add("text-field");
        portField.setAlignment(Pos.CENTER);
        portField.setMaxWidth(120);
        Text invalidPortTxt = new Text("");
        invalidPortTxt.setTextAlignment(TextAlignment.CENTER);
        invalidPortTxt.setFont(Font.font("Arial", 12));
        Button setPortButton = new Button("Set port");
        setPortButton.getStyleClass().add("blue-button");
        portField.setOnMouseClicked(e -> invalidPortTxt.setText(""));
        setPortButton.setOnAction(e -> {
            String portText = portField.getText();
            if (isValidPort(portText)) {
                this.CUSTOM_PORT = Integer.parseInt(portText);
                // invalidPortTxt.setText(""); // Clear error message if valid
                portField.setDisable(true);
                setPortButton.setDisable(true);
                invalidPortTxt.setText("Port is set!");
                invalidPortTxt.setFill(Color.BLUE);
            } else {
                invalidPortTxt.setText(
                        "Please enter a valid port number. The port should be a number with a maximum 5 digits");
                invalidPortTxt.setFill(Color.RED);
            }
        });

        VBox settings = new VBox(10, customPortTitle, customPortLabel, portField, setPortButton, invalidPortTxt);
        settings.setAlignment(Pos.CENTER);

        // Round buttons pane
        HBox roundButtonsPane = createButtonsPane();
        Button helpButton = (Button) roundButtonsPane.getChildren().get(0);
        Text hostModeExp = new Text(txtExplanations.get("guest-mode"));
        hostModeExp.setTextAlignment(TextAlignment.CENTER);
        hostModeExp.setFont(new Font(14));
        helpButton.setOnAction(e -> openCustomWindow(hostModeExp, "red", this.X, 500, 350, false));
        Button settingsButton = (Button) roundButtonsPane.getChildren().get(1);
        settingsButton.setOnAction(e -> openCustomWindow(settings, "red", this.X, 500, 500, false));

        Label waintingLabel = new Label("");

        // Submit buttons
        HBox submitButtons = createSubmitButtons("Connect", 140);
        Button connectButton = (Button) submitButtons.getChildren().get(1);

        // SET GAME MODE
        gameViewModel.setGameMode(GAME_MODE, 0); // 0 = default for guest

        connectButton.setOnAction(event -> {
            MY_NAME = nameTextField.getText();
            String ip = ipTextField.getText();
            // int port = Integer.parseInt(portTextField.getText());
            if (selectedBooks.size() != 0) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                if (CUSTOM_PORT == 0) {
                    CUSTOM_PORT = HostModel.HOST_SERVER_PORT;
                }
                try {
                    gameViewModel.connectMe(MY_NAME, ip, CUSTOM_PORT);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                gameViewModel.myBookChoice(MY_BOOKS_SERILIZED);
                gameViewModel.ready();

                loginFormBox.setDisable(true);

                // Disable form elements
                // nameTextField.setDisable(true);
                // ipTextField.setDisable(true);
                // portTextField.setDisable(true);
                // connectButton.setDisable(true);
                // waintingLabel.setText("Waiting for all players to connect");
                // waintingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                // waintingLabel.setTextFill(Color.BLACK);
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
        primaryStage.setScene(guestLoginFormScene);
    }

    public void showGameFlowWindow() {
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
        //     nameBox.getChildren().add(turnIcon);
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

        VBox myInfoBoard = createInfoBoard(gameViewModel.getPlayerNameProperty(), gameViewModel.getPlayerScoreProperty(), gameViewModel.isMyTurn(), false);

        // Bag count
        Label bagLabel = new Label("Bag:");
        Label bagValueLabel = new Label();
        bagValueLabel.textProperty().bind(gameViewModel.getBagCountProperty());
        bagLabel.getStyleClass().add("login-label");
        bagValueLabel.getStyleClass().add("score-label");
        bagValueLabel.setTextFill(Color.NAVY);
        HBox bagCountBox = new HBox(10, bagLabel, bagValueLabel);

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

        // Game Books
        Label booksLabel = new Label("Game Books:");
        ListView<String> bookListView = new ListView<>();
        bookListView.setItems(gameViewModel.getGameBooksProperty());

        // Player Words
        Label wordsLabel = new Label("My Words:");
        ListView<String> wordsListView = new ListView<>();
        wordsListView.setItems(gameViewModel.getPlayerWordsProperty());

        // Other players info
        Label othersScoreLabel = new Label("Other Players:");
        othersScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        ListView<String> othersScoreListView = new ListView<>();
        othersScoreListView.setItems(gameViewModel.getOthersInfoProperty());

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
        // "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;
        // -fx-background-color: darkblue;");
        // setPadding(new Insets(5));
        // }
        // }
        // });

        sidebar.getChildren().addAll(
                myInfoBoard);

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
                        openCustomWindow(turnText, "blue", "OK", 400, 200, false);
                    }
                });

                gameBoard.add(cellPane, col, row);
            }
        }

        return gameBoard;
    }

    private VBox createInfoBoard(ObservableValue<String> name, ObservableValue<String> score, boolean turn, boolean otherPlayer){
        // Player Name
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(name);
        nameValueLabel.getStyleClass().add("my-name-label");

        ImageView turnIcon = new ImageView(new Image("turn-icon.png"));
        turnIcon.setFitWidth(40); // Set the desired width
        turnIcon.setFitHeight(40);

        HBox nameBox = new HBox();
        if(turn){
            nameBox.getChildren().add(turnIcon);
        }
        nameBox.getChildren().add(nameValueLabel);
        nameBox.setAlignment(Pos.CENTER);

        // Player Score
        Label scoreLabel = new Label("Score:");
        scoreLabel.getStyleClass().add("login-label");
        Label scoreValueLabel = new Label();
        scoreValueLabel.textProperty().bind(score);
        scoreValueLabel.getStyleClass().add("score-label");
        ///
        ImageView scoreIcon = new ImageView(new Image("star-icon.png"));
        scoreIcon.setFitWidth(40); // Set the desired width
        scoreIcon.setFitHeight(40);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIcon, scoreValueLabel);
        scoreBox.setAlignment(Pos.CENTER);

        VBox myInfoBoard = new VBox(nameBox, scoreBox);
        myInfoBoard.getStyleClass().add("wood-score-board");
        myInfoBoard.setMinSize(250, 120);
        myInfoBoard.setMaxSize(250, 120);
        myInfoBoard.setAlignment(Pos.CENTER);

        return myInfoBoard;
    }

    private VBox createButtons() {
        VBox buttonsBox = new VBox(10);
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.setAlignment(Pos.CENTER);

        Label messageLabel = new Label(MessageReader.getMsg());
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create a FlowPane to hold the tile buttons
        FlowPane tilePane = new FlowPane(10, 10);
        tilePane.setAlignment(Pos.CENTER);

        // Clear existing tile buttons
        tilePane.getChildren().clear();

        // Create the tile buttons and add them to the FlowPane
        Button resetTurnButton = new Button("\u2B8C"); // \u2B6F ⟲ ⭯

        tileButtons = gameViewModel.getButtonTilesProperty();
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
                    resetTurnButton.setDisable(false);

                    // tilePane.getChildren().remove(tileButton);

                    // Add the tile value to the word
                    gameViewModel.addToWord(letter);
                }
            });

            tilePane.getChildren().add(tileButton);
        }

        // Reset button
        resetTurnButton.getStyleClass().add("grey-button");
        resetTurnButton.setDisable(true);
        resetTurnButton.setOnAction(event -> {
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
            resetTurnButton.setDisable(true);
        });
        // Set the preferred width and height of the button
        resetTurnButton.setPrefWidth(60); // Adjust the width as desired
        resetTurnButton.setPrefHeight(20); // Adjust the height as desired

        Button passTurnButton = new Button(
                "Pass Turn");
        passTurnButton.getStyleClass().add("yellow-button");
        passTurnButton.setOnAction(event -> {
            // Call the method to handle the "Pass Turn" action
            gameViewModel.skipTurn();
        });

        Button challengeButton = new Button(
                "Challenge");
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

        Button quitGameButton = new Button(
                "Quit Game");
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

            // Call the method to handle the "Try Place Word" action with the collected data
            try {
                gameViewModel.tryPlaceWord(word);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // gameViewModel.clearPlayerTiles();
            resetWordPlacement();

            // Clear existing tile buttons
            // tilePane.getChildren().clear();

        });
        tryPlaceWordButton.setDisable(placementCelles.isEmpty());

        // .....................
        HBox roundButtonsPane = createButtonsPane();
        roundButtonsPane.setPrefHeight(100);
        roundButtonsPane.setAlignment(Pos.BOTTOM_CENTER);

        // .....................

        buttonsBox.getChildren().addAll(messageLabel, tilePane, resetTurnButton, tryPlaceWordButton, passTurnButton,
                challengeButton, quitGameButton, roundButtonsPane);

        return buttonsBox;
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
        connectButton.setPrefHeight(60);
        connectButton.setPrefWidth(buttonWidth);

        // Go Back button
        Button goBackButton = new Button("\u2B05");
        goBackButton.getStyleClass().add("green-button");
        goBackButton.setFont(EMOJI_FONT);
        goBackButton.setOnAction(e -> {
            VBox initialWindowBox = createInitialWindow();
            Scene initialWindowScene = new Scene(initialWindowBox, 600, 480);
            initialWindowBox.setCursor(Cursor.HAND);
            initialWindowBox.getStylesheets().add(getStyleSheet());
            this.primaryStage.setScene(initialWindowScene);
        });

        HBox submitButtons = new HBox(10, goBackButton, connectButton);
        submitButtons.setAlignment(Pos.CENTER);

        return submitButtons;
    }

    public String getStyleSheet() {
        return this.CSS_STYLESHEET;
    }

    private boolean isValidPort(String portText) {
        try {
            int portNumber = Integer.parseInt(portText);
            return portNumber >= 0 && portNumber <= 65535 && portText.length() <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Node getCellFromBoard(int row, int col) {
        for (Node node : gameBoard.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return node;
            }
        }
        return null;
    }

    public void showAlert(String alert) {
        Text message = new Text(alert);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        VBox textBox = new VBox(10, message);
        textBox.setAlignment(Pos.CENTER);
        Button submitButton = openCustomWindow(textBox, "blue", "OK", 800, 300, false);
        submitButton.setOnAction(e -> {
            ///////////////////////////////////////////////////////////////////
            if (this.GAME_MODE.equals("H")) {
                showHostLoginForm();
            } else
                showGuestLoginForm();
        });
    }

}