package view_model;

import java.util.*;

import javafx.beans.property.*;
import model.GameModel;
import model.game.*;


public class ViewModel implements Observer {
    GameModel m;
    public StringProperty myName;
    public IntegerProperty myScore;
    public BooleanProperty myTurn;
    public ObjectProperty<Tile[][]> myBoard;
    public ListProperty<Tile> myHandTiles;
    public ListProperty<Word> myWords;
    public MapProperty<String,Integer> playersScore;

    public ViewModel(GameModel m) {
        this.m = m;
        this.myName = new SimpleStringProperty();
        this.myScore = new SimpleIntegerProperty(0);
        this.myTurn = new SimpleBooleanProperty(false);
        this.myBoard = new SimpleObjectProperty<>();
        this.myHandTiles = new SimpleListProperty<>();
        this.myWords = new SimpleListProperty<>();
        this.playersScore = new SimpleMapProperty<>();
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}
