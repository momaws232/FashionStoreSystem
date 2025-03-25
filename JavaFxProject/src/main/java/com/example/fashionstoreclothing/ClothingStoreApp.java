package com.example.fashionstoreclothing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClothingStoreApp extends Application {
    private static Stage primaryStage;
    private static User currentUser;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        loadNewScene("LoginView.fxml", "Fashion Boutique - Login", 600, 400);
    }

    public static void loadNewScene(String fxmlFileName, String title, int width, int height) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClothingStoreApp.class.getResource("/com/example/fashionstoreclothing/" + fxmlFileName));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof UserAware) {
                ((UserAware) controller).setCurrentUser(currentUser);
            }

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                    ClothingStoreApp.class.getResource("/com/example/fashionstoreclothing/styles.css").toExternalForm());

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to load scene: " + e.getMessage());
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}