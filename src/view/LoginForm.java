package view;
import model.game.GameManager;
import view_model.GameViewModel;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class LoginForm extends Application {
    private GameViewModel gameViewModel;

    @Override
    public void start(Stage primaryStage) {

        // Create UI components for the login form
        RadioButton hostRadioButton = new RadioButton("Host");
        RadioButton guestRadioButton = new RadioButton("Guest");
        TextField nameTextField = new TextField();
        TextField ipTextField = new TextField();
        TextField portTextField = new TextField();
        ListView<String> bookListView = new ListView<>(FXCollections.observableArrayList(GameManager.get().getFullBookList().keySet()));
        Button connectButton = new Button("Connect me");

        // Create a ToggleGroup for the radio buttons
        ToggleGroup toggleGroup = new ToggleGroup();
        hostRadioButton.setToggleGroup(toggleGroup);
        guestRadioButton.setToggleGroup(toggleGroup);

        // Set the default selected radio button
        hostRadioButton.setSelected(true);

        // Create a VBox to hold the login form components
        VBox loginFormBox = new VBox(10);
        loginFormBox.setPadding(new Insets(20));
        loginFormBox.setAlignment(Pos.CENTER);
        loginFormBox.getChildren().addAll(
                new Label("Choose Game Mode:"),
                hostRadioButton,
                guestRadioButton,
                new Label("Name:"),
                nameTextField,
                new Label("IP:"),
                ipTextField,
                new Label("Port:"),
                portTextField,
                new Label("Select a Book:"),
                bookListView,
                connectButton
        );

        // Create a new ComboBox for selecting the number of players
        ComboBox<Integer> numOfPlayersComboBox = new ComboBox<>();
        numOfPlayersComboBox.getItems().addAll(2, 3, 4); // Add the desired number of players options

        // Event handler for Host radio button selection
        hostRadioButton.setOnAction(event -> {
            if (hostRadioButton.isSelected()) {
                loginFormBox.getChildren().add(new Label("Number of Players:"));
                loginFormBox.getChildren().add(numOfPlayersComboBox);
            } else {
                loginFormBox.getChildren().removeAll(new Label("Number of Players:"), numOfPlayersComboBox);
            }
        });

        // Create the login form scene
        Scene loginFormScene = new Scene(loginFormBox, 300, 600);

        // Set up the primaryStage
        primaryStage.setTitle("Login Form");
        primaryStage.setScene(loginFormScene);
        primaryStage.show();

        // Set up event handling for the Connect button
        connectButton.setOnAction(event -> {
            String name = nameTextField.getText();
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Call the corresponding ViewModel method to handle the "Connect me" action
                if (hostRadioButton.isSelected()) {
                    int numOfPlayers = numOfPlayersComboBox.getValue();
                    gameViewModel.setGameMode("H");
                    gameViewModel.setNumOfPlayer(numOfPlayers);
                    gameViewModel.connectMe(name, ip, port);
                    gameViewModel.myBookChoice(selectedBook);
                    gameViewModel.ready();
                } else if (guestRadioButton.isSelected()) {
                    gameViewModel.setGameMode("G");
                    gameViewModel.connectMe(name, ip, port);
                    gameViewModel.myBookChoice(selectedBook);
                    gameViewModel.ready();
                }

                // Disable the form after connecting
                loginFormBox.setDisable(true);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
