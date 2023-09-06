package app.view;

import app.view_model.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameView {

    private GameViewModel gameViewModel;

    public GameView(GameViewModel viewModel) {
        this.gameViewModel = viewModel;
        // Initialize and set up the initial UI components here.
    }

    private VBox createSidebar() {

        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setAlignment(Pos.CENTER);

        // My Name,Score,Turn Board
        Pane myInfoBoard = createInfoBoard(gameViewModel.getPlayerNameProperty(),
                gameViewModel.getPlayerScoreProperty(), gameViewModel.isMyTurn(), false);
        sidebar.getChildren().add(myInfoBoard);

        // My Words
        Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
        ListView<String> wordsListView = new ListView<>(gameViewModel.getPlayerWordsProperty());
        wordsListView.setPrefSize(220, 190);
        wordsListView.setBackground(transparentBackground);

        Label wordsLabel = new Label("My Words:");
        wordsLabel.setAlignment(Pos.CENTER);
        wordsLabel.getStyleClass().add("login-label");

        Pane wordsBox = new Pane(wordsListView);
        wordsBox.getStyleClass().add("my-words-box");
        sidebar.getChildren().addAll(wordsLabel, wordsBox);

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

        // Bag count
        Label bagLabel = new Label("Bag:");
        bagLabel.getStyleClass().add("login-label");
        bagLabel.setAlignment(Pos.CENTER_LEFT);
        Label bagValueLabel = new Label();
        bagValueLabel.textProperty().bind(gameViewModel.getBagCountProperty());
        bagValueLabel.getStyleClass().add("bag-label");
        ImageView bagIcon = new ImageView(new Image("icons/bag-icon.png"));
        bagIcon.setFitWidth(60); // Set the desired width
        bagIcon.setFitHeight(60);
        HBox bagCountBox = new HBox(bagIcon, bagValueLabel);
        // bagCountBox.getStyleClass().add("bag-image");
        bagCountBox.setAlignment(Pos.CENTER);

        // Game Books
        Button bookListButton = new Button("Game Books");
        bookListButton.getStyleClass().add("darkgreen-button");
        // bookListButton.setOnAction(event -> showBookSelectionWindow(false)); ****************************************************************************************
        HBox gameBooksBox = new HBox(bookListButton);
        gameBooksBox.setAlignment(Pos.CENTER);

        VBox bookBagBox = new VBox(10, bagCountBox, gameBooksBox);
        bookBagBox.setPadding(new Insets(40, 0, 50, 0)); // 20 units of padding at top and bottom

        sidebar.getChildren().add(bookBagBox);

        return sidebar;
    }

    private Pane createInfoBoard(ObservableValue<String> name, ObservableValue<String> score, boolean turn,
            boolean otherPlayer) {

        double prefH, prefW;
        prefH = otherPlayer ? 60 : 130;
        prefW = 230;

        // Player Name
        Label nameValueLabel = new Label();
        nameValueLabel.textProperty().bind(name);
        String nameStyle = otherPlayer ? "other-name-label" : "my-name-label";
        nameValueLabel.getStyleClass().add(nameStyle);

        ImageView turnIcon = new ImageView(new Image("icons/arrow-icon2.png"));
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
        // Negative score
        if (score.getValue().startsWith("-")) {
            scoreValueLabel.setStyle("-fx-text-fill: red");
        } else {
            String scoreColor = otherPlayer ? "-fx-text-fill: rgb(0, 174, 255);" : "-fx-text-fill: rgb(0, 174, 255);";
            scoreValueLabel.setStyle(scoreColor);
        }
        scoreValueLabel.getStyleClass().add(scoreStyle);
        ///
        ImageView scoreIcon = new ImageView(new Image("icons/star-icon.png"));
        double size = otherPlayer ? 20 : 50;
        scoreIcon.setFitWidth(size); // Set the desired width
        scoreIcon.setFitHeight(size);
        // iconImageView.setPreserveRatio(true);
        HBox scoreBox = new HBox(scoreIcon, scoreValueLabel);
        scoreBox.setAlignment(Pos.CENTER);

        String backgroundStyle = otherPlayer ? "other-score-board" : "wood-score-board";

        if (otherPlayer) {
            HBox myInfoBoard = new HBox(10, nameBox, scoreBox);
            myInfoBoard.getStyleClass().add(backgroundStyle);
            if (turn)
                myInfoBoard.getStyleClass().add("glow-button");
            myInfoBoard.setMinSize(prefW, prefH);
            myInfoBoard.setMaxSize(prefW, prefH);
            myInfoBoard.setAlignment(Pos.CENTER);

            return myInfoBoard;
        } else {
            VBox myInfoBoard = new VBox(-10, nameBox, scoreBox);
            myInfoBoard.getStyleClass().add(backgroundStyle);
            if (turn)
                myInfoBoard.getStyleClass().add("glow-button");
            myInfoBoard.setMinSize(prefW, prefH);
            myInfoBoard.setMaxSize(prefW, prefH);
            myInfoBoard.setAlignment(Pos.CENTER);

            return myInfoBoard;
        }

    }
}
