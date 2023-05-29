package model.game;

import java.util.Map;

import model.game.Tile.Bag; 

public class GameManager {
    
    private Board gameBoard;
    private Bag bag;
    private Player hostPlayer;
    private Map<Integer,Player> guestsByID;
    private Map<String,Player> guestsByName;

}
