package app.view_model.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.model.host.HostModel;
import app.view_model.GameViewModel;

public class ViewModelTest {
    public static void main(String[] args) {
        // Set GameViewModel
        GameViewModel gvm = new GameViewModel();
        gvm.setGameMode("G", 0);
        // Set HostModel running the host server in a different thread
        ExecutorService es = Executors.newSingleThreadExecutor();
        HostModel hm = HostModel.get();
        es.execute(() -> {
            hm.setNumOfPlayers(2);
            hm.connectMe("Aviv", "localhost", 11224);
            hm.myBooksChoice("Alice in Wonderland");
            hm.ready();
        });
        // Set guest view-model
        gvm.connectMe("Aviv", "localhost", 8040);
        gvm.myBookChoice("Harray Potter");
        gvm.ready();

       
        System.out.println();
        System.err.println(gvm.getCurrentBoard() != null);
        System.err.println(gvm.getOthersScores() != null);
        System.err.println(gvm.getPlayerNameProperty() != null);
        System.err.println(gvm.getPlayerScoreProperty() != null);
        System.err.println(gvm.getPlayerTiles() != null);
        System.err.println(gvm.getPlayerTurnProperty() != null);
        System.err.println(gvm.getPlayerWords() != null);

        gvm.quitGame();
        hm.quitGame();

    }
}
