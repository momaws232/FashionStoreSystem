package com.example.fashionstoreclothing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClothingStoreApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the login view first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fashionstoreclothing/LoginView.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Fashion Boutique - Login");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}