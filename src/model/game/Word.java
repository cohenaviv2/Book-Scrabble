package model.game;

import java.io.Serializable;
import java.util.Arrays;

/*
 * Represents a possible placement of a word on the game board
 * each Word contains starting index on the board(row,col)
 * and a vertical boolean which is false if the Word is horizontal.
 * 
 * @author: Aviv Cohen
 * 
 */

public class Word implements Serializable{

    private Tile[] tiles;
    private int row, col;
    private boolean vertical;

    public Word(Tile[] tiles, int row, int col, boolean vertical) {
        this.tiles = tiles;
        this.row = row;
        this.col = col;
        this.vertical = vertical;
    }

    public Word(Word other) {
        if (this != other && other != null) {
                this.tiles = other.getTiles();
                this.row = other.getRow();
                this.col = other.getCol();
                this.vertical = other.isVertical();
        }
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isVertical() {
        return vertical;
    }

    @Override
    public String toString() {
        String word ="";
        for (Tile t : tiles){
            word+=t.getLetter();
        }
        return word;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(tiles);
        result = prime * result + row;
        result = prime * result + col;
        result = prime * result + (vertical ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Word other = (Word) obj;
        if (!Arrays.equals(tiles, other.tiles))
            return false;
        if (row != other.row)
            return false;
        if (col != other.col)
            return false;
        if (vertical != other.vertical)
            return false;
        return true;
    }

}
