# Book Scrabble Game

### Table Of Contents

- [About The Game](#about-the-game)
- [Development](#development)
- [Donwload](#download)
- [Demo Video](#demo-video)
- [Credits](#credits)

# About The Game

<p align="center">
<img src="https://i.postimg.cc/fyTYh7BB/1234-1.png" alt="App" width="400px" height="189px">
</p>

Book Scrabble puts a new twist on the traditional word game by replacing the standard dictionary with a collection of books, offering players a more imaginative word pool. <br> It's an online multiplayer game where you can play with friends by hosting or joining games over the internet, creating a social gaming experience.
<br>Use your imagination and challenge the game dictionary with unique words!

<p align="center">
  <img src="https://i.postimg.cc/yN46R14H/8-1.png" alt="Game" width="700px" height="362px">
</p>

# Development

The Book Scrabble game is a desktop application developed in Java, following the MVVM (Model-View-ViewModel) architecture to ensure a smooth experience for users and effective data handling. With its user-friendly JavaFX interface, the game provides an engaging and visually appealing environment for players.

### The Model

The model in the Book Scrabble app is divided into two parts: the Host Model and the Guest Model, both implementing the game model interface. <br>The Host Model manages the game, communicates with guests through a Host Server and Guest Handler, and interacts separately with the Game Server for word legality checks.<br> The communication between the host and the guest is facilitated through a custom-defined protocol, with objects being transferred using Java's Serializable interface. <br> Meanwhile, the Guest Model connects to the Host Server using a socket and communicates via the straightforward Communication Handler. This division simplifies the game, providing specific functions through two different model implementations.

<p align="center">
  <img src="https://i.postimg.cc/6Qq8cZdn/Book-Scrabble-presentation.jpg" alt="Model" width="600px" height="424px">
</p>


### The Game Server

The game server, hosted on Oracle Cloud via an Ubuntu 22.04 VM, is responsible for checking the dictionary legality of words in the Book Scrabble app. <br> It manages a pool of dictionaries, each linked to a specific book, and employs a Cache Manager with two caches—one for found words and another for unfound ones. <br> The cache management uses LRU and LFU policies, alongside a space-efficient Bloom Filter to ensures rapid  query responses. <br> Players can challenge a "false" response, triggering the server to verify using an IO Searcher that scans all books and updates relevant caches, upholding the accuracy of word legality checks.

<p align="center">
  <img src="https://i.postimg.cc/G25HG3gc/3.png" alt="Model" width="600px" height="424px">
</p>


# Download

### Windows

Download the game installer for Windows: <br>


### Other Platforms

For other platforms, ensure you have Java Runtime Environment (JRE) installed. <br> You can play the game by downloading the repository and running the JAR file: <br>
https://github.com/cohenaviv2/Book-Scrabble/archive/main.zip

# Demo Video

- [Video Link]()

# Credits

- Game Development: [Aviv Cohen](https://github.com/cohenaviv2) <br> <br>
Find any bugs? Enjoyed the game? Feel free to reach out to me at cohenaviv2@gmail.com for feedback or to report any issues. Your input is valuable!