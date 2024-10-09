package com.example.tictactoe.net;

import com.example.tictactoe.gui.TicTacToeGUI;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;
import com.example.tictactoe.service.TicTacToeHTMLTranslator;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicTacToeIntegrationTest {

    private TicTacToeServer server;
    private TicTacToeClient client1;
    private TicTacToeClient client2;
    private ServerSocket serverSocket;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize the server
        serverSocket = new ServerSocket(12345);
        server = new TicTacToeServer(serverSocket, new Game(), new TicTacToeHTMLTranslator());

        // Start server in its own thread
        new Thread(() -> {
            server.handleGame();  // Keep handling the game
        }).start();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Shutdown server and clients
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (client1 != null) {
            client1.shutdown();  // Assume a shutdown method exists in client
        }
        if (client2 != null) {
            client2.shutdown();  // Assume a shutdown method exists in client
        }
    }

    @Test
    void testServerClientConnectionAndMove() throws IOException {
        // Initialize client 1 and 2
        client1 = new TicTacToeClient("localhost", 12345, new TicTacToeGUI(new Game()));
        client2 = new TicTacToeClient("localhost", 12345, new TicTacToeGUI(new Game()));

        // Use Awaitility to wait for both clients to be initialized and connected
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> client1.isConnected() && client2.isConnected());

        // Simulate client 1 making a move
        client1.sendMove(0, 0);  // X makes a move

        // Wait for the server to broadcast the updated game state
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> client2.getGameState().getBoard()[0][0] == 'X');

        // Verify that client 2 received the correct game state
        GameState gameStateClient2 = client2.getGameState();
        assertEquals('X', gameStateClient2.getBoard()[0][0], "Client 2 should have received updated board from server.");

        // Simulate client 2 making a move
        client2.sendMove(1, 1);  // O makes a move

        // Wait for the server to broadcast the updated game state
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> client1.getGameState().getBoard()[1][1] == 'O');

        // Verify that client 1 received the updated game state
        GameState gameStateClient1 = client1.getGameState();
        assertEquals('O', gameStateClient1.getBoard()[1][1], "Client 1 should have received updated board from server.");
    }

    @Test
    void testServerHandlesDisconnectionAndReconnection() throws IOException {
        // Initialize client 1
        client1 = new TicTacToeClient("localhost", 12345, new TicTacToeGUI(new Game()));
        client2 = new TicTacToeClient("localhost", 12345, new TicTacToeGUI(new Game()));

        // Ensure client 1 is connected
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(client1::isConnected);

        // Simulate client 1 disconnecting
        client1.shutdown();  // Assume we have a shutdown method to simulate disconnect

        // Wait for the server to detect the disconnection
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> !server.isPlayer1Connected());

        // Simulate client 1 reconnecting
        client1 = new TicTacToeClient("localhost", 12345, new TicTacToeGUI(new Game()));

        // Wait for client 1 to reconnect
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(client1::isConnected);

        // Verify that client 1 can receive the game state after reconnection
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> client1.getGameState() != null);
    }
}

