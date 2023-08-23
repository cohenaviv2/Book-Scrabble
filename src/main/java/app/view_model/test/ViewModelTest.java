package app.view_model.test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.model.host.HostModel;
import app.view_model.GameViewModel;

public class ViewModelTest {
    public static void main(String[] args) {
        // Set GameViewModel
        GameViewModel gvm = new GameViewModel();
        gvm.setGameMode("G");
        // Set HostModel running the host server in a different thread
        ExecutorService es = Executors.newSingleThreadExecutor();
        HostModel hm = HostModel.get();
        es.execute(() -> {
            hm.setNumOfPlayers(2);
            try {
                hm.connectMe("Aviv", "localhost", 11224);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            hm.myBooksChoice("Alice in Wonderland");
            hm.ready();
        });
        // Set guest view-model
            gvm.connectMe("Aviv", "localhost", 8040);
      
        gvm.myBookChoice("Harray Potter");
        gvm.ready();

       
        System.out.println();
        System.err.println(gvm.getCurrentBoard() != null);
        System.err.println(gvm.getOthersInfoProperty() != null);
        System.err.println(gvm.getPlayerNameProperty() != null);
        System.err.println(gvm.getPlayerScoreProperty() != null);
        System.err.println(gvm.getPlayerTilesProperty() != null);
        System.err.println(gvm.getPlayerTurnProperty() != null);
        System.err.println(gvm.getPlayerWordsProperty() != null);

        gvm.quitGame();
        hm.quitGame();

    }
}
