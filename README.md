# Book Scrabble Game

## Table Of Contents

- [About The Game](#about-the-game)
- [About The App](#about-the-app)
- [Donwload](#download)
- [Demo Video](#demo-video)
- [Credits](#credits)

## About The Game

<p align="center">
<img src="https://i.postimg.cc/DyvK6z7P/app-screen-shots.png" alt="App" width="500px" height="253px">
</p>

Book Scrabble puts a new twist on the traditional word game by replacing the standard dictionary with a collection of books, offering players a more imaginative word pool. It's an online multiplayer game where you can play with friends by hosting or joining games over the internet, creating a social gaming experience.
Use your imagination and challange the dictionary with unique words!

<p align="center">
  <img src="https://i.ibb.co/7rSqx9m/screen-shot-1.jpg" alt="Game" width="820px" height="406px">
</p>

## About The App

The Book Scrabble game is a desktop application developed in Java, following the MVVM (Model-View-ViewModel) architecture to ensure a smooth experience for users and effective data handling. With its user-friendly JavaFX interface, the game provides an engaging and visually appealing environment for players.

### The Model

The model in the Book Scrabble app is divided into two parts: the Host Model and the Guest Model, both implementing the game model interface. The Host Model manages the game, communicates with guests through a Host Server and Guest Handler, and interacts separetly with the Game Server for word legality checks. Meanwhile, the Guest Model connects to the Host Server using a socket and communicates via the straightforward Communication Handler. This division simplifies the game, providing specific functions through two different model implementations.

<p align="center">
  <img src="https://i.postimg.cc/TPMfXMzk/Book-Scrabble-presentation.png" alt="Model" width="600px" height="424px">
</p>


### The Game Server

The game server, hosted on Oracle Cloud via an Ubuntu 22.04 VM, is responsible for checking the dictionary legality of words in the Book Scrabble app. It manages a pool of dictionaries, each linked to a specific book, and employs a Cache Manager with two caches—one for found words and another for unfound ones. The cache management uses LRU and LFU policies, alongside a space-efficient Bloom Filter, ensures rapid and precise query responses. Players can challenge a "false" response, triggering the server to verify using an IO Searcher that scans all books and updates pertinent caches, upholding the accuracy of word legality checks.

<p align="center">
  <img src="https://i.postimg.cc/G25HG3gc/3.png" alt="Model" width="600px" height="424px">
</p>


## Download

### Windows

Download the game installer for Windows:
[Book Scrabble Install]()

### Other Platforms

For other platforms, ensure you have Java Runtime Environment (JRE) installed. You can play the game by downloading the repository and running the JAR file:
[Book Scrabble Repository]()

## Demo link

-

## Credits

- Game Development: [Aviv Cohen](https://github.com/cohenaviv2)
