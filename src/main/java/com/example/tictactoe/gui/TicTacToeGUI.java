package com.example.tictactoe.gui;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;
import com.example.tictactoe.net.TicTacToeClient;

import javax.swing.*;
import java.awt.*;

public class TicTacToeGUI extends JFrame {
    private JButton[][] buttons = new JButton[3][3]; // 3x3 grid of buttons
    private JLabel statusLabel;
    private Game game;
    private TicTacToeClient client;  // Client injected via prepareClient
    private boolean isMyTurn;
    private char playerMark;

    public TicTacToeGUI(Game game) {
        this.game = game;  // The game state should come from the server
        this.isMyTurn = false;  // This will be managed by the server

        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        // Initialize the status panel
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Waiting for server to start the game...");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);

        // Initialize the game board
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        initializeBoard(boardPanel);  // We initialize the buttons, but not the board state
        add(boardPanel, BorderLayout.CENTER);

        setVisible(true);
        setButtonsEnabled(false);  // Initially, disable the buttons
    }

    public void prepareClient(TicTacToeClient client) {
        this.client = client;
    }

    public void setPlayerMark(char playerMark) {
        this.playerMark = playerMark;
    }

    private void initializeBoard(JPanel boardPanel) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new JButton("");
                buttons[row][col].setFont(new Font("Arial", Font.PLAIN, 60));
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].setEnabled(false);  // Initially disabled

                int finalRow = row;
                int finalCol = col;

                // Add action listener for button clicks
                buttons[row][col].addActionListener(e -> {
                    if (isMyTurn && buttons[finalRow][finalCol].getText().equals("")) {
                        handleMove(finalRow, finalCol);
                    }
                });

                boardPanel.add(buttons[row][col]);
            }
        }
    }

    public void sendRandomMove() {
        int[] move = game.getRandomMove();
        if (move != null) {
            handleMove(move[0], move[1]);
        }
    }

    // Handle the player's move when a button is clicked
    private void handleMove(int row, int col) {
        sendMoveToServer(row, col);
        setTurn(false);  // Disable buttons until it's the player's turn again
        checkGameStatus();
    }

    private void sendMoveToServer(int row, int col) {
        if (client != null) {
            client.sendMove(row, col);  // Send the move to the server via client
        }
        // No need to toggle turn locally. The server will control turn logic and send updates.
    }

    private void checkGameStatus() {
        if (game.checkForWin()) {
            updateStatus("Player " + game.getCurrentPlayer() + " Wins!");
            setButtonsEnabled(false);
        } else if (game.checkForLose()) {
            updateStatus("Player " + (game.getCurrentPlayer() == 'X' ? 'O': 'X') + " Wins!");
            setButtonsEnabled(false);
        } else if (game.isBoardFull()) {
            updateStatus("It's a Draw!");
            setButtonsEnabled(false);
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateGUI(GameState gameState) {
        game.loadState(gameState);
        setTurn(game.getCurrentPlayer() == playerMark);
        updateBoard(game.getBoard());
        if (game.isSuspended()) {
            setButtonsEnabled(false);
            updateStatus("Waiting for server to start the game...");
            if (isMyTurn) {
                JOptionPane.showMessageDialog(
                        this,
                        "You have been disconnected for inactivity. Please, reconnect",
                        "Disconnect",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        checkGameStatus();
    }

    private void checkIfGameSuspended() {

    }

    private void setTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        setButtonsEnabled(isMyTurn);
        updateStatus(isMyTurn ? "Your move" : "Waiting for opponent...");
    }

    private void setButtonsEnabled(boolean enabled) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (buttons[row][col].getText().equals("")) {
                    buttons[row][col].setEnabled(enabled);
                }
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        statusLabel.updateUI();
    }

    // Method to update the board based on the game state received from the server
    private void updateBoard(char[][] boardState) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                char mark = boardState[row][col];
                if (mark == '-') {
                    buttons[row][col].setText("");  // Empty if it's a blank spot
                    buttons[row][col].setEnabled(isMyTurn);  // Enable if it's the player's turn
                } else {
                    buttons[row][col].setText(String.valueOf(mark));  // X or O
                    buttons[row][col].setEnabled(false);  // Disable buttons that already have marks
                }
            }
        }

        // Ensure the panel is refreshed and changes are visible
        this.revalidate();  // Revalidates the layout
        this.repaint();     // Forces the GUI to repaint and reflect changes
    }
}
