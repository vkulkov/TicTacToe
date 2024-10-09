package com.example.tictactoe.net;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;
import com.example.tictactoe.service.TicTacToeHTMLTranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class TicTacToeServer {
    private ServerSocket serverSocket;
    private Socket client1Socket;
    private Socket client2Socket;
    private PrintWriter out1, out2;
    private BufferedReader in1, in2;
    private Game game;
    private TicTacToeHTMLTranslator htmlTranslator;  // For saving/loading the game state
    private volatile boolean player1Connected = false;
    private volatile boolean player2Connected = false;
    private GameState gameState;
    private CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean serverKeepAlive = true;

    public TicTacToeServer(ServerSocket serverSocket, Game game, TicTacToeHTMLTranslator htmlTranslator) {
        this.serverSocket = serverSocket;
        this.game = game;
        this.htmlTranslator = htmlTranslator;
        this.gameState = initGameState();

        new Thread(() -> {
            try {
                connectPlayers();  // Connect both players
                unblockGameState();  // Unblock game after connection
                broadcastGameState();
                new Thread(() -> {
                    handleGame();  // Handle game logic
                }).start();

                latch.countDown();

                while (serverKeepAlive) {
                    Thread.sleep(30000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Server interrupted.");
            } catch (IOException e) {
                handleError("Error setting up the server", e);
            } finally {
                shutdown();
            }
        }).start();
    }

    void waitUntilInitialized() throws InterruptedException {
        latch.await();
    }

    // Default constructor for production usage
    public TicTacToeServer(int port, Game game, TicTacToeHTMLTranslator htmlTranslator) throws IOException {
        this(new ServerSocket(port), game, htmlTranslator);  // Use the DI constructor
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            handleError("Error while server close down", e);
        }
    }

    // Initialize game state (load from HTML or create new)
    private GameState initGameState() {
        GameState gameState = htmlTranslator.loadGameStateFromHTML(game);
        if (gameState == null) {
            gameState = new GameState(game, true);  // Default initial state
            htmlTranslator.updateGameStateHTML(gameState);
        }
        return gameState;
    }

    // Handle player connections
    private void connectPlayers() throws IOException, InterruptedException {
        ackPlayer(1);  // Connect Player 1
        ackPlayer(2);  // Connect Player 2
    }

    // Acknowledge a player and wait for connection
    void ackPlayer(int playerNumber) throws IOException, InterruptedException {
        System.out.println("Waiting for Player " + playerNumber + " to connect...");
        Socket playerSocket = serverSocket.accept();
        PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));

        out.println("You are player:" + (playerNumber == 1 ? "X" : "O"));

        String response;
        while (!(response = in.readLine()).equals("Acknowledged")) {
            checkForIOErrors(response);
            Thread.sleep(1000);  // Wait for acknowledgment
        }

        if (playerNumber == 1) {
            client1Socket = playerSocket;
            out1 = out;
            in1 = in;
            player1Connected = true;
        } else {
            client2Socket = playerSocket;
            out2 = out;
            in2 = in;
            player2Connected = true;
        }

        System.out.println("Player " + playerNumber + " connected.");
    }

    // Handle the main game logic, including player moves and timeout management
    void handleGame() {
        try {
            if (isGameEnd()) {
                offerRematch();
                return;
            }
            int counter = 0;  // Counter for connection timeout
            while (serverKeepAlive && counter < 10) {  // Timeout condition after 10 seconds of inactivity
                if (processMoveIfReady(in1, out1, 1)) {
                    broadcastGameState();
                    if (isGameEnd()) {  // Check if the game has ended
                        offerRematch();
                        return;
                    }
                    counter = 0;  // Reset counter after a valid move
                } else if (processMoveIfReady(in2, out2, 2)) {
                    broadcastGameState();
                    if (isGameEnd()) {  // Check if the game has ended
                        offerRematch();
                        return;
                    }
                    counter = 0;  // Reset counter after a valid move
                } else {
                    counter++;  // Increment counter if no moves are ready
                    Thread.sleep(1000);  // Wait 1 second between checks
                }
            }

            if (isPlayer1Turn()) {
                handlePlayerDisconnection(1);
            } else {
                handlePlayerDisconnection(2);
            }

            manageGameStateAfterDisconnection();

        } catch (IOException | InterruptedException e) {
            handleError("Error during game handling", e);
        }
    }

    // Check if player move is ready and process it
    boolean processMoveIfReady(BufferedReader in, PrintWriter out, int playerNumber) throws IOException {
        if (in.ready()) {
            String move = in.readLine();
            checkForIOErrors(move);
            processMove(move, playerNumber);  // Process the move
            return true;
        }
        return false;
    }

    // Process the move made by the player
    private void processMove(String move, int playerNumber) {
        String[] parts = move.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        if (game.placeMark(row, col)) {
            game.changePlayer();  // Switch to the other player
            gameState = new GameState(game, false);  // Update game state
            htmlTranslator.updateGameStateHTML(gameState);  // Save game state
        } else {
            System.err.println("Invalid move by player " + playerNumber);
        }
    }

    // Broadcast the game state to both players
    void broadcastGameState() {
        String gameStateStr = gameState.compress();
        if (gameStateStr != null && !gameStateStr.isEmpty()) {
            try {
                out1.println("STATE:" + gameStateStr);
                out2.println("STATE:" + gameStateStr);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Game state broadcast tempo error");
            }
        } else {
            System.err.println("Cannot broadcast invalid game state.");
        }
    }

    // Check if the game has ended (either by a win or the board being full)
    boolean isGameEnd() {
        if (game.checkForWin() || game.checkForLose() || game.isBoardFull()) {
            broadcastGameState();
            htmlTranslator.updateGameStateHTML(gameState);
            return true;
        }
        return false;
    }

    private void offerRematch() throws IOException, InterruptedException {
        out1.println("Rematch?");
        out2.println("Rematch?");

        int votesForRematch = collectVotesForRematch();

        if (votesForRematch == 2) {
            resetGame();
        } else {
            serverKeepAlive = false;
        }
    }

    private int collectVotesForRematch() throws IOException, InterruptedException {
        int votesFor = 0;
        int counter = 0;

        while (counter < 10 && votesFor < 2) {
            votesFor += getVote(in1) ? 1 : 0;
            votesFor += getVote(in2) ? 1 : 0;
            counter++;
            Thread.sleep(1000);
        }

        return votesFor;
    }

    private boolean getVote(BufferedReader in) throws IOException {
        if (in.ready()) {
            String response = in.readLine();
            return response.equalsIgnoreCase("Yes");
        }
        return false;
    }

    private void resetGame() throws IOException {
        game = new Game();
        gameState = new GameState(game, false);
        broadcastGameState();
        handleGame();
    }

    private void handlePlayerDisconnection(int playerNumber) {
        if (playerNumber == 1) {
            player1Connected = false;
        } else if (playerNumber == 2) {
            player2Connected = false;
        }
    }

    private void manageGameStateAfterDisconnection() throws IOException, InterruptedException {
        blockGameState();          // Block game state
        broadcastGameState();      // Notify clients of game state
        handleDisconnection();     // Handle reconnection process

        unblockGameState();        // Unblock game state
        broadcastGameState();      // Notify clients of updated game state
        handleGame();              // Continue game handling after reconnection
    }


    // Handle disconnection and reconnection of players after timeout
    void handleDisconnection() throws IOException, InterruptedException {
        if (!player1Connected) {
            System.out.println("Attempting to reconnect Player 1...");
            ackPlayer(1);  // Reconnect Player 1
        }
        if (!player2Connected) {
            System.out.println("Attempting to reconnect Player 2...");
            ackPlayer(2);  // Reconnect Player 2
        }
    }

    // Block the game state (used during disconnection)
    private void blockGameState() {
        gameState.suspendState(true);
    }

    // Unblock the game state (after reconnection)
    private void unblockGameState() {
        gameState.suspendState(false);
    }

    // Handle IO-related errors
    private void checkForIOErrors(String message) throws IOException {
        if (message.startsWith("Error:")) {
            throw new IOException(message.substring(6));
        }
    }

    // Handle general errors
    private void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    // Method to shutdown the server gracefully
    public void shutdown() {
        System.out.println("Shutting down server...");

        // Set the running flag to false to stop any loops
        serverKeepAlive = false;

        // Close all connections and sockets
        closeConnections();

        // Close the server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        System.out.println("Server shutdown complete.");
    }

    // Close all connections
    void closeConnections() {
        try {
            if (in1 != null) in1.close();
            if (out1 != null) out1.close();
            if (client1Socket != null) client1Socket.close();

            if (in2 != null) in2.close();
            if (out2 != null) out2.close();
            if (client2Socket != null) client2Socket.close();
        } catch (IOException e) {
            handleError("Error closing connections", e);
        }
    }

    private boolean isPlayer1Turn() {
        return gameState.getCurrentPlayer() == 'X';
    }

    public boolean isPlayer1Connected() {
        return player1Connected;
    }

    public boolean isPlayer2Connected() {
        return player2Connected;
    }

    void setOut1(PrintWriter out1) {
        this.out1 = out1;
    }

    void setOut2(PrintWriter out2) {
        this.out2 = out2;
    }
}
