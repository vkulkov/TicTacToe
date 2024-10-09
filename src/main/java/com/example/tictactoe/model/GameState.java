package com.example.tictactoe.model;

import java.util.Locale;

// GameState class to encapsulate the parsed game state
public class GameState {
    private Game game;
    private final char[][] board;
    private final char currentPlayer;
    private State state;

    public GameState(Game game, boolean isWaiting) {
        this.game = game;
        this.board = game.getBoard();
        this.currentPlayer = game.getCurrentPlayer();
        this.state = defineState(isWaiting);
    }

    public GameState(char[][] board, char currentPlayer, State state) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.state = state;
    }

    public void setGame(Game game) {
        this.game = game;
        this.state = defineState(true);
    }

    public char[][] getBoard() {
        return board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public State getState() {
        return state;
    }

    private State defineState(boolean isSuspended) {
        if (isSuspended) {
            return State.WAITING;
        } else if (game.checkForWin()) {
            return State.WIN;
        } else if (game.checkForLose()) {
            return State.LOSE;
        } else if (game.isBoardFull()) {
            return State.DRAW;
        } else {
            return State.ONGOING;
        }
    }

    public void suspendState(boolean suspend) {
        state = defineState(suspend);
    }

    public boolean isSuspended() {
        if (state.equals(State.WAITING)) {
            return true;
        }
        return false;
    }

    public boolean isBoardFull() {
        return game.isBoardFull();
    }

    public boolean checkForWin() {
        return game.checkForWin();
    }

    public String compress() {
        StringBuilder gameState = new StringBuilder();

        // Append the board state
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                gameState.append(board[row][col]);
                if (col < 2) gameState.append(",");  // Separate columns with commas
            }
            gameState.append(";");  // Separate rows with semicolons
        }

        // Append the current player
        gameState.append("currentPlayer=").append(currentPlayer).append(";");

        // Append game result if any
        gameState.append("result=").append(state.getState()).append(";");

        return gameState.toString();
    }

    public static GameState expand(String gameStateStr) {
        String[] parts = gameStateStr.split(";");  // Split by semicolons to get rows and game metadata
        char[][] board = new char[3][3];
        char currentPlayer = '-';
        State state = State.ONGOING;

        // Parse board state
        for (int i = 0; i < 3; i++) {
            String[] row = parts[i].split(",");  // Split each row by commas
            for (int j = 0; j < 3; j++) {
                board[i][j] = row[j].charAt(0);  // Get the character for each board position
            }
        }

        // Parse the current player and result
        for (int i = 3; i < parts.length; i++) {  // Iterate over remaining parts for current player and result
            String part = parts[i];
            if (part.startsWith("currentPlayer=")) {
                currentPlayer = part.split("=")[1].charAt(0);  // Extract current player
            }
            if (part.startsWith("result=")) {
                state = State.valueOf(part.split("=")[1].toUpperCase(Locale.ROOT));  // Extract state
            }
        }

        return new GameState(board, currentPlayer, state);
    }

    public enum State {
        WAITING("waiting"),
        ONGOING("ongoing"),
        DRAW("draw"),
        WIN("win"),
        LOSE("lose");

        String state;

        State(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }
}
