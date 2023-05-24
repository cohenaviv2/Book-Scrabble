package model;

public interface GameModel {
    void addPlayer(String name);
    int query(String word);
    int challenge(String word);
    void skipTurn(int id);
}
