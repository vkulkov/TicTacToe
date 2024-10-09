package com.example.tictactoe.net;

import com.example.tictactoe.gui.TicTacToeGUI;
import com.example.tictactoe.model.GameState;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicTacToeClientTest {

    private TicTacToeClient client;
    private TicTacToeGUI mockGUI;
    private Socket mockSocket;
    private PrintWriter mockOut;
    private BufferedReader mockIn;

    @BeforeEach
    void setUp() throws IOException {
        // Mocking GUI
        mockGUI = mock(TicTacToeGUI.class);
        // Mocking the socket
        mockSocket = mock(Socket.class);

        // Creating piped streams for input and output simulation
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);

        mockOut = mock(PrintWriter.class);
        mockIn = mock(BufferedReader.class);

        // Mock socket behavior for input and output streams
        when(mockSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(mockSocket.getInputStream()).thenReturn(mock(InputStream.class));

        // Now, initialize the client with the mocked socket and GUI
        client = new TicTacToeClient("localhost", 12345, mockGUI);
        client.setOut(mockOut);
        client.setIn(mockIn);
    }

    @Test
    void testSendMove() {
        // Setup
        client.sendMove(1, 2);

        // Verify that the move was sent in the correct format
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockOut).println(captor.capture());
        assertEquals("1,2", captor.getValue(), "The move should be sent in 'row,col' format.");
    }

    @Test
    void testPlayerAssignment() throws IOException {
        // Mock the input to simulate server messages
        when(mockIn.readLine()).thenReturn("You are Player:X", "Acknowledged", null);

        // Start the listening thread manually (this is the key part)
        new Thread(() -> client.listenForGameState()).start();

        // Send a move to invoke the listening loop
        client.sendMove(1, 1);

        // Use Awaitility to wait for the player's mark to be set on the GUI
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                verify(mockGUI).setPlayerMark('X')
        );

        verify(mockOut).println("Acknowledged");
    }

    @Test
    void testGameStateUpdate() throws IOException {
        // Prepare sample game state string
        String sampleGameState = "X,-,-;O,-,-;X,O,-;currentPlayer=X;result=ongoing;";

        // Simulate receiving game state from the server
        when(mockIn.readLine()).thenReturn("STATE:" + sampleGameState, null);

        // Start the listening thread to simulate server response handling
        new Thread(() -> client.listenForGameState()).start();

        // Trigger the sendMove method to initiate the listening loop
        client.sendMove(1, 1);

        // Use Awaitility to wait until the GameState is passed to the GUI
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                verify(mockGUI).updateGUI(any(GameState.class))
        );

        // Capture and verify the GameState passed to the GUI
        ArgumentCaptor<GameState> captor = ArgumentCaptor.forClass(GameState.class);
        verify(mockGUI).updateGUI(captor.capture());

        GameState capturedGameState = captor.getValue();

        // Assert that the current player is 'X' and the GameState matches the expected sample
        assertEquals('X', capturedGameState.getCurrentPlayer());
        assertEquals(sampleGameState, capturedGameState.compress());
    }

    @Test
    void testServerDisconnection() throws IOException {
        // Simulate server sending a null value to indicate disconnection
        when(mockIn.readLine()).thenReturn(null);

        // Start the listening thread to simulate the server disconnection
        new Thread(() -> client.listenForGameState()).start();

        // Trigger the sendMove method to start the listening loop
        client.sendMove(1, 1);

        // Use Awaitility to wait until the GUI shows an error message
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mockGUI).showErrorMessage(captor.capture());

            // Assert the captured message is the expected one
            assertEquals("Failed to connect to server. Please try again.", captor.getValue());
        });
    }
}

