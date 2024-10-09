package com.example.tictactoe.net;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;
import com.example.tictactoe.service.TicTacToeHTMLTranslator;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicTacToeServerTest {

    private TicTacToeServer server;

    @Mock
    private ServerSocket serverSocket;

    @Mock
    private Socket client1Socket, client2Socket;

    @Mock
    private PrintWriter mockOut1, mockOut2;

    private Game game;
    private TicTacToeHTMLTranslator mockTranslator;

    private ExecutorService executor;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        game = new Game();
        mockTranslator = mock(TicTacToeHTMLTranslator.class);
        serverSocket = mock(ServerSocket.class);
        client1Socket = mock(Socket.class);
        client2Socket = mock(Socket.class);

        // Mock OutputStream and InputStream for both clients
        OutputStream outputStream1 = mock(OutputStream.class);
        OutputStream outputStream2 = mock(OutputStream.class);
        InputStream inputStream1 = new ByteArrayInputStream("Acknowledged\n".getBytes());  // Simulate acknowledgment
        InputStream inputStream2 = new ByteArrayInputStream("Acknowledged\n".getBytes());

        // Mock getOutputStream() and getInputStream() for both clients
        when(client1Socket.getOutputStream()).thenReturn(outputStream1);
        when(client2Socket.getOutputStream()).thenReturn(outputStream2);
        when(client1Socket.getInputStream()).thenReturn(inputStream1);
        when(client2Socket.getInputStream()).thenReturn(inputStream2);

        // Initialize PrintWriter and BufferedReader with mocked streams
        mockOut1 = mock(PrintWriter.class);
        mockOut2 = mock(PrintWriter.class);

        // Mock the serverSocket to return the mock client sockets when accept() is called
        when(serverSocket.accept())
                .thenReturn(client1Socket)
                .thenReturn(client2Socket);

        // Initialize the server with mocked components
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                server = new TicTacToeServer(serverSocket, game, mockTranslator);
                server.waitUntilInitialized();  // Ensure server is initialized
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> server != null);

        // Proceed with test assertions after server is initialized
        assertNotNull(server);

        // Inject the mocked PrintWriters into the server if necessary
        server.setOut1(mockOut1);
        server.setOut2(mockOut2);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.shutdown(); // Ensure the server has a proper shutdown method
        }
        executor.shutdown(); // Clean up the ExecutorService
    }

    @Test
    void testPlayer1AcknowledgesConnection() {
        System.out.println("Waiting for connection");
        // Wait until player 1 is connected
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> server.isPlayer1Connected());

        System.out.println("Check for Player 1 connection");
        assertTrue(server.isPlayer1Connected(), "Player 1 should be connected.");
    }

    @Test
    void testBroadcastGameState() {
        // Simulate broadcasting the game state
        server.broadcastGameState();

        // Await until the game state is broadcasted
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(mockOut1).println(contains("STATE:")));
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(mockOut2).println(contains("STATE:")));
    }

    @Test
    void testPlayer2ReconnectsAfterTimeout() throws IOException, InterruptedException {
        // Simulate Player 2 disconnect and reconnect
        server.handleDisconnection();  // Trigger the disconnection handler

        // Wait until Player 2 reconnects
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> server.isPlayer2Connected());

        assertTrue(server.isPlayer2Connected(), "Player 2 should be reconnected.");
    }
}

