package com.example.tictactoe;

import com.example.tictactoe.config.TicTacToeConfig;
import com.example.tictactoe.gui.TicTacToeGUI;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.net.TicTacToeClient;
import com.example.tictactoe.net.TicTacToeServer;
import com.example.tictactoe.service.TicTacToeHTMLTranslator;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainApp {

    public static void main(String[] args) {
        MainApp app = new MainApp();
        try {
            app.startApp();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unknown Error");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void startApp() throws IOException {
        TicTacToeConfig config = config();

        boolean isServer = JOptionPane.showConfirmDialog(null, "Host the game?") == JOptionPane.YES_OPTION;

        // Get IP address from user (only required for client)
        String ip = config.getIp();  // Default IP
        if (!isServer) {
            boolean validIP = false;
            while (!validIP) {
                ip = JOptionPane.showInputDialog(null, "Enter Server IP:", "localhost");
                if (ip == null) {
                    System.exit(0);  // Exit if user cancels the input
                }
                validIP = validateIP(ip);
                if (!validIP) {
                    JOptionPane.showMessageDialog(null, "Invalid IP Address. Please enter a valid IP or 'localhost'.");
                }
            }
        }

        // Get and validate port from user
        int port = config.getPort();  // Default port
        boolean validPort = false;
        while (!validPort) {
            String portStr = JOptionPane.showInputDialog(null, "Enter Port:", "12345");
            if (portStr == null) {
                System.exit(0);  // Exit if user cancels the input
            }
            validPort = validatePort(portStr);
            if (validPort) {
                port = Integer.parseInt(portStr);  // Safe to parse now
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Port. Please enter a valid port between 1024 and 65535.");
            }
        }

        Game game = game();
        if (isServer) {
            TicTacToeHTMLTranslator htmlTranslator = htmlTranslator();
            TicTacToeServer server = server(port, game, htmlTranslator);  // Server setup
        } else {
            TicTacToeGUI gui = gui(game);  // Initialize GUI
            TicTacToeClient client = client(ip, port, gui);
            gui.prepareClient(client);  // Inject the client into the GUI
        }
    }

    // Validate IP address (IPv4 or "localhost")
    private boolean validateIP(String ip) {
        try {
            InetAddress.getByName(ip);  // Will throw an exception if IP is invalid
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    // Validate port (must be numeric and between 1024 and 65535)
    private boolean validatePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1024 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;  // Not a valid number
        }
    }

    private TicTacToeConfig config() {
        return new TicTacToeConfig();
    }

    private TicTacToeHTMLTranslator htmlTranslator() {
        return new TicTacToeHTMLTranslator();
    }

    private Game game() {
        return new Game();
    }

    private TicTacToeGUI gui(Game game) {
        return new TicTacToeGUI(game);
    }

    private TicTacToeServer server(int port, Game game, TicTacToeHTMLTranslator htmlTranslator) throws IOException {
        return new TicTacToeServer(port, game, htmlTranslator);
    }

    private TicTacToeClient client(String ip, int port, TicTacToeGUI gui) {
        return new TicTacToeClient(ip, port, gui);
    }
}
