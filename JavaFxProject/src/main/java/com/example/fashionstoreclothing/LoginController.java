package com.example.fashionstoreclothing;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private UserService userService = UserService.getInstance();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            return;
        }

        Optional<User> user = userService.authenticate(username, password);

        if (user.isPresent()) {
            try {
                ClothingStoreApp.setCurrentUser(user.get());
                if (user.get().isAdmin()) {
                    ClothingStoreApp.loadNewScene("AdminView.fxml", "Fashion Boutique - Admin", 800, 600);
                } else {
                    ClothingStoreApp.loadNewScene("InventoryView.fxml", "Fashion Boutique - Inventory", 800, 600);
                }
            } catch (IOException e) {
                messageLabel.setText("Error loading main view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid username or password");
        }
    }

    @FXML
    protected void handleRegister() {
        try {
            ClothingStoreApp.loadNewScene("RegisterView.fxml", "Fashion Boutique - Register", 600, 400);
        } catch (IOException e) {
            messageLabel.setText("Error loading register view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}