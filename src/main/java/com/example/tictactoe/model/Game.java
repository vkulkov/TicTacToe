package com.example.tictactoe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private char[][] board;
    private Player currentPlayer;
    private boolean isSuspended;

    public Game() {
        board = new char[3][3];
        currentPlayer = Player.X; // X always starts
        initializeBoard();
    }

    public void initializeBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = '-';
            }
        }
    }

    public char getCurrentPlayer() {
        return currentPlayer.symbol;
    }

    public boolean placeMark(int row, int col) {
        if (board[row][col] == '-') {
            board[row][col] = currentPlayer.symbol;
            return true;
        }
        return false;
    }

    public void changePlayer() {
        currentPlayer = (currentPlayer.equals(Player.X)) ? Player.O : Player.X;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public boolean checkForWin() {
        return (checkRows(currentPlayer) || checkColumns(currentPlayer) || checkDiagonals(currentPlayer));
    }

    public boolean checkForLose() {
        Player opponentPlayer = Player.X;
        if (currentPlayer.equals(Player.X)) {
            opponentPlayer = Player.O;
        }
        return (checkRows(opponentPlayer) || checkColumns(opponentPlayer) || checkDiagonals(opponentPlayer));
    }

    public boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkRows(Player player) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == player.symbol
                    && board[row][1] == player.symbol
                    && board[row][2] == player.symbol) {
                return true;
            }
        }
        return false;
    }

    private boolean checkColumns(Player player) {
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == player.symbol
                    && board[1][col] == player.symbol
                    && board[2][col] == player.symbol) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonals(Player player) {
        return ((board[0][0] == player.symbol
                && board[1][1] == player.symbol
                && board[2][2] == player.symbol)
                || (board[0][2] == player.symbol
                        && board[1][1] == player.symbol
                        && board[2][0] == player.symbol));
    }

    public char[][] getBoard() {
        return board;
    }

    public int[] getRandomMove() {
        List<int[]> availableMoves = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == '-') {
                    availableMoves.add(new int[]{row, col});
                }
            }
        }
        if (availableMoves.isEmpty()) return null; // No available moves
        Random random = new Random();
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    public void loadState(GameState gameState) {
        char[][] loadedBoard = gameState.getBoard();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = loadedBoard[row][col];
            }
        }

        // Set the current player from the GameState object
        currentPlayer = Player.valueOf(String.valueOf(gameState.getCurrentPlayer()));
        isSuspended = gameState.isSuspended();
    }

    enum Player {
        X('X'),
        O('O');

        char symbol;

        Player(char symbol) {
            this.symbol = symbol;
        }
    }
}
