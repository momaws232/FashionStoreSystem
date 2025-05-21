package com.fashionstore.controllers;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.User;
import com.fashionstore.storage.DataManager;
import com.fashionstore.utils.PasswordUtil;
import com.fashionstore.utils.SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button adminLoginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Button themeToggleButton;

    private DataManager dataManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataManager = FashionStoreApp.getDataManager();
        errorLabel.setVisible(false);

        // Add enter key handler to password field
        passwordField.setOnAction(event -> handleLogin());
        
        // Set initial theme toggle button text
        Platform.runLater(() -> {
            if (loginButton.getScene() != null) {
                String currentTheme = "light";
                if (loginButton.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) loginButton.getScene().getRoot().getProperties().get("theme");
                }
                themeToggleButton.setText(currentTheme.equals("dark") ? "Light Mode" : "Dark Mode");
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Special admin login check - always allow admin/admin
        if (username.equals("admin") && password.equals("admin")) {
            System.out.println("Admin login successful with hardcoded credentials");
            SceneManager.loadScene("AdminView.fxml");
            return;
        }

        User user = dataManager.getUserByUsername(username);
        if (user == null) {
            System.out.println("Login failed: User not found: " + username);
            showError("Invalid username or password");
            return;
        }

        System.out.println("Found user: " + username);

        // Use the new verification method in User class
        if (user.verifyPassword(password)) {
            System.out.println("Login successful for user: " + username);

            // Check if user is banned
            if (user.isBanned()) {
                // Check if the ban has expired
                if (user.isBanExpired()) {
                    // Automatically remove expired ban
                    user.unbanUser();
                    dataManager.saveAllData();

                    // Allow login to proceed
                    System.out.println("User's ban has expired, allowing login");
                } else {
                    // Ban is still active
                    String banMessage = "Your account has been banned. Reason: " + user.getBanReason();

                    // Add ban duration info if it's temporary
                    int daysLeft = user.getDaysLeftOnBan();
                    if (daysLeft > 0) {
                        banMessage += "\nYour ban will expire in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s")
                                + ".";
                    } else if (daysLeft == -1) {
                        banMessage += "\nThis is a permanent ban.";
                    }

                    showError(banMessage);
                    return;
                }
            }

            // Check if account is deactivated
            if (user.isDeactivated()) {
                // If the account is deactivated but within the grace period
                if (!user.isDeactivationPeriodExpired()) {
                    int daysLeft = user.getDaysLeftUntilPermanentDeletion();
                    boolean confirm = SceneManager.showConfirmationDialog(
                            "Reactivate Account",
                            "Your account is currently deactivated.",
                            "Would you like to reactivate it? You have " + daysLeft +
                                    " day" + (daysLeft == 1 ? "" : "s") + " left before permanent deletion.");

                    if (confirm) {
                        // Reactivate the account
                        dataManager.reactivateUser(user.getUserId());
                    } else {
                        // User does not want to reactivate
                        return;
                    }
                } else {
                    // Deactivation period has expired
                    showError("This account has been permanently deleted.");
                    return;
                }
            }

            user.updateLastLogin();
            dataManager.setCurrentUser(user);
            
            // Apply user's dark mode preference before loading the scene
            System.out.println("Applying user's dark mode preference: " + user.isDarkModeEnabled());
            if (user.isDarkModeEnabled()) {
                setDarkMode();
            } else {
                setLightMode();
            }

            // If it's admin, go to admin view, otherwise go to home view
            if (username.equals("admin")) {
                SceneManager.loadScene("AdminView.fxml");
            } else {
                SceneManager.loadScene("HomeView.fxml");
            }
            return;
        }

        // If we get here, password verification failed
        System.out.println("Login failed: Invalid password for user: " + username);
        showError("Invalid username or password");
    }

    @FXML
    private void showRegistration() {
        SceneManager.loadScene("RegisterView.fxml");
    }

    @FXML
    private void showAdminLogin() {
        // Clear existing fields
        usernameField.setText("admin");
        passwordField.clear();
        errorLabel.setVisible(false);

        // Set focus to password field
        Platform.runLater(() -> passwordField.requestFocus());
    }

    @FXML
    private void handleExit() {
        boolean confirm = SceneManager.showConfirmationDialog(
                "Exit Application",
                "Are you sure you want to exit?",
                "The application will close.");

        if (confirm) {
            Platform.exit();
        }
    }

    /**
     * Toggles between dark and light mode
     */
    @FXML
    public void toggleTheme() {
        try {
            if (loginButton.getScene() != null) {
                String currentTheme = "light"; // Default theme
                if (loginButton.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) loginButton.getScene().getRoot().getProperties().get("theme");
                }
                
                if ("dark".equals(currentTheme)) {
                    // Currently dark, switch to light
                    setLightMode();
                    themeToggleButton.setText("Dark Mode");
                } else {
                    // Currently light, switch to dark
                    setDarkMode();
                    themeToggleButton.setText("Light Mode");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to toggle theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the application to dark mode
     */
    @FXML
    public void setDarkMode() {
        try {
            if (loginButton.getScene() != null) {
                // Remove light theme if present
                loginButton.getScene().getStylesheets().removeIf(
                        style -> style.contains("application.css"));

                // Add dark theme stylesheet
                String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
                if (!loginButton.getScene().getStylesheets().contains(darkThemePath)) {
                    loginButton.getScene().getStylesheets().add(darkThemePath);
                }

                // Store theme preference
                loginButton.getScene().getRoot().getProperties().put("theme", "dark");
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();
                
                // Apply dark mode style class to all elements
                applyDarkModeToAllNodes(loginButton.getScene().getRoot());
            }
        } catch (Exception e) {
            System.err.println("Failed to apply dark theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the application to light mode
     */
    @FXML
    public void setLightMode() {
        try {
            if (loginButton.getScene() != null) {
                // Remove dark theme if present
                loginButton.getScene().getStylesheets().removeIf(
                        style -> style.contains("dark-theme.css"));

                // Add light theme stylesheet
                String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
                if (!loginButton.getScene().getStylesheets().contains(lightThemePath)) {
                    loginButton.getScene().getStylesheets().add(lightThemePath);
                }

                // Store theme preference
                loginButton.getScene().getRoot().getProperties().put("theme", "light");
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();
                
                // Remove dark mode style class from all elements
                removeDarkModeFromAllNodes(loginButton.getScene().getRoot());
            }
        } catch (Exception e) {
            System.err.println("Failed to apply light theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fixes the MenuBarButtonStyle error by resetting the style of MenuBar and Menu elements
     */
    private void fixMenuBarStyling() {
        try {
            // Find the MenuBar in the scene
            javafx.scene.control.MenuBar menuBar = (javafx.scene.control.MenuBar) loginButton.getScene().lookup(".menu-bar");
            if (menuBar != null) {
                // Reset the style of the MenuBar to prevent binding errors
                menuBar.setStyle(null);
                
                // Reset the style of all Menu buttons
                for (javafx.scene.control.Menu menu : menuBar.getMenus()) {
                    menu.setStyle(null);
                }
                
                // Find and reset any MenuBarButtonStyle elements
                for (javafx.scene.Node node : menuBar.lookupAll(".MenuBarButtonStyle")) {
                    node.setStyle(null);
                }
                
                // Reset any MenuButton styles
                for (javafx.scene.Node node : menuBar.lookupAll(".menu-button")) {
                    node.setStyle(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fixing MenuBar styling: " + e.getMessage());
        }
    }

    /**
     * Recursively applies dark mode styling to all nodes in the scene graph
     */
    private void applyDarkModeToAllNodes(javafx.scene.Parent parent) {
        if (parent == null) return;
        
        if (!parent.getStyleClass().contains("dark-mode")) {
            parent.getStyleClass().add("dark-mode");
        }
        
        // Ensure text elements have proper color in dark mode
        if (parent instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) parent).setTextFill(javafx.scene.paint.Color.WHITE);
        } else if (parent instanceof javafx.scene.control.TextInputControl) {
            ((javafx.scene.control.TextInputControl) parent).setStyle("-fx-text-fill: white;");
        }
        
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (!node.getStyleClass().contains("dark-mode")) {
                node.getStyleClass().add("dark-mode");
            }
            
            // Handle text elements explicitly
            if (node instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) node).setTextFill(javafx.scene.paint.Color.WHITE);
            } else if (node instanceof javafx.scene.control.TextInputControl) {
                ((javafx.scene.control.TextInputControl) node).setStyle("-fx-text-fill: white;");
            }
            
            if (node instanceof javafx.scene.Parent) {
                applyDarkModeToAllNodes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Recursively removes dark mode styling from all nodes in the scene graph
     */
    private void removeDarkModeFromAllNodes(javafx.scene.Parent parent) {
        if (parent == null) return;
        
        parent.getStyleClass().remove("dark-mode");
        
        // Restore text color for light mode
        if (parent instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) parent).setTextFill(javafx.scene.paint.Color.BLACK);
        } else if (parent instanceof javafx.scene.control.TextInputControl) {
            ((javafx.scene.control.TextInputControl) parent).setStyle("-fx-text-fill: black;");
        }
        
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            node.getStyleClass().remove("dark-mode");
            
            // Handle text elements explicitly
            if (node instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) node).setTextFill(javafx.scene.paint.Color.BLACK);
            } else if (node instanceof javafx.scene.control.TextInputControl) {
                ((javafx.scene.control.TextInputControl) node).setStyle("-fx-text-fill: black;");
            }
            
            if (node instanceof javafx.scene.Parent) {
                removeDarkModeFromAllNodes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Opens about dialog
     */
    @FXML
    public void openAbout() {
        SceneManager.showAlert("About Fashion Store",
                "Fashion Store Application\nVersion 1.0\n" +
                        "A virtual fashion store experience allowing users to create and manage outfits.");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}