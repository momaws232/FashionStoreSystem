package com.fashionstore.controllers;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.User;
import com.fashionstore.storage.DataManager;
import com.fashionstore.utils.PasswordUtil;
import com.fashionstore.utils.SceneManager;
import com.fashionstore.controllers.HomeController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

public class UserProfileController implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Label createdDateLabel;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private CheckBox darkModeCheckbox;
    @FXML
    private Button deactivateAccountButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label preferencesStatusLabel;

    private DataManager dataManager;
    private User currentUser;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataManager = FashionStoreApp.getDataManager();
        currentUser = dataManager.getCurrentUser();

        if (currentUser == null) {
            SceneManager.loadScene("LoginView.fxml");
            return;
        }

        // Populate user information
        usernameLabel.setText(currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());

        if (currentUser.getDateRegistered() != null) {
            createdDateLabel.setText(dateFormat.format(currentUser.getDateRegistered()));
        } else {
            createdDateLabel.setText("N/A");
        }

        // Set dark mode checkbox based on user preference
        darkModeCheckbox.setSelected(currentUser.isDarkModeEnabled());

        // Disable deactivate button if it's an admin
        if (currentUser.getUsername().equals("admin")) {
            deactivateAccountButton.setDisable(true);
        }

        // Apply the appropriate theme and ensure form fields have proper style
        applyTheme();

        // Ensure text input fields have proper styling in dark mode
        if (currentUser.isDarkModeEnabled()) {
            // Explicitly style the text fields for dark mode
            styleForDarkMode();

            // Apply dark mode to root parent and entire scene
            if (firstNameField.getScene() != null) {
                applyDarkModeToAllNodes((javafx.scene.Parent) firstNameField.getScene().getRoot());
            }
        }
    }

    @FXML
    private void saveProfileChanges() {
        boolean hasChanges = false;
        boolean hasPasswordChange = false;

        // Check if first name or last name changed
        if (!firstNameField.getText().equals(currentUser.getFirstName())) {
            currentUser.setFirstName(firstNameField.getText());
            hasChanges = true;
        }

        if (!lastNameField.getText().equals(currentUser.getLastName())) {
            currentUser.setLastName(lastNameField.getText());
            hasChanges = true;
        }

        // Check if password fields are filled
        if (!currentPasswordField.getText().isEmpty() && !newPasswordField.getText().isEmpty()) {
            // Verify current password
            if (currentUser.verifyPassword(currentPasswordField.getText())) {
                try {
                    // Hash the new password and set it
                    String hashedPassword = PasswordUtil.hashPassword(newPasswordField.getText());
                    currentUser.setPasswordHash(hashedPassword);
                    hasChanges = true;
                    hasPasswordChange = true;

                    // Clear password fields
                    currentPasswordField.clear();
                    newPasswordField.clear();
                } catch (Exception e) {
                    statusLabel.setText("Error: Failed to update password");
                    return;
                }
            } else {
                statusLabel.setText("Error: Current password is incorrect");
                return;
            }
        } else if (!currentPasswordField.getText().isEmpty() || !newPasswordField.getText().isEmpty()) {
            // One password field is filled but not the other
            statusLabel.setText("Error: Please fill both password fields to change password");
            return;
        }

        if (hasChanges) {
            // Save changes
            dataManager.saveAllData();

            if (hasPasswordChange) {
                statusLabel.setText("Profile and password updated successfully");
            } else {
                statusLabel.setText("Profile updated successfully");
            }
        } else {
            statusLabel.setText("No changes made");
        }
    }

    @FXML
    private void savePreferences() {
        // Save the dark mode preference
        dataManager.saveUserDarkModePreference(currentUser.getUserId(), darkModeCheckbox.isSelected());
        preferencesStatusLabel.setText("Preferences saved successfully");
    }

    @FXML
    private void toggleDarkMode() {
        boolean isDarkMode = darkModeCheckbox.isSelected();
        System.out.println("Toggling dark mode: " + (isDarkMode ? "ON" : "OFF"));

        // Apply theme based on checkbox
        if (isDarkMode) {
            applyDarkMode();
        } else {
            applyLightMode();
        }

        // Save preference
        dataManager.saveUserDarkModePreference(currentUser.getUserId(), isDarkMode);

        // Update status message
        preferencesStatusLabel.setText("Theme changed to " + (isDarkMode ? "dark" : "light") + " mode");
    }

    /**
     * Applies dark mode to current scene and all windows
     */
    private void applyDarkMode() {
        try {
            // Apply to current scene
            if (darkModeCheckbox.getScene() != null) {
                Scene scene = darkModeCheckbox.getScene();

                // Remove any existing MenuBar style issues
                cleanupMenuBarStyles(scene);

                // Remove light theme if present
                scene.getStylesheets().clear();

                // Add dark theme stylesheet
                String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
                if (!scene.getStylesheets().contains(darkThemePath)) {
                    scene.getStylesheets().add(darkThemePath);
                }

                // Store theme preference and add dark-mode class to root
                if (!scene.getRoot().getStyleClass().contains("dark-mode")) {
                    scene.getRoot().getStyleClass().add("dark-mode");
                }
                scene.getRoot().getProperties().put("theme", "dark");
            }

            // Apply to all other windows
            applyThemeGlobally();
        } catch (Exception e) {
            System.err.println("Error applying dark mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applies light mode to current scene and all windows
     */
    private void applyLightMode() {
        try {
            // Apply to current scene
            if (darkModeCheckbox.getScene() != null) {
                Scene scene = darkModeCheckbox.getScene();

                // Remove any existing MenuBar style issues
                cleanupMenuBarStyles(scene);

                // Remove dark theme if present
                scene.getStylesheets().clear();

                // Add light theme stylesheet
                String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
                if (!scene.getStylesheets().contains(lightThemePath)) {
                    scene.getStylesheets().add(lightThemePath);
                }

                // Remove dark-mode class and store theme preference
                scene.getRoot().getStyleClass().remove("dark-mode");
                scene.getRoot().getProperties().put("theme", "light");
            }

            // Apply to all other windows
            applyThemeGlobally();
        } catch (Exception e) {
            System.err.println("Error applying light mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to cleanup MenuBar styling issues
     */
    private void cleanupMenuBarStyles(Scene scene) {
        try {
            // Find all MenuBar elements in the scene
            javafx.scene.control.MenuBar menuBar = (javafx.scene.control.MenuBar) scene.lookup(".menu-bar");
            if (menuBar != null) {
                // Reset any bound styles that might cause errors
                menuBar.setStyle(null);

                // Reset styles on all menus
                for (javafx.scene.control.Menu menu : menuBar.getMenus()) {
                    menu.setStyle(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning MenuBar styles: " + e.getMessage());
        }
    }

    /**
     * Applies theme globally to all windows
     */
    private void applyThemeGlobally() {
        boolean isDarkMode = darkModeCheckbox.isSelected();

        try {
            System.out.println("Applying " + (isDarkMode ? "dark" : "light") + " mode globally to all windows");

            // Apply to all open stages
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;
                    Scene scene = stage.getScene();

                    if (scene != null) {
                        // Clear existing stylesheets
                        scene.getStylesheets().clear();

                        // Add appropriate stylesheet
                        if (isDarkMode) {
                            String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
                            scene.getStylesheets().add(darkThemePath);
                            scene.getRoot().getProperties().put("theme", "dark");

                            // Add dark-mode class to root
                            if (!scene.getRoot().getStyleClass().contains("dark-mode")) {
                                scene.getRoot().getStyleClass().add("dark-mode");
                            }
                        } else {
                            String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
                            scene.getStylesheets().add(lightThemePath);
                            scene.getRoot().getProperties().put("theme", "light");

                            // Remove dark-mode class from root
                            scene.getRoot().getStyleClass().remove("dark-mode");
                        }

                        System.out
                                .println("Applied " + (isDarkMode ? "dark" : "light") + " theme to window: " + window);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying global theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recursively applies dark mode styling to all nodes in the scene graph
     */
    private void applyDarkModeToAllNodes(javafx.scene.Parent parent) {
        if (parent == null)
            return;

        if (!parent.getStyleClass().contains("dark-mode")) {
            parent.getStyleClass().add("dark-mode");
        }

        // Ensure elements have proper colors in dark mode
        if (parent instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) parent).setTextFill(javafx.scene.paint.Color.WHITE);
        } else if (parent instanceof javafx.scene.control.TextInputControl) {
            ((javafx.scene.control.TextInputControl) parent).setStyle(
                    "-fx-text-fill: white; -fx-background-color: #2a2a2a; -fx-control-inner-background: #2a2a2a;");
        } else if (parent instanceof javafx.scene.control.TabPane) {
            // Set TabPane background
            parent.setStyle("-fx-background-color: #1e1e1e;");
        } else if (parent instanceof javafx.scene.layout.AnchorPane) {
            // Set AnchorPane background
            parent.setStyle("-fx-background-color: transparent;");
        } else if (parent instanceof javafx.scene.layout.BorderPane) {
            // Set BorderPane background
            parent.setStyle("-fx-background-color: #1e1e1e;");
        } else if (parent instanceof javafx.scene.layout.VBox || parent instanceof javafx.scene.layout.HBox) {
            // Only apply to VBox/HBox that don't explicitly have other styling
            if (!parent.getStyleClass().contains("card")) {
                parent.setStyle("-fx-background-color: transparent;");
            }
        }

        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (!node.getStyleClass().contains("dark-mode")) {
                node.getStyleClass().add("dark-mode");
            }

            // Handle specific element types
            if (node instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) node).setTextFill(javafx.scene.paint.Color.WHITE);
            } else if (node instanceof javafx.scene.control.TextInputControl) {
                ((javafx.scene.control.TextInputControl) node).setStyle(
                        "-fx-text-fill: white; -fx-background-color: #2a2a2a; -fx-control-inner-background: #2a2a2a;");
            } else if (node instanceof javafx.scene.control.TabPane) {
                node.setStyle("-fx-background-color: #1e1e1e;");
            } else if (node instanceof javafx.scene.layout.AnchorPane) {
                node.setStyle("-fx-background-color: transparent;");
            } else if (node instanceof javafx.scene.layout.Region) {
                // Only apply to regions without specific styling
                if (!node.getStyleClass().contains("card")) {
                    node.setStyle("-fx-background-color: transparent;");
                }
            }

            if (node instanceof javafx.scene.Parent) {
                applyDarkModeToAllNodes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Helper method to recursively find and style all AnchorPane nodes
     */
    private void applyStyleToAllAnchorPanes(javafx.scene.Parent parent) {
        if (parent == null)
            return;

        if (parent instanceof javafx.scene.layout.AnchorPane) {
            parent.setStyle("-fx-background-color: transparent;");
        }

        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.layout.AnchorPane) {
                node.setStyle("-fx-background-color: transparent;");
            }

            if (node instanceof javafx.scene.Parent) {
                applyStyleToAllAnchorPanes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Recursively removes dark mode styling from all nodes in the scene graph
     */
    private void removeDarkModeFromAllNodes(javafx.scene.Parent parent) {
        if (parent == null)
            return;

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
     * Applies the theme based on user preference
     */
    private void applyTheme() {
        if (darkModeCheckbox.getScene() == null)
            return;

        // Apply theme based on checkbox
        if (darkModeCheckbox.isSelected()) {
            // Apply dark theme
            darkModeCheckbox.getScene().getStylesheets().clear();
            String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
            darkModeCheckbox.getScene().getStylesheets().add(darkThemePath);
            darkModeCheckbox.getScene().getRoot().getProperties().put("theme", "dark");

            // Add dark-mode class to scene root
            darkModeCheckbox.getScene().getRoot().getStyleClass().add("dark-mode");

            // Apply dark mode to all elements
            applyDarkModeToAllNodes(darkModeCheckbox.getScene().getRoot());

            // Explicitly handle TabPane styling
            javafx.scene.control.TabPane tabPane = (javafx.scene.control.TabPane) darkModeCheckbox.getScene()
                    .lookup(".tab-pane");
            if (tabPane != null) {
                tabPane.setStyle("-fx-background-color: #1e1e1e;");

                // Style the tab content areas
                for (javafx.scene.control.Tab tab : tabPane.getTabs()) {
                    if (tab.getContent() instanceof javafx.scene.Parent) {
                        javafx.scene.Parent content = (javafx.scene.Parent) tab.getContent();
                        content.setStyle("-fx-background-color: #1e1e1e;");
                        applyStyleToAllAnchorPanes(content);
                    }
                }
            }
        } else {
            // Apply light theme
            darkModeCheckbox.getScene().getStylesheets().clear();
            String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
            darkModeCheckbox.getScene().getStylesheets().add(lightThemePath);
            darkModeCheckbox.getScene().getRoot().getProperties().put("theme", "light");

            // Remove dark-mode class from scene root
            darkModeCheckbox.getScene().getRoot().getStyleClass().remove("dark-mode");

            // Remove dark mode from all elements
            removeDarkModeFromAllNodes(darkModeCheckbox.getScene().getRoot());
        }
    }

    /**
     * Explicitly applies dark mode styling to all UI elements
     */
    private void styleForDarkMode() {
        // Style text fields with dark backgrounds and white text
        firstNameField.getStyleClass().add("dark-mode");
        lastNameField.getStyleClass().add("dark-mode");
        currentPasswordField.getStyleClass().add("dark-mode");
        newPasswordField.getStyleClass().add("dark-mode");

        // Explicit styling for text fields
        firstNameField.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-control-inner-background: #2a2a2a;");
        lastNameField.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-control-inner-background: #2a2a2a;");
        currentPasswordField.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-control-inner-background: #2a2a2a;");
        newPasswordField.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-control-inner-background: #2a2a2a;");

        // Ensure all labels have white text
        usernameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        emailLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        createdDateLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        statusLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        preferencesStatusLabel.setTextFill(javafx.scene.paint.Color.WHITE);

        // Apply dark-mode class to all labels to ensure CSS styling is applied
        usernameLabel.getStyleClass().add("dark-mode");
        emailLabel.getStyleClass().add("dark-mode");
        createdDateLabel.getStyleClass().add("dark-mode");
        statusLabel.getStyleClass().add("dark-mode");
        preferencesStatusLabel.getStyleClass().add("dark-mode");

        // Force all tab backgrounds to be dark
        if (darkModeCheckbox.getScene() != null) {
            // Find and style all TabPanes
            javafx.scene.control.TabPane tabPane = (javafx.scene.control.TabPane) darkModeCheckbox.getScene()
                    .lookup(".tab-pane");
            if (tabPane != null) {
                // Style tab pane
                tabPane.setStyle("-fx-background-color: #1e1e1e;");

                // Get the tab header area
                javafx.scene.Node tabHeaderArea = tabPane.lookup(".tab-header-area");
                if (tabHeaderArea != null) {
                    tabHeaderArea.setStyle("-fx-background-color: #1a1a1a !important;");
                }

                // Get the tab header background
                javafx.scene.Node tabHeaderBackground = tabPane.lookup(".tab-header-background");
                if (tabHeaderBackground != null) {
                    tabHeaderBackground.setStyle("-fx-background-color: #1a1a1a !important;");
                }

                // Style each tab content
                for (javafx.scene.control.Tab tab : tabPane.getTabs()) {
                    // Style the tab content areas
                    if (tab.getContent() instanceof javafx.scene.Parent) {
                        javafx.scene.Parent content = (javafx.scene.Parent) tab.getContent();
                        content.setStyle("-fx-background-color: #1e1e1e;");

                        // Find all labels and set them to white
                        applyWhiteTextToAllLabels(content);
                    }
                }
            }
        }
    }

    /**
     * Recursively applies white text to all labels in the scene graph
     */
    private void applyWhiteTextToAllLabels(javafx.scene.Parent parent) {
        if (parent == null)
            return;

        if (parent instanceof javafx.scene.control.Label) {
            ((javafx.scene.control.Label) parent).setTextFill(javafx.scene.paint.Color.WHITE);
        }

        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.control.Label) {
                ((javafx.scene.control.Label) node).setTextFill(javafx.scene.paint.Color.WHITE);
            }

            if (node instanceof javafx.scene.Parent) {
                applyWhiteTextToAllLabels((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Force all tab panes to have dark header backgrounds
     */
    private void forceDarkTabHeaders() {
        if (darkModeCheckbox.getScene() == null)
            return;

        // Find all TabPanes in the scene
        for (javafx.scene.control.TabPane tabPane : findAllNodesOfType(darkModeCheckbox.getScene().getRoot(),
                javafx.scene.control.TabPane.class)) {

            // Style tab pane background
            tabPane.setStyle("-fx-background-color: #1e1e1e !important;");

            // Force dark styling on tab header area
            javafx.scene.Node tabHeaderArea = tabPane.lookup(".tab-header-area");
            if (tabHeaderArea != null) {
                tabHeaderArea.setStyle("-fx-background-color: #121212 !important;");
            }

            // Force dark styling on tab header background
            javafx.scene.Node tabHeaderBackground = tabPane.lookup(".tab-header-background");
            if (tabHeaderBackground != null) {
                tabHeaderBackground.setStyle("-fx-background-color: #121212 !important;");
            }

            // Make tab labels white
            for (javafx.scene.control.Tab tab : tabPane.getTabs()) {
                // Style the tab label
                javafx.scene.Node tabLabel = tab.getGraphic();
                if (tabLabel instanceof javafx.scene.control.Labeled) {
                    ((javafx.scene.control.Labeled) tabLabel).setTextFill(javafx.scene.paint.Color.WHITE);
                    ((javafx.scene.control.Labeled) tabLabel).setStyle("-fx-text-fill: white !important;");
                }

                // Force white text on all labels within tab content
                if (tab.getContent() instanceof javafx.scene.Parent) {
                    forceLabelsToWhite((javafx.scene.Parent) tab.getContent());
                }
            }
        }
    }

    /**
     * Helper method to find all nodes of a specific type in the scene graph
     */
    private <T> List<T> findAllNodesOfType(javafx.scene.Node root, Class<T> type) {
        List<T> nodes = new ArrayList<>();
        if (root == null)
            return nodes;

        if (type.isInstance(root)) {
            nodes.add(type.cast(root));
        }

        if (root instanceof javafx.scene.Parent) {
            for (javafx.scene.Node node : ((javafx.scene.Parent) root).getChildrenUnmodifiable()) {
                nodes.addAll(findAllNodesOfType(node, type));
            }
        }

        return nodes;
    }

    /**
     * Force all labels to have white text
     */
    private void forceLabelsToWhite(javafx.scene.Parent parent) {
        if (parent == null)
            return;

        if (parent instanceof javafx.scene.control.Label) {
            javafx.scene.control.Label label = (javafx.scene.control.Label) parent;
            label.setTextFill(javafx.scene.paint.Color.WHITE);
            label.setStyle("-fx-text-fill: white !important;");
        }

        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.control.Label) {
                javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                label.setTextFill(javafx.scene.paint.Color.WHITE);
                label.setStyle("-fx-text-fill: white !important;");
            }

            if (node instanceof javafx.scene.Parent) {
                forceLabelsToWhite((javafx.scene.Parent) node);
            }
        }
    }

    @FXML
    private void handleDeactivateAccount() {
        // Show confirmation dialog
        boolean confirm = SceneManager.showConfirmationDialog(
                "Confirm Deactivation",
                "Are you sure you want to deactivate your account?",
                "Your account will be hidden for 7 days after which it will be permanently deleted if not reactivated.");

        if (confirm) {
            // Deactivate the account
            dataManager.deactivateUser(currentUser.getUserId());

            // Show confirmation and log the user out
            SceneManager.showAlert("Account Deactivated",
                    "Your account has been deactivated. You can reactivate it within 7 days by logging in.");

            // Log out
            dataManager.setCurrentUser(null);
            SceneManager.loadScene("LoginView.fxml");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.loadScene("HomeView.fxml");
    }
}