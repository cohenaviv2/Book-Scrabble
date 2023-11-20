# Book Scrabble Game

### Table Of Contents

- [About The Game](#about-the-game)
- [Development](#development)
- [Donwload](#download)
- [Demo Video](#demo-video)
- [Credits](#credits)

# About The Game

### Book Scrabble - Unleashing Fresh Word Challenges

Book Scrabble puts a new twist on the traditional word game by replacing the standard dictionary with a collection of books, offering players a more imaginative word pool. It's an online multiplayer game where you can play with friends by hosting or joining games, creating a social gaming experience.
<br>Use your imagination and challenge the game dictionary with unique words! <br> <br>

<p align="center">
  <img src="https://i.postimg.cc/8zjRS2M6/1.png" alt="Game" width="800px" height="458px">
</p>

### Connect and Play with Friends Anywhere
Gather around and challenge your buddies to a wordy showdown. Connect with friends from anywhere in the world using IP addresses over the internet. Whether you're near or far, the joy of wordplay knows no bounds. Let the game begin!

<p align="center">
<img src="https://i.postimg.cc/fyTYh7BB/1234-1.png" alt="App" width="500px" height="236px">
</p>



# Development

The Book Scrabble game is a desktop application developed in Java, following the MVVM (Model-View-ViewModel) architecture to ensure a smooth experience for users and effective data handling. With its user-friendly JavaFX interface, the game provides an engaging and visually appealing environment for players.
<br> <br>

<p align="center">
Architecture Overview
<br> <br>
<img src="https://i.postimg.cc/MTjHDZ0D/1.png" width="700px" height="289">
</p>

### The Model

The model in the Book Scrabble app is divided into two parts: the Host Model and the Guest Model, both implementing the game model interface. <br>The Host Model manages the game, communicates with guests through a Host Server and Guest Handler, and interacts separately with the Game Server for word legality checks.<br> The communication between the host and the guest is facilitated through a custom-defined protocol, with objects being transferred using Java's Serializable interface. <br> Meanwhile, the Guest Model connects to the Host Server using a socket and communicates via the straightforward Communication Handler. This division simplifies the game, providing specific functions through two different model implementations.

<p align="center">
  <img src="https://i.postimg.cc/JnTKgZg8/Book-Scrabble-presentation.jpg" alt="Model" width="600px" height="505px">
</p>


### The Game Server

The game server, hosted on Oracle Cloud via an Ubuntu 22.04 VM, is responsible for checking the dictionary legality of words in the Book Scrabble app. <br> It manages a pool of dictionaries, each linked to a specific book, and employs a Cache Manager with two caches—one for found words and another for unfound ones. <br> The cache management uses LRU and LFU policies, alongside a space-efficient Bloom Filter to ensures rapid  query responses. <br> Players can challenge a "false" response, triggering the server to verify using an IO Searcher that scans all books and updates relevant caches, upholding the accuracy of word legality checks.

<p align="center">
  <img src="https://i.postimg.cc/529Y8VXH/Book-Scrabble-presentation.jpg" alt="GameServer" width="600px" height="470px">
</p>


# Download

### Windows

Download the game installer for Windows: <br>
https://github.com/cohenaviv2/Book-Scrabble/releases/download/v1.0/Book_Scrabble_Setup.exe

### Other Platforms

For other platforms, ensure you have Java Runtime Environment (JRE) installed. <br> You can play the game by downloading the repository and running the JAR file: <br>
https://github.com/cohenaviv2/Book-Scrabble/archive/main.zip

# Demo Video

- [Video Link]()

# Credits

- Game Development: [Aviv Cohen](https://github.com/cohenaviv2) <br> <br>
Find any bugs? Enjoyed the game? Feel free to reach out to me at cohenaviv2@gmail.com for feedback or to report any issues. Your input is valuable!
