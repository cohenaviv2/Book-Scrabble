package view_model;

import java.util.Observable;
import java.util.Observer;
import model.GameModel;

public class ViewModel implements Observer {
    GameModel m;
    
    public ViewModel(GameModel m) {
        this.m = m;
        
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}
