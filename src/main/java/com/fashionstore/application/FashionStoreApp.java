package com.fashionstore.application;

import java.util.Timer;
import java.util.TimerTask;

import com.fashionstore.storage.DataManager;
import com.fashionstore.utils.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class FashionStoreApp extends Application {

    private static DataManager dataManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the data manager and load data
        dataManager = new DataManager();
        dataManager.loadAllData();

        // Setup auto-save timer
        setupAutoSave();

        // Configure the primary stage
        primaryStage.setTitle("Fashion Store");

        // Set minimum window size
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);

        // Set initial window size
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);

        // Center the window on screen
        primaryStage.centerOnScreen();

        // Set the title bar color to black (this will only work on some platforms)
        String darkTitleBar = "-fx-background-color: #000000;";
        try {
            // Apply dark style to the window decoration
            if (primaryStage.getScene().getRoot().styleProperty().isBound()) {
                primaryStage.getScene().getRoot().styleProperty().unbind();
            }
            primaryStage.getScene().getRoot().setStyle(darkTitleBar);
        } catch (Exception e) {
            // The scene may not be available yet
        }

        SceneManager.setPrimaryStage(primaryStage);

        // Load the initial scene
        SceneManager.loadScene("LoginView.fxml");

        // Add a shutdown hook to save data on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving data before shutdown...");
            dataManager.saveAllData();
        }));
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    private void setupAutoSave() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dataManager.saveAllData();
            }
        }, 5 * 60 * 1000, 5 * 60 * 1000); // Every 5 minutes
    }
}