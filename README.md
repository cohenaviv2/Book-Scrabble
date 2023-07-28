# Book Scrabble Game
Immerse yourself in the world of Book Scrabble with this Java-based desktop application, where the magic of words awaits your strategic play.

## Table Of Contents
- [Overview](#overview)
- [Rules And Gameplay](#Rules-and-gameplay)
- [Credits](#credits)
- [Team Division & GANNT](#team-division--gantt)

## Video Links
- [Presentationv(including demo video)](https://www.youtube.com/watch?v=_trNdM3s-Rk)

## Overview
BookScrabble is a classic board game where players aim to score points by creating words on a grid-based game board using letter tiles. Each tile has a specific point value, and the objective is to strategically place tiles to form words and maximize the score.
However, the valid words in the game are not all the words in the English dictionary, but only the words that appear in the chosen books.

## Definitions
### Tile

- A small board containing a letter and its value in the game - the number of points the letter is worth.
- In the following diagram, you can see how much each letter is worth in the game:
 
<img src="https://github.com/Matan-Eliyahu/BookScrabble/blob/master/Readme%20images/Tiles.png" alt="Tiles" width="500px" height="300px">
- However, unlike the original game, there are no two blank tiles available in this version.

### Bag

- A bag containing 98 tiles
- Allows players to randomly draw tiles (i.e., without seeing them beforehand).
<img src="https://github.com/Matan-Eliyahu/BookScrabble/blob/master/Readme%20images/Bag.png" alt="Bag" width="225px" height="150px">

- The number of tiles in the bag for each letter at the beginning of the game:

<img src="https://github.com/Matan-Eliyahu/BookScrabble/blob/master/Readme%20images/Values.png" alt="Values" width="500px" height="60px">

### The Game Board

- 15x15 two-dimensional board
- The board features several bonus slots:
  - The central square (marked with a star) doubles the value of the word written on it.
  - Squares that double the value of the letter on them (light blue).
  - Squares that triple the value of the letter on them (blue).
  - Squares that double the value of the entire word (yellow).
  - Squares that triple the value of the entire word (red).
- The bonus slots are distributed as shown in the following diagram:

<img src="https://github.com/Matan-Eliyahu/BookScrabble/blob/master/Readme%20images/Board.png" alt="Board" width="600px" height="600px">


## Rules And Gameplay

1. Each player randomly draws a tile from the bag.
2. The order of the players is determined by the order of the letters drawn (from smallest to largest).
    - If an empty tile is drawn, it is returned to the bag, and another one is drawn.
3. All the tiles are returned to the bag.
4. Each player randomly draws 7 tiles.
5. The first player (the one who drew the smallest letter in the lottery) has to form a legal word that passes through the central slot (the star) on the board.
    - Only he gets a double score for it.
    - He replenishes his tiles from the bag to have 7 tiles again.
6. Gradually, each player, in turn, assembles a legal word from the tiles in their possession.
    - Like in a crossword puzzle, each word must rest on one of the tiles on the board.
    - After writing the word, the player adds 7 tiles from the bag.
    - Their score is accumulated based on all the words created on the board following the placement of the tiles.
        - Tiles placed on double or triple letter squares will be doubled or tripled in value accordingly.
        - The word then receives the sum of the tile value.
        - This amount will be doubled or tripled for each doubling or tripling word slot that the tiles are superimposed on (e.g., it is possible to multiply by 4 or 9 if the word took two double word or triple word slots, respectively).
        - The above calculation is true for every new word created on the board following the placement in turn.
7. A player who cannot form a valid word forfeits their turn.
8. The game will end after N rounds.

### Legal Word Criteria

A legal word must meet all the following conditions:
- Written from left to right or from top to bottom (and not in any other way).
- A word that appears in one of the books chosen for the game.
- Leans on one of the existing tiles on the board.
- Does not produce other illegal words on the board.

## Team Division & GANTT
<img src="https://github.com/cohenaviv2/Book-Scrabble/blob/main-model/src/resources/workDivision.png" alt="WorkDivision" width="1100px" height="500px">
<img src="https://github.com/Matan-Eliyahu/BookScrabble/blob/master/Readme%20images/GANTT.png" alt="GANTT" width="1100px" height="500px">

## Credits
- Game Development: [Aviv Cohen](https://github.com/cohenaviv2)

## Demo link
- https://www.youtube.com/watch?v=_trNdM3s-Rk
