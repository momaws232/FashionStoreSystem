package com.example.fashionstoreclothing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login screen of the Fashion Boutique application.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label dateTimeLabel;
    
    private Stage stage;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Update date/time label
        updateDateTime();
        
        // Add key press event handler for Enter key
        passwordField.setOnKeyPressed(this::handleEnterKeyPress);
        
        // Clear error message when user starts typing
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setText(""));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setText(""));
    }
    
    /**
     * Sets the primary stage reference.
     * 
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Updates the date/time label with the current system time.
     */
    private void updateDateTime() {
        // For testing purposes, using a fixed date/time
        dateTimeLabel.setText("Current Date: 2025-03-24 14:27:49");
        
        // Uncomment below to use the actual system time instead
        /*
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText("Current Date: " + now.format(formatter));
        */
    }
    
    /**
     * Handles the login button click event.
     */
    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (authenticateUser(username, password)) {
            openMainApplication(username);
        } else {
            showLoginError();
        }
    }
    
    /**
     * Handles the Enter key press in the password field.
     * 
     * @param event The key event
     */
    private void handleEnterKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLoginAction();
        }
    }
    
    /**
     * Authenticates a user based on username and password.
     * 
     * @param username The username
     * @param password The password
     * @return True if authentication is successful, false otherwise
     */
    private boolean authenticateUser(String username, String password) {
        // For demo purposes, using hardcoded credentials
        // In a real application, you would connect to a database or service
        
        // Test user: momaws232 / password123
        if (username.equals("momaws232") && password.equals("password123")) {
            return true;
        }
        
        // Additional test users could be added here
        
        return false;
    }
    
    /**
     * Opens the main application after successful login.
     * 
     * @param username The authenticated username
     */
    private void openMainApplication(String username) {
        try {
            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("com/example/fashionstoreclothing/MainView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and initialize it with user data
            MainController mainController = loader.getController();
            mainController.setStage(stage);
            
            // Format the current date and time
            String currentDateTime = "2025-03-24 14:27:49"; // Fixed for testing
            
            // Set the username and date/time in the main controller
            mainController.updateDateTimeLabel(currentDateTime);
            mainController.setUserLabel(username);
            
            // Create and set the scene
            Scene scene = new Scene(root, 900, 600);
            
            // Update stage and show
            stage.setTitle("Fashion Boutique - Clothing Management");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
            System.out.println("User '" + username + "' successfully logged in at " + currentDateTime);
            
        } catch (IOException e) {
            System.err.println("Error loading main application: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error loading application. Please try again.");
        }
    }
    
    /**
     * Shows an error message for failed login.
     */
    private void showLoginError() {
        errorLabel.setText("Invalid username or password");
        passwordField.clear();
        passwordField.requestFocus();
        
        // Visual shake animation for error feedback
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
    }
}