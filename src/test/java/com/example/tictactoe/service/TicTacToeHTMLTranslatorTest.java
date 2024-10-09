package com.example.tictactoe.service;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class TicTacToeHTMLTranslatorTest {

    private TicTacToeHTMLTranslator translator;
    private Game game;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        translator = new TicTacToeHTMLTranslator();
        game = new Game();
        gameState = new GameState(game, false);
    }

    @Test
    void testUpdateGameStateHTML() throws Exception {
        translator.updateGameStateHTML(gameState);

        // Check if the HTML file was created
        File file = new File("game_state.html");
        assertTrue(file.exists(), "HTML file should be created");

        // Verify some content of the file (just a simple check)
        String content = new String(Files.readAllBytes(Paths.get("game_state.html")));
        assertTrue(content.contains("<h1>Current Game State</h1>"), "HTML content should have game state header");
        assertTrue(content.contains("Player X's Turn"), "HTML content should display the current player's turn");
    }

    @Test
    void testLoadGameStateFromHTML() throws Exception {
        // Simulate updating the HTML with some game state
        game.placeMark(0, 0);  // X at (0, 0)
        game.changePlayer();    // Change to O
        game.placeMark(1, 1);  // O at (1, 1)
        game.changePlayer();    // Change back to X
        translator.updateGameStateHTML(new GameState(game, false));

        // Load the game state from the HTML file
        Game loadedGame = new Game();
        GameState loadedGameState = translator.loadGameStateFromHTML(loadedGame);

        // Assert that the game state was loaded correctly
        char[][] loadedBoard = loadedGameState.getBoard();
        assertEquals('X', loadedBoard[0][0], "Loaded board should have X at (0,0)");
        assertEquals('O', loadedBoard[1][1], "Loaded board should have O at (1,1)");
        assertEquals('X', loadedGameState.getCurrentPlayer(), "Loaded current player should be X");
    }

    @Test
    void testParseGameStateFromHTML() throws Exception {
        // Simulate updating the HTML with some game state
        game.placeMark(0, 0);  // X at (0, 0)
        game.changePlayer();    // Change to O
        game.placeMark(1, 1);  // O at (1, 1)
        game.changePlayer();    // Change back to X
        translator.updateGameStateHTML(new GameState(game, false));

        // Load the content of the HTML file
        String htmlContent = new String(Files.readAllBytes(Paths.get("game_state.html")));
        GameState parsedState = translator.parseGameStateFromHTML(htmlContent);

        // Assert that the parsed state is correct
        char[][] parsedBoard = parsedState.getBoard();
        assertEquals('X', parsedBoard[0][0], "Parsed board should have X at (0,0)");
        assertEquals('O', parsedBoard[1][1], "Parsed board should have O at (1,1)");
        assertEquals('X', parsedState.getCurrentPlayer(), "Parsed current player should be X");
    }

    @Test
    void testUpdateGameStateHTMLAfterWin() throws Exception {
        // Simulate a win condition
        game.placeMark(0, 0);
        game.placeMark(1, 0);
        game.placeMark(2, 0);  // X wins vertically
        translator.updateGameStateHTML(new GameState(game, false));

        // Check if the HTML file was updated
        File file = new File("game_state.html");
        assertTrue(file.exists(), "HTML file should be created");

        // Verify the win condition is reflected in the file
        String content = new String(Files.readAllBytes(Paths.get("game_state.html")));
        assertTrue(content.contains("Player X Wins!"), "HTML content should display the winning message");
    }

    @Test
    void testUpdateGameStateHTMLAfterDraw() throws Exception {
        // Simulate a draw condition (fill the board)
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
        translator.updateGameStateHTML(new GameState(game, false));

        // Check if the HTML file was updated
        File file = new File("game_state.html");
        assertTrue(file.exists(), "HTML file should be created");

        // Verify the draw condition is reflected in the file
        String content = new String(Files.readAllBytes(Paths.get("game_state.html")));
        assertTrue(content.contains("It's a Draw!"), "HTML content should display the draw message");
    }
}

