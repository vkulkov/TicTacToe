package com.example.tictactoe.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TicTacToeConfig {
    private Properties properties;

    public TicTacToeConfig() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getIp() {
        return properties.getProperty("ip");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }
}
