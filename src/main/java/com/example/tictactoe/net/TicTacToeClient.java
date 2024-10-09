package com.example.tictactoe.net;

import com.example.tictactoe.model.GameState;
import com.example.tictactoe.gui.TicTacToeGUI;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TicTacToeClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final TicTacToeGUI gui;
    private boolean connected;
    private GameState gameState;

    public TicTacToeClient(String ip, int port, TicTacToeGUI gui) {
        this.gui = gui;
        try {
            initializeConnection(ip, port);  // Initialize the connection to the server
            listenForGameState();  // Start listening for game state updates
        } catch (IOException e) {
            gui.showErrorMessage("Failed to connect to server. Please try again.");
            connected = false;
        }
    }

    // Initialize connection to the server
    private void initializeConnection(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
    }

    // Listen for game state updates from the server
    void listenForGameState() {
        new Thread(() -> {
            try {
                String response;
                while (connected && (response = in.readLine()) != null) {
                    processServerResponse(response);  // Process each response from the server
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println(e.getMessage());
                    gui.showErrorMessage("Connection lost.");
                }
            }
        }).start();
    }

    // Process each server response
    private void processServerResponse(String response) throws IOException {
        if (response.startsWith("You")) {
            handlePlayerAssignment(response);  // Assign the player mark (X or O)
        } else if (isRematchRequest(response)) {
            handleRematchRequest();  //Decide if rematch
        } else if (isGameStateUpdate(response)) {
            handleGameStateUpdate(extractGameState(response));  // Handle game state update
        }
    }

    // Handle player assignment from the server response
    private void handlePlayerAssignment(String response) {
        char playerMark = response.split(":")[1].charAt(0);
        gui.setPlayerMark(playerMark);
        out.println("Acknowledged");
    }

    private boolean isRematchRequest(String response) {
        return response.equals("Rematch?");
    }

    private void handleRematchRequest() throws IOException {
        boolean rematch = JOptionPane.showConfirmDialog(gui, "Rematch?") == JOptionPane.YES_OPTION;
        if (rematch) {
            out.println("Yes");
        }
    }

    private boolean isGameStateUpdate(String response) {
        return response.startsWith("STATE:");
    }

    private String extractGameState(String response) {
        return response.substring(6);  // Extract the game state after "STATE:"
    }

    // Handle game state update and make a random move
    private void handleGameStateUpdate(String boardState) {
        gameState = GameState.expand(boardState);
        gui.updateGUI(gameState);  // Update GUI with the new game state
        if (gameState.getState().equals(GameState.State.ONGOING)) {
            sendRandomMove();  // Send a random move after a delay
        }
    }

    // Method to send a random move to the server
    private void sendRandomMove() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);  // Delay to simulate player thinking time
                gui.sendRandomMove();  // Make a random move
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Method to send a specific move to the server
    public void sendMove(int row, int col) {
        out.println(row + "," + col);  // Send the move in "row,col" format
    }

    // Shutdown method to close client socket and cleanup resources
    void shutdown() {
        try {
            connected = false;  // Stop listening for new messages
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }

    // Check if client is connected
    boolean isConnected() {
        return connected;
    }

    // Getter for the latest game state
    GameState getGameState() {
        return gameState;
    }

    void setOut(PrintWriter out) {
        this.out = out;
    }

    void setIn(BufferedReader in) {
        this.in = in;
    }
}
