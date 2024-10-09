package com.example.tictactoe.model;

import com.example.tictactoe.model.GameState.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    private Game game;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        game = new Game();
        gameState = new GameState(game, false);  // Default game state for tests
    }

    @Test
    void testGetBoard() {
        char[][] board = gameState.getBoard();
        assertNotNull(board, "Board should not be null.");
        assertEquals(3, board.length, "Board should have 3 rows.");
        assertEquals(3, board[0].length, "Each row should have 3 columns.");
    }

    @Test
    void testGetCurrentPlayer() {
        assertEquals('X', gameState.getCurrentPlayer(), "Initial player should be X.");
    }

    @Test
    void testStateWaiting() {
        gameState.suspendState(true);
        assertTrue(gameState.isSuspended(), "Game should be in a suspended (waiting) state.");
    }

    @Test
    void testStateOngoing() {
        gameState.suspendState(false);
        assertEquals(State.ONGOING, gameState.getState(), "Game should be ongoing when not suspended.");
    }

    @Test
    void testStateDraw() {
        // Fill the board with alternating moves to simulate a draw (without triggering a loss)
        game.placeMark(0, 0); // X
        game.placeMark(0, 1); // X
        game.changePlayer();
        game.placeMark(0, 2); // O
        game.placeMark(1, 0); // O
        game.placeMark(1, 1); // O
        game.changePlayer();
        game.placeMark(1, 2); // X
        game.placeMark(2, 1); // X
        game.placeMark(2, 0); // X
        game.changePlayer();
        game.placeMark(2, 2); // O

        GameState drawState = new GameState(game, false);
        assertEquals(State.DRAW, drawState.getState(), "Game state should be DRAW.");
    }

    @Test
    void testStateWin() {
        // Simulate a win condition for Player X
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // X wins vertically

        GameState winState = new GameState(game, false);
        assertEquals(State.WIN, winState.getState(), "Game state should be WIN.");
    }

    @Test
    void testStateLose() {
        // Simulate Player X winning and Player O losing
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // X wins vertically

        game.changePlayer();  // Switch to Player O
        GameState loseState = new GameState(game, false);
        assertEquals(State.LOSE, loseState.getState(), "Game state should be LOSE for Player O.");
    }

    @Test
    void testCompress() {
        String compressedState = gameState.compress();
        assertNotNull(compressedState, "Compressed state should not be null.");
        assertTrue(compressedState.contains("currentPlayer=X;"), "Compressed state should contain the current player.");
        assertTrue(compressedState.contains("result=ongoing;"), "Compressed state should contain the ongoing state.");
    }

    @Test
    void testExpand() {
        String compressedState = gameState.compress();
        GameState expandedState = GameState.expand(compressedState);

        assertArrayEquals(gameState.getBoard(), expandedState.getBoard(), "Board should match after expansion.");
        assertEquals(gameState.getCurrentPlayer(), expandedState.getCurrentPlayer(), "Current player should match after expansion.");
        assertEquals(gameState.getState(), expandedState.getState(), "Game state should match after expansion.");
    }

    @Test
    void testExpandForWinState() {
        // Simulate a win condition for Player X
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // X wins vertically
        GameState winState = new GameState(game, false);
        String compressedState = winState.compress();

        GameState expandedState = GameState.expand(compressedState);
        assertEquals(State.WIN, expandedState.getState(), "Expanded game state should match the win state.");
    }

    @Test
    void testExpandForLoseState() {
        // Simulate Player O losing
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // X wins vertically
        game.changePlayer();  // Switch to Player O
        GameState loseState = new GameState(game, false);
        String compressedState = loseState.compress();

        GameState expandedState = GameState.expand(compressedState);
        assertEquals(State.LOSE, expandedState.getState(), "Expanded game state should match the lose state for Player O.");
    }

    @Test
    void testExpandForDrawState() {
        // Fill the board with alternating moves to simulate a draw (without triggering a loss)
        game.placeMark(0, 0); // X
        game.placeMark(0, 1); // X
        game.changePlayer();
        game.placeMark(0, 2); // O
        game.placeMark(1, 0); // O
        game.placeMark(1, 1); // O
        game.changePlayer();
        game.placeMark(1, 2); // X
        game.placeMark(2, 1); // X
        game.placeMark(2, 0); // X
        game.changePlayer();
        game.placeMark(2, 2); // O

        GameState drawState = new GameState(game, false);
        String compressedState = drawState.compress();
        GameState expandedState = GameState.expand(compressedState);

        assertEquals(State.DRAW, expandedState.getState(), "Expanded game state should match the draw state.");
    }
}


