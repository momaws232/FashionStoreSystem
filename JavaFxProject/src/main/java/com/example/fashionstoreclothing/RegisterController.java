package com.example.fashionstoreclothing;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private UserService userService = UserService.getInstance();

    @FXML
    protected void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        boolean success = userService.registerUser(username, password, false);

        if (success) {
            messageLabel.setText("Registration successful!");

            // Redirect to login page after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            ClothingStoreApp.loadNewScene("LoginView.fxml", "Fashion Boutique - Login", 600, 400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            messageLabel.setText("Username already exists.");
        }
    }

    @FXML
    protected void handleCancel() {
        try {
            ClothingStoreApp.loadNewScene("LoginView.fxml", "Fashion Boutique - Login", 600, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}