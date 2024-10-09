# Tic Tac Toe Multiplayer Game

A Java-based Tic Tac Toe game with a graphical user interface (GUI) using Swing. The game supports multiplayer functionality over a network and allows players to reconnect during gameplay.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [How to Play](#how-to-play)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## Features
- Multiplayer Tic Tac Toe game with 2 players.
- Server-client architecture for network-based play.
- Automatic reconnection in case of a client disconnect.
- Synchronization of game state across clients.
- Graphical User Interface (GUI) built with Swing.
- Game state saved and loaded using HTML.

## Installation

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven (for dependency management and building)
- Git (for version control)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/TicTacToe.git
   cd TicTacToe
   ```
2. Build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the server:
   ```bash
   java -cp target/tictactoe-1.0-SNAPSHOT.jar com.example.tictactoe.MainApp
   ```
4. Run two clients (on a different terminal or machine):
   ```bash
   java -cp target/tictactoe-1.0-SNAPSHOT.jar com.example.tictactoe.MainApp
   ```
###Alternative: Running in IntelliJ IDEA
1. Import the project into IntelliJ IDEA.
2. Build and run the MainApp class for the server and client modes.
##How to Play
1. The server should be started first.
2. Once the server is running, clients can connect using the provided GUI.
3. Players take turns by clicking on the grid to place their marks (X or O).
4. If a player disconnects, the game pauses and waits for the player to reconnect.
5. The game can resume from where it left off, with the game state synchronized between both clients.
##Testing
This project includes unit tests and integration tests using JUnit and Mockito. To run tests:
   ```bash
   mvn test
   ```
For integration tests, the Awaitility library is used to handle asynchronous interactions between the server and clients.
##Project Structure
```bash
 TicTacToe/
│
├── src/main/java/com/example/tictactoe/
│   ├── gui/                # GUI components (Swing)
│   ├── model/              # Game models (Game, GameState)
│   ├── net/                # Server and client networking code
│   ├── service/            # Game state translation (e.g., HTML)
│   └── MainApp.java        # Main entry point
│
├── src/test/java/com/example/tictactoe/
│   └── All related unit and integration tests
│
├── pom.xml                 # Maven configuration
└── README.md               # Project documentation
   ```
##Contributing
Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (git checkout -b feature-branch).
3. Commit your changes (git commit -am 'Add some feature').
4. Push to the branch (git push origin feature-branch).
5. Create a Pull Request.
##License
This project is licensed under the MIT License. See the LICENSE file for details.
