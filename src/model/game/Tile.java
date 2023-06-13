package model.game;

import java.io.Serializable;
import java.util.Random;

/*
 * Represents a tile in the game
 * each tile contains letter and score
 * a tile is immutable(final) and created once.
 * 
 * @author: Aviv Cohen
 * 
 */

public class Tile implements Serializable {

    public final char letter;
    public final int score;

    private Tile(char letter, int score) {
        this.letter = letter;
        this.score = score;
    }

    public char getLetter() {
        return letter;
    }

    public int getScore() {
        return score;
    }
    
    /* Bag */
    /*
     * Game bag, Contains 98 tiles and array of quantities
     * this is the only class that can create tiles.
     * 
     * @author: Aviv Cohen
     * 
     */

    public static class Bag {

        private int[] quantities;
        private Tile[] bagTiles;
        private static Bag bagInstance = null; // Singleton

        private Bag() {
            this.quantities = new int[26];
            this.bagTiles = new Tile[26];

            /* initialize */
            this.quantities = new int[] { 9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2,
                    1 };

            this.bagTiles[0] = new Tile('A', 1);
            this.bagTiles[1] = new Tile('B', 3);
            this.bagTiles[2] = new Tile('C', 3);
            this.bagTiles[3] = new Tile('D', 2);
            this.bagTiles[4] = new Tile('E', 1);
            this.bagTiles[5] = new Tile('F', 4);
            this.bagTiles[6] = new Tile('G', 2);
            this.bagTiles[7] = new Tile('H', 4);
            this.bagTiles[8] = new Tile('I', 1);
            this.bagTiles[9] = new Tile('J', 8);
            this.bagTiles[10] = new Tile('K', 5);
            this.bagTiles[11] = new Tile('L', 1);
            this.bagTiles[12] = new Tile('M', 3);
            this.bagTiles[13] = new Tile('N', 1);
            this.bagTiles[14] = new Tile('O', 1);
            this.bagTiles[15] = new Tile('P', 3);
            this.bagTiles[16] = new Tile('Q', 10);
            this.bagTiles[17] = new Tile('R', 1);
            this.bagTiles[18] = new Tile('S', 1);
            this.bagTiles[19] = new Tile('T', 1);
            this.bagTiles[20] = new Tile('U', 1);
            this.bagTiles[21] = new Tile('V', 4);
            this.bagTiles[22] = new Tile('W', 4);
            this.bagTiles[23] = new Tile('X', 8);
            this.bagTiles[24] = new Tile('Y', 4);
            this.bagTiles[25] = new Tile('Z', 10);
        }

        public Tile getRand() {
            /* Pull a random tile from the bag */

            Random r = new Random();
            int rand = r.nextInt(26); // Random tile index

            if (this.quantities[rand] > 0) {
                this.quantities[rand]--;
                return this.bagTiles[rand];
            } else { // Modulo loop
                int i = rand;
                while ((i + 1) % 26 != rand) {
                    if (this.quantities[(i + 1) % 26] > 0) {
                        this.quantities[(i + 1) % 26]--;
                        return this.bagTiles[(i + 1) % 26];
                    }
                    i++;
                }
            }
            return null; /* Bag is empty */
        }

        public Tile getTile(char letter) {
            /* Pulls the requested tile(letter) from the bag */

            if ((int) letter < 65 || (int) letter > 90) // must be a Capital letter
                return null;

            int i = (int) (letter - 'A');
            if (this.quantities[i] > 0) {
                this.quantities[i]--;
                return this.bagTiles[i];
            }

            return null; /* Letter is empty */
        }

        public void put(Tile t) {
            /*
             * Puts back the Tile into the bag and updates the quantities
             * can not put back if the quantity of this tile is max
             */
            switch (t.letter) {
                case 'A':
                    if (this.quantities[0] < 9)
                        this.quantities[0]++;
                    break;
                case 'B':
                    if (this.quantities[1] < 2)
                        this.quantities[1]++;
                    break;
                case 'C':
                    if (this.quantities[2] < 2)
                        this.quantities[2]++;
                    break;
                case 'D':
                    if (this.quantities[3] < 4)
                        this.quantities[3]++;
                    break;
                case 'E':
                    if (quantities[4] < 12)
                        quantities[4]++;
                    break;
                case 'F':
                    if (this.quantities[5] < 2)
                        this.quantities[5]++;
                    break;
                case 'G':
                    if (this.quantities[6] < 3)
                        this.quantities[6]++;
                    break;
                case 'H':
                    if (this.quantities[7] < 2)
                        this.quantities[7]++;
                    break;
                case 'I':
                    if (this.quantities[8] < 9)
                        this.quantities[8]++;
                    break;
                case 'J':
                    if (this.quantities[9] < 1)
                        this.quantities[9]++;
                    break;
                case 'K':
                    if (this.quantities[10] < 1)
                        this.quantities[10]++;
                    break;
                case 'L':
                    if (this.quantities[11] < 4)
                        this.quantities[11]++;
                    break;
                case 'M':
                    if (this.quantities[12] < 2)
                        this.quantities[12]++;
                    break;
                case 'N':
                    if (this.quantities[13] < 6)
                        this.quantities[13]++;
                    break;
                case 'O':
                    if (this.quantities[14] < 8)
                        this.quantities[14]++;
                    break;
                case 'P':
                    if (this.quantities[15] < 2)
                        this.quantities[15]++;
                    break;
                case 'Q':
                    if (this.quantities[16] < 1)
                        this.quantities[16]++;
                    break;
                case 'R':
                    if (this.quantities[17] < 6)
                        this.quantities[17]++;
                    break;
                case 'S':
                    if (this.quantities[18] < 4)
                        this.quantities[18]++;
                    break;
                case 'T':
                    if (this.quantities[19] < 6)
                        this.quantities[19]++;
                    break;
                case 'U':
                    if (this.quantities[20] < 4)
                        this.quantities[20]++;
                    break;
                case 'V':
                    if (this.quantities[21] < 2)
                        this.quantities[21]++;
                    break;
                case 'W':
                    if (this.quantities[22] < 2)
                        this.quantities[22]++;
                    break;
                case 'X':
                    if (this.quantities[23] < 1)
                        this.quantities[23]++;
                    break;
                case 'Y':
                    if (this.quantities[24] < 2)
                        this.quantities[24]++;
                    break;
                case 'Z':
                    if (this.quantities[25] < 1)
                        this.quantities[25]++;
                    break;
            }
        }

        public int size() {
            int size = 0;
            for (int i : quantities)
                size += i;

            return size;
        }

        public int[] getQuantities() {
            /* returns a copy of the quantities array */

            int[] quantitiesCopy = new int[26];
            System.arraycopy(quantities, 0, quantitiesCopy, 0, 26);

            return quantitiesCopy;
        }

        public static Bag getBag() {
            if (bagInstance == null)
                bagInstance = new Bag();
            return bagInstance;
        }

    }

    // Hash & Equals:
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + letter;
        result = prime * result + score;
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
        Tile other = (Tile) obj;
        if (letter != other.letter)
            return false;
        if (score != other.score)
            return false;
        return true;
    }

}
