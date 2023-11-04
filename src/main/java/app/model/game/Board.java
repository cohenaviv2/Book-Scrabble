package app.model.game;

import java.util.ArrayList;

import app.model.game.Tile.Bag;

/*
 * Represents the Game Board
 * Contains 15x15 matrix of Squares
 * Can operate on the board by placing a word
 * Placement is done after checking whether the word is legal in terms of the board and the dictionary
 * and calculates the score accordingly.
 * 
 * @author: Aviv Cohen
 *
 */

public class Board {

    private static Board boardInstance = null; // Singleton
    private Square[][] board;
    private static final int SIZE = 15;
    private boolean starBonusAct;
    private ArrayList<Word> placedWords;
    private ArrayList<Word> turnWords;
    private Tile[][] boardTiles;

    public Board() {

        /* initialize */
        this.starBonusAct = false;
        this.placedWords = new ArrayList<>(); // all the placed words on the board (Added in tryPlaceWord)
        this.turnWords = new ArrayList<>(); // all the words that was made for this turn - flush in tryPlaceWord
        this.boardTiles = new Tile[SIZE][SIZE]; // for getTiles

        board = new Square[SIZE][SIZE];

        // TW bonus - triple word
        board[0][0] = new Square(null, "TW");
        board[0][7] = new Square(null, "TW");
        board[0][14] = new Square(null, "TW");
        board[7][0] = new Square(null, "TW");
        board[7][14] = new Square(null, "TW");
        board[14][0] = new Square(null, "TW");
        board[14][7] = new Square(null, "TW");
        board[14][14] = new Square(null, "TW");

        // DW bonus - double word
        board[1][1] = new Square(null, "DW");
        board[1][13] = new Square(null, "DW");
        board[2][2] = new Square(null, "DW");
        board[2][12] = new Square(null, "DW");
        board[3][3] = new Square(null, "DW");
        board[3][11] = new Square(null, "DW");
        board[4][4] = new Square(null, "DW");
        board[4][10] = new Square(null, "DW");

        // ** STAR **
        board[7][7] = new Square(null, "DW");
        board[10][4] = new Square(null, "DW");
        board[10][10] = new Square(null, "DW");
        board[11][3] = new Square(null, "DW");
        board[11][11] = new Square(null, "DW");
        board[12][2] = new Square(null, "DW");
        board[12][12] = new Square(null, "DW");
        board[13][1] = new Square(null, "DW");
        board[13][13] = new Square(null, "DW");

        // TL bonus - triple letter
        board[1][5] = new Square(null, "TL");
        board[1][9] = new Square(null, "TL");
        board[5][1] = new Square(null, "TL");
        board[5][5] = new Square(null, "TL");
        board[5][9] = new Square(null, "TL");
        board[5][13] = new Square(null, "TL");
        board[9][1] = new Square(null, "TL");
        board[9][5] = new Square(null, "TL");
        board[9][9] = new Square(null, "TL");
        board[9][13] = new Square(null, "TL");
        board[13][5] = new Square(null, "TL");
        board[13][9] = new Square(null, "TL");
        // DL bonus - double letter
        board[0][3] = new Square(null, "DL");
        board[0][11] = new Square(null, "DL");
        board[2][6] = new Square(null, "DL");
        board[2][8] = new Square(null, "DL");
        board[3][0] = new Square(null, "DL");
        board[3][7] = new Square(null, "DL");
        board[3][14] = new Square(null, "DL");
        board[6][2] = new Square(null, "DL");
        board[6][6] = new Square(null, "DL");
        board[6][8] = new Square(null, "DL");
        board[6][12] = new Square(null, "DL");
        board[7][3] = new Square(null, "DL");
        board[7][11] = new Square(null, "DL");
        board[8][2] = new Square(null, "DL");
        board[8][6] = new Square(null, "DL");
        board[8][8] = new Square(null, "DL");
        board[8][12] = new Square(null, "DL");
        board[11][0] = new Square(null, "DL");
        board[11][7] = new Square(null, "DL");
        board[11][14] = new Square(null, "DL");
        board[12][6] = new Square(null, "DL");
        board[12][8] = new Square(null, "DL");
        board[14][3] = new Square(null, "DL");
        board[14][11] = new Square(null, "DL");

        // intialize the rest to null
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board.length; j++)
                if (board[i][j] == null)
                    board[i][j] = new Square();
    }

    public Tile[][] getTiles() {
        /*
         * returns a copy of the current(played) Board,
         * As a 2D array of Tiles[][]
         * Where there is no tile on the board it will be null.
         */

        return boardTiles;
    }

    private boolean boardLegal(Word word) {
        /*
         * returns true if the word is in the board(in place),
         * leans on one of the existing tiles,
         * and does not replace any existing tiles.
         * first word must lean on the star[7][7].
         */

        if (!isOnBoard(word))
            return false;
        else if (!(board[SIZE / 2][SIZE / 2].isTile())) // first word
            return checkFirstWord(word);
        else
            return (isLeanOnExistTile(word) && !isReplaceExistTile(word));
    }

    private boolean isOnBoard(Word word) {
        /* returns true if the word has valid index */

        // starting index check:
        if ((word.getRow() >= SIZE || word.getRow() < 0) || (word.getCol() >= SIZE || word.getCol() < 0))
            return false;

        // ending index check:
        if (word.isVertical()) {
            int endIndex = word.getRow() + (word.getTiles().length - 1);
            if (endIndex >= SIZE)
                return false;
        } else {
            int endIndex = word.getCol() + (word.getTiles().length - 1);
            if (endIndex >= SIZE)
                return false;
        }
        return true;

    }

    private boolean isLeanOnExistTile(Word word) {
        /*
         * returns true if the word leans on one of the existing tiles on the board
         * (adjacent or overlapping tile)
         */

        if (word.isVertical()) {
            for (int i = word.getRow(); i < word.getTiles().length + word.getRow(); i++) {
                if ((i > 0 && this.board[i - 1][word.getCol()].isTile()) || (i + word.getTiles().length < SIZE
                        && this.board[i + word.getTiles().length][word.getCol()].isTile()))
                    return true;
                else if ((word.getCol() > 0 && this.board[i][word.getCol() - 1].isTile())
                        || (word.getCol() < SIZE - 1 && this.board[i][word.getCol() + 1].isTile()))
                    return true;
            }
            return false;

        } else {
            for (int i = word.getCol(); i < word.getTiles().length + word.getCol(); i++) {
                if ((i > 0 && this.board[word.getRow()][i - 1].isTile()) || (i + word.getTiles().length < SIZE
                        && this.board[word.getRow()][i + word.getTiles().length].isTile()))
                    return true;
                else if ((word.getRow() > 0 && this.board[word.getRow() - 1][i].isTile())
                        || (word.getRow() < SIZE - 1 && this.board[word.getRow() + 1][i].isTile()))
                    return true;
            }
            return false;
        }
    }

    private boolean isReplaceExistTile(Word word) {
        /*
         * Returns true if one of the word tiles replaces an existing tile on the board
         */

        if (word.isVertical()) {
            for (int i = word.getRow(), index = 0; i < word.getTiles().length + word.getRow(); i++, index++)
                if (this.board[i][word.getCol()].isTile()
                        && this.board[i][word.getCol()].tile != word.getTiles()[index])
                    return true;
        } else {
            for (int i = word.getCol(), index = 0; i < word.getTiles().length + word.getCol(); i++, index++)
                if (this.board[word.getRow()][i].isTile()
                        && this.board[word.getRow()][i].tile != word.getTiles()[index])
                    return true;
        }

        return false;
    }

    private boolean checkFirstWord(Word word) {
        /*
         * returns true if the first word placement in the game leans on the star
         * tile[7][7]
         */

        if (word.isVertical()) {
            if (word.getCol() != (SIZE / 2))
                return false;
            else {
                int startIndex = word.getRow();
                int endIndex = (word.getRow() + word.getTiles().length - 1);
                return (startIndex <= (SIZE / 2) && endIndex >= (SIZE / 2));
            }
        } else {
            if (word.getRow() != (SIZE / 2))
                return false;
            else {
                int startIndex = word.getCol();
                int endIndex = (word.getCol() + word.getTiles().length - 1);
                return (startIndex <= (SIZE / 2) && endIndex >= (SIZE / 2));
            }
        }
    }

    private boolean dictionaryLegal(Word word) {
        return GameManager.get().dictionaryLegal(word);
    }

    public ArrayList<Word> getWords(Word word) {
        /*
         * generates all the new words that will be created for this word placemnent,
         * including the same word in to a dynamic array.
         */

        ArrayList<Word> words = new ArrayList<Word>();

        /*
         * word completes ANOTHER GREATER WORD check:
         * (for example - on the board "SET", the given word is "SUB" output: "SUBSET")
         */
        if (word.isVertical() && ((word.getRow() > 0 && this.board[word.getRow() - 1][word.getCol()].isTile())
                || (word.getRow() + word.getTiles().length < SIZE
                        && this.board[word.getRow() + word.getTiles().length][word.getCol()].isTile()))) {

            int start = word.getRow();
            int end = word.getRow() + word.getTiles().length - 1;

            // Find new starting index:
            while (start >= 1 && board[start - 1][word.getCol()].isTile()) {
                start--;
            }

            // Find new ending index:
            while (end <= SIZE - 2 && board[end + 1][word.getCol()].isTile()) {
                end++;
            }

            // Build the new word and add it into the array:
            int wordLength = end - start + 1;
            int startIndex = start;
            Tile[] newTiles = new Tile[wordLength];
            for (int i = 0, current = start; i < wordLength; i++, current++) {
                if (!board[current][word.getCol()].isTile()) {
                    newTiles[i] = word.getTiles()[current - start];
                } else {
                    newTiles[i] = board[current][word.getCol()].getTile();
                }
            }

            Word newWord = new Word(newTiles, startIndex, word.getCol(), true);
            words.add(newWord);

        } else if (!word.isVertical() && ((word.getCol() > 0 && this.board[word.getRow()][word.getCol() - 1].isTile())
                || (word.getCol() + word.getTiles().length < SIZE
                        && this.board[word.getRow()][word.getCol() + word.getTiles().length].isTile()))) {
            // int start, end;
            // for (start = word.getCol() - 1; this.board[word.getRow()][start].isValue() &&
            // start >= 0; --start)
            // ;
            // for (end = word.getCol() + word.getTiles().length;
            // this.board[word.getRow()][end]
            // .isValue() && end < SIZE; ++end)
            // ;
            int start = word.getCol();
            int end = word.getCol() + word.getTiles().length - 1;

            // Find new starting index:
            while (start >= 1 && board[word.getRow()][start - 1].isTile()) {
                start--;
            }

            // Find new ending index:
            while (end <= SIZE - 2 && board[word.getRow()][end + 1].isTile()) {
                end++;
            }

            int wordLength = end - start + 1;
            int startIndex = start;
            Tile[] newTiles = new Tile[wordLength];
            for (int i = 0, current = start; i < wordLength; i++, current++) {
                if (!board[word.getRow()][current].isTile()) {
                    newTiles[i] = word.getTiles()[current - start];
                } else {
                    newTiles[i] = board[word.getRow()][current].getTile();
                }
            }

            Word newWord = new Word(newTiles, word.getRow(), startIndex, false);
            words.add(newWord);
        } else {
            words.add(getFullWord(word));
        }

        /*
         * find all the word that crosses the given word
         */
        if (word.isVertical()) {
            for (int j = 0, i = word.getRow(); i < word.getTiles().length + word.getRow(); i++, j++) {
                if (word.getTiles()[j] == null && this.board[i][word.getCol()].isTile()) // Tile already exist on the
                                                                                         // board
                    continue;
                if ((word.getCol() + 1 < SIZE && this.board[i][word.getCol() + 1].isTile())
                        || (word.getCol() - 1 >= 0 && this.board[i][word.getCol() - 1].isTile())) {

                    int start = word.getCol();
                    int end = word.getCol();

                    // Find new starting index:
                    while (start >= 1 && board[i][start - 1].isTile()) {
                        start--;
                    }

                    // Find new ending index:
                    while (end <= SIZE - 2 && board[i][end + 1].isTile()) {
                        end++;
                    }

                    System.out.println("start: "+start+"\n"+"end: "+end);

                    int wordLength = end - start + 1;
                    int startIndex = start;
                    Tile[] newTiles = new Tile[wordLength];
                    for (int k = 0, current = start; k < wordLength; k++, current++) {
                        if (!board[i][current].isTile()) {
                            newTiles[k] = word.getTiles()[j];
                        } else {
                            newTiles[k] = board[i][current].getTile();
                        }
                    }

                    Word newWord = new Word(newTiles, i, startIndex, false);
                    words.add(newWord);
                }
            }
        } else {
            for (int j = 0, i = word.getCol(); i < word.getTiles().length + word.getCol(); i++, j++) {
                if (word.getTiles()[j] == null && this.board[word.getRow()][i].isTile()) // Tile already exist on the
                                                                                         // board
                    continue;
                if ((word.getRow() + 1 < SIZE && this.board[word.getRow() + 1][i].isTile())
                        || (word.getRow() - 1 >= 0 && this.board[word.getRow() - 1][i]
                                .isTile())) { /************************************************ */

                    int start = word.getRow();
                    int end = word.getRow();

                    // Find new starting index:
                    while (start >= 1 && board[start - 1][i].isTile()) {
                        start--;
                    }

                    // Find new ending index:
                    while (end <= SIZE - 2 && board[end + 1][i].isTile()) {
                        end++;
                    }

                    int wordLength = end - start + 1;
                    int startIndex = start;
                    Tile[] newTiles = new Tile[wordLength];
                    for (int k = 0, current = start; k < wordLength; k++, current++) {
                        if (!board[current][i].isTile()) {
                            newTiles[k] = word.getTiles()[j];
                        } else {
                            newTiles[k] = board[current][i].getTile();
                        }
                    }

                    Word newWord = new Word(newTiles, startIndex, i, true);
                    words.add(newWord);
                }
            }
        }

        return words;
    }

    private int getScore(Word word) {
        /*
         * returns the total score of this word placement, including the Bonus squares
         */

        int cntTW = 0, cntDW = 0;
        int score = 0;

        if (word.isVertical()) {
            int j = 0;
            for (int i = word.getRow(); i < word.getTiles().length + word.getRow(); i++, j++) {
                if (word.getTiles()[j] == null)
                    return 0;
                if (this.board[i][word.getCol()].isBonus()) { // There is a BONUS:
                    switch (this.board[i][word.getCol()].scoreModifier) { // Bonus Modifier
                        case "TW":
                            cntTW++;
                            break;
                        case "DW":
                            if (i == (SIZE / 2) && word.getCol() == (SIZE / 2) // Star Bonus already activated
                                    && starBonusAct)
                                break;
                            cntDW++;
                            break;
                        case "TL":
                            score += (word.getTiles()[j].getScore() * 3);
                            continue;
                        case "DL":
                            score += (word.getTiles()[j].getScore() * 2);
                            continue;
                    }
                }
                score += (word.getTiles()[j].getScore());
            }
            if (cntTW != 0)
                score = (score * 3 * cntTW);
            if (cntDW != 0)
                score = (score * 2 * cntDW);
        } else {
            int j = 0;
            for (int i = word.getCol(); i < word.getTiles().length + word.getCol(); i++, j++) {
                if (word.getTiles()[j] == null)
                    return 0;
                if (board[word.getRow()][i].isBonus()) { // There is a BONUS:
                    switch (board[word.getRow()][i].scoreModifier) { // Bonus Modifier
                        case "TW":
                            cntTW++;
                            break;
                        case "DW":
                            if (i == (SIZE / 2) && word.getRow() == (SIZE / 2) // Star Bonus already activated
                                    && starBonusAct)
                                break;
                            cntDW++;
                            break;
                        case "TL":
                            score += (word.getTiles()[j].getScore() * 3);
                            continue;
                        case "DL":
                            score += (word.getTiles()[j].getScore() * 2);
                            continue;
                    }
                }
                score += (word.getTiles()[j].getScore());
            }

            if (cntTW != 0)
                score = (score * 3 * cntTW);
            if (cntDW != 0)
                score = (score * 2 * cntDW);
        }
        if (!starBonusAct)
            starBonusAct = true;
        return score;
    }

    private Word getFullWord(Word word) {
        /*
         * generates the full word for potential word placement with a null where there
         * is existing tile on the board
         */

        Word newWord = null;
        Tile[] ts = new Tile[word.getTiles().length];

        for (int i = 0; i < word.getTiles().length; i++) {
            if (word.getTiles()[i] == null) {
                if (word.isVertical()) {
                    ts[i] = this.board[word.getRow() + i][word.getCol()].getTile();
                } else {
                    ts[i] = this.board[word.getRow()][word.getCol() + i].getTile();
                }
            } else {
                ts[i] = word.getTiles()[i];
            }
        }
        newWord = new Word(ts, word.getRow(), word.getCol(), word.isVertical()); // COPY CONSTRUCTOR

        return newWord;
    }

    public String wordToString(Word word) {
        Word fullword = getFullWord(word);
        //
        // System.out.println("full Word - "+fullword);
        String w = "";
        for (Tile t : fullword.getTiles()) {
            w += t.getLetter();
        }
        return w;
    }

    private void placeWord(Word word) {
        /*
         * makes a word placment on the board
         */

        if (!isOnBoard(word))
            return;

        if (word.isVertical()) {
            for (int j = 0, i = word.getRow(); i < word.getTiles().length + word.getRow(); i++, j++) {
                if (word.getTiles()[j] != null) {
                    this.board[i][word.getCol()].setTile(word.getTiles()[j]);
                    this.boardTiles[i][word.getCol()] = word.getTiles()[j];
                }
                // else {
                // this.boardTiles[i][word.getCol()] = word.getTiles()[j];

                // }
            }
        } else {
            for (int j = 0, i = word.getCol(); i < word.getTiles().length + word.getCol(); i++, j++) {
                if (word.getTiles()[j] != null) {
                    this.board[word.getRow()][i].setTile(word.getTiles()[j]);
                    this.boardTiles[word.getRow()][i] = word.getTiles()[j];
                }
                // else {
                // this.boardTiles[word.getRow()][i] = word.getTiles()[j];
                // }
            }
        }

    }

    public int tryPlaceWord(Word word) {
        /*
         * if the word is Board Legal,
         * generates all the new posible word for this placement
         * and for each one checks if its Dictionary Legal.
         * finally returns the total score of each word that was made else returns 0.
         */
        int score = 0;
        this.turnWords.clear();

        /* word placement contains null - leans on a tile */
        Word fullWord = null;
        for (Tile t : word.getTiles())
            if (t == null) {
                fullWord = new Word(getFullWord(word)); /* make the full word */
                break;
            }

        if (fullWord == null)
            fullWord = new Word(word);

        /* Check parameters */

        if (boardLegal(fullWord)) {
            ArrayList<Word> words = new ArrayList<Word>(getWords(word));
            this.turnWords.addAll(words);
            for (Word w : words) {
                if (!dictionaryLegal(w)) {
                    return 0;
                } else {
                    score += getScore(w);
                    if (score == 0)
                        return 0; /***** illegal - we pulled a null tile that doesnt exist */
                    placedWords.add(w); /* all the words that was made is dictonairyLegal - add to placedWords List */
                }
            }

            /* make a placement on the board */
            if (isBoardFull())
                return 0;
            else
                placeWord(word);
            // this.turnWords.addAll(words);
            // printBoard();

            return score;
        }
        return -1;
    }

    public ArrayList<Word> getTurnWords() {
        return this.turnWords;
    }

    public void clearTurnWords() {
        this.turnWords.clear();
    }

    public void printPlacedWords() {
        System.out.print("Words on Board: ");
        for (Word w : placedWords) {
            for (int i = 0; i < w.getTiles().length; i++) {
                System.out.print(w.getTiles()[i].letter);
            }
            System.out.print(", ");
        }
        System.out.println();
    }

    public void printBoard() {
        /* prints the current board */

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (this.board[i][j].tile == null)
                    System.out.print("- ");
                else
                    System.out.print(this.board[i][j].tile.getLetter() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printBonus() {
        /* prints the bonus indexes */

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (this.board[i][j].scoreModifier == null)
                    System.out.print("-  ");
                else
                    System.out.print(this.board[i][j].getScoreModifier() + " ");
            }
            System.out.println(" ");
        }
    }

    public void reset() {
        /* clears all the words on the board */

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this.board[i][j].tile = null;
            }
        }
    }

    public boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!this.board[i][j].isTile())
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String stringBoard = "";
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j].getTile() == null) {
                    stringBoard += '_';
                } else {
                    stringBoard += board[i][j].getTile().getLetter();
                }
            }
            stringBoard += ":";
        }
        return stringBoard;
    }

    public static Board getBoard() {
        if (boardInstance == null)
            boardInstance = new Board();
        return boardInstance;
    }

    /* Square */
    /*
     * Represents a square on the board,
     * each Square stores information about the letter tile,if placed,
     * and the score modifier of the bonus.
     * 
     * @author: Aviv Cohen
     * 
     */

    public class Square {

        private Tile tile;
        private final String scoreModifier; // TW,DW,TL,DL (Triple/Double Word/Letter bonus)

        public Square() {
            /* initialize to null sqaures that do not contain a bonus */
            this.tile = null;
            this.scoreModifier = null;
        }

        public Square(Tile letterTile, String scoreModifier) {
            /* .... */
            this.tile = letterTile;
            if (scoreModifier == "TW" || scoreModifier == "DW" || scoreModifier == "TL" || scoreModifier == "DL")
                this.scoreModifier = scoreModifier;
            else
                this.scoreModifier = null;
        }

        public Tile getTile() {
            return tile;
        }

        public void setTile(Tile letterTile) {
            this.tile = letterTile;
        }

        public void removeLetterTile(Tile letterTile) {
            this.tile = null;
        }

        public String getScoreModifier() {
            return scoreModifier;
        }

        public boolean isTile() {
            if (this.tile != null)
                return true;
            else
                return false;
        }

        public boolean isBonus() {
            if (this.scoreModifier != null)
                return true;
            else
                return false;
        }

    }
}
