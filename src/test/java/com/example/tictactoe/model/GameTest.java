package com.example.tictactoe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();  // Initialize the game before each test
    }

    @Test
    void testInitializeBoard() {
        char[][] board = game.getBoard();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                assertEquals('-', board[row][col], "Board should be initialized with '-'");
            }
        }
    }

    @Test
    void testGetCurrentPlayer() {
        assertEquals('X', game.getCurrentPlayer(), "X should be the starting player.");
    }

    @Test
    void testChangePlayer() {
        game.changePlayer();
        assertEquals('O', game.getCurrentPlayer(), "After the first change, the current player should be O.");
        game.changePlayer();
        assertEquals('X', game.getCurrentPlayer(), "After changing again, the current player should be X.");
    }

    @Test
    void testPlaceMark() {
        assertTrue(game.placeMark(0, 0), "Mark should be placed on an empty cell.");
        assertFalse(game.placeMark(0, 0), "Mark should not be placed on an already filled cell.");
    }

    @Test
    void testIsBoardFull() {
        assertFalse(game.isBoardFull(), "Board should not be full at the start.");

        // Fill the board
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                game.placeMark(row, col);
                game.changePlayer();
            }
        }

        assertTrue(game.isBoardFull(), "Board should be full after placing marks on every cell.");
    }

    @Test
    void testCheckRowsForWin() {
        // Test horizontal win for Player X
        game.placeMark(0, 0);
        game.placeMark(0, 1);
        game.placeMark(0, 2);
        assertTrue(game.checkForWin(), "Player X should win with a horizontal line.");
    }

    @Test
    void testCheckColumnsForWin() {
        // Test vertical win for Player X
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);
        assertTrue(game.checkForWin(), "Player X should win with a vertical line.");
    }

    @Test
    void testCheckDiagonalsForWin() {
        // Test diagonal win for Player X
        game.placeMark(0, 0);
        game.placeMark(1, 1);
        game.placeMark(2, 2);
        assertTrue(game.checkForWin(), "Player X should win with a diagonal line.");
    }

    @Test
    void testCheckForLose() {
        // Test that Player O loses if Player X wins
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // Player X wins vertically

        game.changePlayer();  // Switch to Player O
        assertTrue(game.checkForLose(), "Player O should lose when Player X wins.");
    }

    @Test
    void testRandomMove() {
        int[] move = game.getRandomMove();
        assertNotNull(move, "There should be available moves at the start.");

        // Mark the whole board
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                game.placeMark(row, col);
                game.changePlayer();
            }
        }

        move = game.getRandomMove();
        assertNull(move, "There should be no moves left after the board is full.");
    }

    @Test
    void testLoadState() {
        // Create a mock GameState object
        char[][] testBoard = {
                {'X', 'O', '-'},
                {'-', 'X', 'O'},
                {'-', '-', 'X'}
        };
        GameState gameState = new GameState(testBoard, 'O', GameState.State.ONGOING);

        // Load the state
        game.loadState(gameState);

        // Validate the loaded state
        assertArrayEquals(testBoard, game.getBoard(), "Board should be correctly loaded from GameState.");
        assertEquals('O', game.getCurrentPlayer(), "Current player should be loaded from GameState.");
        assertFalse(game.isSuspended(), "Game should not be suspended after loading.");
    }
}

