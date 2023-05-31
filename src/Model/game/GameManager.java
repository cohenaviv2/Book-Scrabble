package model.game;

import java.util.Map;

import model.game.Tile.Bag; 

public class GameManager {
    
    private Board gameBoard;
    private Bag bag;
    private Map<String,String> gameBooks;
    private StringBuilder bookList;
    private int turnIndex;

    private Player hostPlayer;
    private Map<Integer,Player> guestsByID;
    private Map<String,Player> guestsByName;

}
