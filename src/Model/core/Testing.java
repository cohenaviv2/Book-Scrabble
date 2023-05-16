package Model.core;

import Model.core.Tile.Bag;

public class Testing {
    private static Tile[] get(String s) {
        Tile[] ts = new Tile[s.length()];
        int i = 0;
        for (char c : s.toCharArray()) {
            ts[i] = Bag.getBag().getTile(c);
            i++;
        }
        return ts;
    }

    public static void main(String[] args) {

        // BOARD & BAG
        Bag bag = Bag.getBag();
        Board b = Board.getBoard();
        b.printBonus();
        System.out.println();
        b.printBoard();
        System.out.println();

        // WORD IS ON BOARD
        Tile[] word = new Tile[7];
        for (int i = 0; i < word.length; i++)
            word[i] = bag.getRand();

        Word w0 = new Word(word, -1, 0, false); // false
        Word w1 = new Word(word, 3, 3, false); // true
        Word w2 = new Word(word, 2, 10, false); // false
        Word w3 = new Word(word, 7, 7, false); // true
        Word w4 = new Word(word, 13, 10, false); // false
        Word w5 = new Word(word, 15, 1, false); // false
        Word w6 = new Word(word, -1, 5, true); // false
        Word w7 = new Word(word, 13, 14, true); // false
        Word w8 = new Word(word, 10, 0, true); // false
        Word w9 = new Word(word, 2, 7, true); // true

        System.out.println(b.isOnBoard(w0));
        System.out.println(b.isOnBoard(w1));
        System.out.println(b.isOnBoard(w2));
        System.out.println(b.isOnBoard(w3));
        System.out.println(b.isOnBoard(w4));
        System.out.println(b.isOnBoard(w5));
        System.out.println(b.isOnBoard(w6));
        System.out.println(b.isOnBoard(w7));
        System.out.println(b.isOnBoard(w8));
        System.out.println(b.isOnBoard(w9));
        System.out.println("------------");

        // FIRST WORD ON THE STAR
        System.out.println(b.boardLegal(w0)); // false
        System.out.println(b.boardLegal(w1)); // false
        System.out.println(b.boardLegal(w2)); // false
        System.out.println(b.boardLegal(w3)); // true
        System.out.println(b.boardLegal(w4)); // false
        System.out.println(b.boardLegal(w5)); // false
        System.out.println(b.boardLegal(w6)); // false
        System.out.println(b.boardLegal(w7)); // false
        System.out.println(b.boardLegal(w8)); // false
        System.out.println(b.boardLegal(w9)); // true
        System.out.println();
        System.out.println("------------");

        // WORD LEAN ON EXIST TILE (/* FULL WORD CHECK */)
        System.out.println(" : ");
        Word horn1 = new Word(get("HORN"), 7, 5, false);
        System.out.println(b.isLeanOnExistTile(horn1)); // FALSE
        b.placeWord(horn1); /* FIRST ADD */
        System.out.println(" : ");

        Word farm1 = new Word(get("FARM"), 5, 7, true);
        System.out.println(b.isLeanOnExistTile(farm1)); // TRUE
        Word farm2 = new Word(get("FARM"), 2, 7, true);
        System.out.println(b.isLeanOnExistTile(farm2)); // FALSE
        System.out.println(" : ");

        Word paste1 = new Word(get("PASTE"), 8, 8, false);
        System.out.println(b.isLeanOnExistTile(paste1)); // TRUE
        Word paste2 = new Word(get("PASTE"), 9, 5, false);
        System.out.println(b.isLeanOnExistTile(paste2)); // FALSE
        System.out.println(" : ");

        Word mob1 = new Word(get("MOB"), 8, 7, false);
        System.out.println(b.isLeanOnExistTile(mob1)); // TRUE
        Word mob2 = new Word(get("MOB"), 4, 9, false);
        System.out.println(b.isLeanOnExistTile(mob2)); // FALSE
        System.out.println(" : ");

        Word bit12 = new Word(get("BIT"), 6, 5, false);
        System.out.println(b.isLeanOnExistTile(bit12)); // TRUE
        Word bit22 = new Word(get("BIT"), 10, 4, false);
        System.out.println(b.isLeanOnExistTile(bit22)); // FALSE
        System.out.println(" : ");

        b.printBoard();
        System.out.println();

        // ADD WORDS TO THE BOARD - PART OF tryPlaceWord (/* NOT FULL WORD */)

        Word farm = new Word(get("FA_M"), 5, 7, true);
        b.placeWord(farm);
        Word paste = new Word(get("PASTE"), 9, 5, false);
        b.placeWord(paste);
        Word mob = new Word(get("_OB"), 8, 7, false);
        b.placeWord(mob);
        Word bit = new Word(get("BIT"), 10, 4, false);
        b.placeWord(bit);

        b.printBoard();
        System.out.println();

        Word bit2 = new Word(get("S_TA"), 9, 4, true);
        b.placeWord(bit2);

        Word bit3 = new Word(get("A_ONE"), 11, 3, false);
        b.placeWord(bit3);

        b.printBoard();
        System.out.println();

        Word fuckyou = new Word(get("_UCKYOU"), 5, 7, false);
        b.placeWord(fuckyou);

        Word sosanty = new Word(get("S_S___Y"), 6, 6, true);
        b.placeWord(sosanty);

        Word bit6 = new Word(get("_MPTYT"), 9, 9, true); // FALSE
        b.placeWord(bit6);

        b.printBoard();
        System.out.println();

        // BOARD LEGAL TEST
        b.clearWords();

        b.printBoard();
        System.out.println();

        if (b.boardLegal(horn1)) {
            System.out.println("GOOD");
            b.placeWord(horn1);
        } else
            System.out.println("BAD");

        b.printBoard();
        System.out.println();

        if (b.boardLegal(farm1)) {
            System.out.println("GOOD");
            b.placeWord(farm);
        } else
            System.out.println("BAD");

        b.printBoard();
        System.out.println();

        if (b.boardLegal(paste2)) {
            System.out.println("GOOD");
            b.placeWord(paste);
        } else
            System.out.println("BAD");

        System.out.println();
    }
}
