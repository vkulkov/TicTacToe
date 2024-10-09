package com.example.tictactoe.service;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameState;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicTacToeHTMLTranslator {

    public void updateGameStateHTML(GameState gameState) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Tic Tac Toe</title></head><body>");
        html.append("<h1>Current Game State</h1>");

        // Generate the game board as a table
        html.append("<table border='1' style='font-size:30px; text-align:center;'>");
        for (int row = 0; row < 3; row++) {
            html.append("<tr>");
            for (int col = 0; col < 3; col++) {
                char mark = gameState.getBoard()[row][col];
                if (mark == '-') {
                    html.append("<td style='width:50px; height:50px;'> </td>");
                } else {
                    html.append("<td style='width:50px; height:50px;'>" + mark + "</td>");
                }
            }
            html.append("</tr>");
        }
        html.append("</table>");

        // Display current player's turn or the game result
        if (gameState.checkForWin()) {
            html.append("<p><strong>Player " + gameState.getCurrentPlayer() + " Wins!</strong></p>");
        } else if (gameState.isBoardFull()) {
            html.append("<p><strong>It's a Draw!</strong></p>");
        } else {
            html.append("<p>Player " + gameState.getCurrentPlayer() + "'s Turn</p>");
        }

        html.append("</body></html>");

        // Write the HTML to a file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("game_state.html"))) {
            writer.write(html.toString());
        } catch (IOException e) {
            System.err.println("Error writing game state HTML: " + e.getMessage());
        }
    }

    // New method to load the game state from the HTML file
    public GameState loadGameStateFromHTML(Game game) {
        StringBuilder htmlContent = new StringBuilder();

        // Read the HTML content from the file
        try (BufferedReader reader = new BufferedReader(new FileReader("game_state.html"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading game state HTML: " + e.getMessage());
            return null;  // Return null or handle errors appropriately
        }

        // Parse the game board and current player from the HTML content
        GameState gameState = parseGameStateFromHTML(htmlContent.toString());
        game.loadState(gameState);
        gameState.setGame(game);
        return gameState;
    }

    // Helper method to parse the game state from the HTML content
    GameState parseGameStateFromHTML(String html) {
        char[][] board = new char[3][3];
        char currentPlayer = 'X';  // Default value
        GameState.State state = GameState.State.WAITING;

        // Regular expression to match the table rows and columns in the HTML
        Pattern boardPattern = Pattern.compile("<td style='width:50px; height:50px;'>(.*?)</td>");
        Matcher matcher = boardPattern.matcher(html);

        int row = 0, col = 0;
        while (matcher.find()) {
            String mark = matcher.group(1).trim();
            board[row][col] = mark.isEmpty() ? '-' : mark.charAt(0);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        // Extract the current player's turn from the HTML
        Pattern playerPattern = Pattern.compile("<p>Player (X|O)'s Turn</p>");
        Matcher playerMatcher = playerPattern.matcher(html);
        if (playerMatcher.find()) {
            currentPlayer = playerMatcher.group(1).charAt(0);
        }

        return new GameState(board, currentPlayer, state);  // Return the parsed game state
    }
}
